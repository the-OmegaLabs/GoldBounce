/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.ClientApi
import net.ccbluex.liquidbounce.api.Status
import net.ccbluex.liquidbounce.api.autoSettingsList
import net.ccbluex.liquidbounce.api.loadSettings
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.ClientUtils.logger
import net.ccbluex.liquidbounce.utils.SettingsUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils.get
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URL

object SettingsCommand : Command("cloud", "cloudsettings") {
    var cloudSettings : Array<CloudSettings>? = null
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val usedAlias = args[0].lowercase()

        if (args.size <= 1) {
            chatSyntax("$usedAlias <load/list/upload/report>")
            return
        }

        GlobalScope.launch {
            when (args[1].lowercase()) {
                "load" -> loadSettings(args)
                "list" -> listSettings()
                else -> chatSyntax("$usedAlias <load/list>")
            }
        }
    }

    // Load subcommand
    private suspend fun loadSettings(args: Array<String>) {
        withContext(Dispatchers.IO) {
            if (args.size < 3) {
                chatSyntax("${args[0].lowercase()} load <name/url>")
                return@withContext
            }

            try {
                val settings = if (args[2].startsWith("http")) {
                    val (text, code) = get(args[2])
                    if (code != 200) {
                        error(text)
                    }

                    text
                } else {
                    try {
                        URL("https://hub.gitmirror.com/https://raw.githubusercontent.com/bzym2/GoldBounce-Cloud/refs/heads/main/${args[2]}.txt").readText()
                    } catch (e: Exception) {
                        chat("请求失败, ${e.message}")
                        return@withContext
                    }
                }
                chat("应用配置中...")
                SettingsUtils.applyScript(settings)
                chat("§6配置成功应用！")
                playEdit()
            } catch (e: Exception) {
                logger.error("Failed to load settings", e)
                chat("加载配置失败: ${e.message}")
            }
        }
    }
    fun loadCloudSettings(): Array<CloudSettings> {
        val url = "https://hub.gitmirror.com/https://raw.githubusercontent.com/bzym2/GoldBounce-Cloud/refs/heads/main/list.txt"

        return try {
            URL(url).readText() // 读取全部内容
                .lines()        // 按行拆分
                .filter { it.isNotBlank() } // 过滤空行
                .map { line ->
                    val parts = line.split(",") // 按英文逗号拆分
                    if (parts.size >= 4) {
                        CloudSettings(
                            name = parts[0].trim(),
                            date = parts[1].trim(),
                            desc = parts[2].trim(),
                            author = parts[3].trim()
                        )
                    } else {
                        // 如果某行格式不对，返回一个空对象或抛异常可自定义
                        CloudSettings("N/A", "N/A", "N/A", "N/A")
                    }
                }
                .toTypedArray() // 转成 Array
        } catch (e: Exception) {
            e.printStackTrace()
            emptyArray()
        }
    }

    // List subcommand
    private suspend fun listSettings() {
        withContext(Dispatchers.IO) {
            chat("加载云配中...")
            val settings = loadCloudSettings()
            if (settings.isEmpty()) {
                chat("加载失败！")
            }
            settings.forEach { it ->
                chat("参数${it.name} 描述: ${it.desc} 发布日期：${it.date} by ${it.author}")
            }
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) {
            return emptyList()
        }

        return when (args.size) {
            1 -> listOf("list", "load").filter { it.startsWith(args[0], true) }
            2 -> {
                when (args[0].lowercase()) {
                    "load"-> {
                        if (cloudSettings == null) {
                            cloudSettings = loadCloudSettings()
                        }
                        return cloudSettings?.filter { it.name.startsWith(args[1], true) }?.map { it.name }
                            ?: emptyList()
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}

data class CloudSettings(
    val name: String,
    val date: String,
    val desc: String,
    val author: String
)