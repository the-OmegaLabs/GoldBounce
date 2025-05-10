package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ReflectionUtil
import net.ccbluex.liquidbounce.value.TextValue
import net.ccbluex.liquidbounce.value.int
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S38PacketPlayerListItem.AddPlayerData
import net.minecraft.util.ChatComponentText
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object IRC : Module("IRC", Category.MISC) {

    // 配置项
    private val host by TextValue("Host", "127.0.0.1")
    private val port by int("Port", 6666)
    private val commandPrefix by TextValue("Prefix", "/irc ")
    private val executor = Executors.newSingleThreadScheduledExecutor()

    // 连接相关
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null
    private var reconnectAttempts = 0

    // 数据存储
    private var loggedInNick: String? = null

    @JvmField
    val nameMap = mutableMapOf<String, Pair<String, String>>() // <UUID, (GameID, Nick)>

    // 颜色常量
    private const val COLOR_ERROR = "§c"
    private const val COLOR_SUCCESS = "§a"
    private const val COLOR_INFO = "§b"
    private const val COLOR_CHAT = "§7"

    override fun onEnable() = connect()

    private fun connect() {
        connectionLock.withLock {
            try {
                resetConnection()
                Socket().apply {
                    connect(InetSocketAddress(host, port), 5000)
                    soTimeout = 15000
                    keepAlive = true
                    socket = this
                    writer = BufferedWriter(OutputStreamWriter(getOutputStream()))
                    reader = BufferedReader(InputStreamReader(getInputStream()))
                    state = true
                }
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))

                executor.scheduleWithFixedDelay({ readLoop() }, 0, 50, TimeUnit.MILLISECONDS)
                scheduleHeartbeat()
                attemptAutoLogin()

            } catch (e: Exception) {
                handleConnectError(e)
            }
        }
    }

    private fun scheduleHeartbeat() {
        executor.scheduleAtFixedRate({
            writer?.apply {
                write("PING ${System.currentTimeMillis()}\n")
                flush()
            }
        }, 30, 30, TimeUnit.SECONDS)
    }

    private fun attemptAutoLogin() {
        // 自动登录逻辑（需实现凭证存储）
    }

    private fun handleConnectError(e: Exception) {
        mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_ERROR[IRC] 连接失败: ${e.message}"))
        if (reconnectAttempts++ < 3) {
            executor.schedule({ connect() }, 5, TimeUnit.SECONDS)
        } else {
            toggle()
        }
    }

    override fun onDisable() {
        executor.shutdownNow()
        writer?.apply {
            write("LOGOUT\n")
            flush()
            close()
        }
        reader?.close()
        socket?.close()
        nameMap.clear()
    }

    private fun readLoop() {
        try {
            while (reader?.ready() == true) {
                reader!!.readLine()?.let { processLine(it) }
            }
        } catch (_: Exception) {
        }
    }

    private fun processLine(line: String) {
        when {
            line.startsWith("OK REGISTER") ->
                mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_SUCCESS[IRC] 注册成功，请使用/login登录"))

            line.startsWith("OK LOGIN ") -> handleLoginSuccess(line)
            line.startsWith("NAMES ") -> updateNameMap(line)
            line.startsWith("MSG ") -> showChatMessage(line)
            line.startsWith("ERROR ") -> {
                val errorMsg = when (line.substringAfter("ERROR ")) {
                    "USER_EXISTS" -> "用户已存在"
                    "AUTH_FAILED" -> "认证失败"
                    else -> line.substringAfter("ERROR ")
                }
                mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_ERROR[IRC] $errorMsg"))
            }

            line.startsWith("PONG ") -> showLatency(line)
            else -> {/* 其他协议处理 */
            }
        }
    }

    private fun handleLoginSuccess(line: String) {
        loggedInNick = line.substringAfter("OK LOGIN ").trim()
        mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_SUCCESS[IRC] 登录成功"))
    }

    private fun updateNameMap(line: String) {
        nameMap.clear()
        line.substringAfter("NAMES ").split(",").forEach {
            val (uuid, gameId, nick) = it.split(":", limit = 3)
            nameMap[uuid] = Pair(gameId, nick)
        }
    }

    private fun showChatMessage(line: String) {
        val (gameId, msg) = line.substringAfter("MSG ").split(":", limit = 2)
        nameMap.values.find { it.first == gameId }?.let {
            mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_CHAT$gameId[${it.second}]§7: $msg"))
        }
    }

    private fun showErrorMessage(line: String) {
        val error = line.substringAfter("ERROR ")
        mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_ERROR[IRC] $error"))
    }

    private fun showLatency(line: String) {
        val latency = System.currentTimeMillis() - line.substringAfter("PONG ").toLong()
        mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_INFO[IRC] 延迟: ${latency}ms"))
    }

    @EventTarget
    fun onPacketSend(event: PacketEvent) {
        val packet = event.packet
        if (packet is C01PacketChatMessage && packet.message.startsWith(commandPrefix)) {
            event.cancelEvent()
            handleCommand(packet.message.removePrefix(commandPrefix))
        }
    }

    private fun handleCommand(cmd: String) {
        val parts = cmd.trim().split(" ", limit = 3)
        when (parts[0].uppercase()) {
            "REGISTER" -> registerUser(parts)
            "LOGIN" -> loginUser(parts)
            "MSG" -> sendMessage(parts)
            "STATUS" -> showStatus()
            else -> showHelp()
        }
    }

    private val connectionLock = ReentrantLock()

    private fun checkConnection(): Boolean = connectionLock.withLock {
        socket?.run { !isClosed && isConnected } ?: false
    }

    private fun registerUser(parts: List<String>) {
        if (parts.size < 3) {
            mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_ERROR[IRC] 用法: /irc register 用户名 密码"))
            return
        }

        writer?.apply {
            write("REGISTER ${parts[1]} ${parts[2]}\n")
            flush()
            mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_INFO[IRC] 注册请求已发送"))
        }
    }

    private fun loginUser(parts: List<String>) {
        if (parts.size < 3) {
            mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_ERROR[IRC] 用法: /irc login 用户名 密码"))
            return
        }

        writer?.apply {
            write("LOGIN ${parts[1]} ${parts[2]}\n")
            flush()
            mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_INFO[IRC] 登录请求已发送"))
        }
    }

    private fun showStatus() {
        val status = buildString {
            appendLine("§6=== IRC状态 ===")
            appendLine("§a连接状态: ${if (socket?.isConnected == true) "已连接" else "断开"}")
            appendLine("§b服务器: $host:$port")
            appendLine("§e登录用户: ${loggedInNick ?: "未登录"}")
            appendLine("§d在线人数: ${nameMap.size}")
            appendLine("§7使用/irc help查看帮助")
        }
        mc.thePlayer.addChatMessage(ChatComponentText(status))
    }

    private fun showHelp() {
        val help = """
        §6=== IRC帮助 ===
        §a/irc register <用户名> <密码> §7- 注册新账户
        §a/irc login <用户名> <密码> §7- 登录账户
        §a/irc msg <消息> §7- 发送聊天消息
        §a/irc status §7- 查看连接状态
        §a/irc help §7- 显示本帮助
    """.trimIndent()
        mc.thePlayer.addChatMessage(ChatComponentText(help))
    }

    private fun sendMessage(parts: List<String>) {
        if (!checkConnection()) {
            mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_ERROR[IRC] 未连接服务器"))
            return
        }

        connectionLock.withLock {
            try {
                writer?.apply {
                    write("MSG ${parts.drop(1).joinToString(" ")}\n")
                    flush()
                } ?: throw IOException("Writer is null")
            } catch (e: IOException) {
                handleSendError(e)
            }
        }
    }

    private fun resetConnection() {
        connectionLock.withLock {
            try {
                writer?.close()
                reader?.close()
                socket?.close()
            } catch (e: IOException) {
                // 忽略关闭异常
            } finally {
                writer = null
                reader = null
                socket = null
                state = false
            }
        }
    }

    private fun handleSendError(e: Exception) {
        mc.thePlayer.addChatMessage(ChatComponentText("$COLOR_ERROR[IRC] 发送失败: ${e.message}"))
        resetConnection()
        if (state) { // 仅在模块启用时重连
            executor.schedule({ connect() }, 1, TimeUnit.SECONDS)
        }
    }

    @EventTarget
    fun onPacketReceive(event: PacketEvent) {
        when (val packet = event.packet) {
            is S38PacketPlayerListItem -> updatePlayerList(packet)
            is S02PacketChat -> modifyChatMessage(packet)
        }
    }

    private fun updatePlayerList(packet: S38PacketPlayerListItem) {
        packet.entries.forEach { entry ->
            entry.profile.id.toString().let { uuid ->
                nameMap[uuid]?.let { (gameId, nick) ->
                    ReflectionUtil.setFieldValue(
                        entry, "displayName",
                        ChatComponentText("§b$gameId§7[§f$nick§7]")
                    )
                }
            }
        }
    }

    private fun modifyChatMessage(packet: S02PacketChat) {
        var text = packet.chatComponent.unformattedText
        nameMap.values.forEach { (gameId, nick) ->
            text = text.replace(gameId, "$gameId[$nick]")
        }
        ReflectionUtil.setFieldValue(packet, "chatComponent", ChatComponentText(text))
    }
}
