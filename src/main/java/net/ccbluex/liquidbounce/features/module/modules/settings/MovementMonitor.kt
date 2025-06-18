package net.ccbluex.liquidbounce.features.module.modules.settings

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.settings.MovementMonitor
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.entity.living.LivingFallEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.util.*
import kotlin.math.abs


object MovementMonitor : Module("ModuleMonitor", Category.SETTINGS) {
    private val playerData: MutableMap<UUID?, PlayerMovementData> = HashMap()
    private var lastReportTime: Long = 0
    const val REPORT_INTERVAL: Long = 5000 // 5秒报告间隔
    // 增强的玩家运动数据结构
    class PlayerMovementData {
        var lastX: Double = 0.0
        var lastY: Double = 0.0
        var lastZ: Double = 0.0
        var totalMotionX: Double = 0.0
        var totalMotionY: Double = 0.0
        var totalMotionZ: Double = 0.0
        var tickCount: Int = 0
        var maxFallDistance: Double = 0.0
        var maxAirTime: Int = 0
        var currentAirTime: Int = 0
        var wasOnGround: Boolean = false
        var wasJumping: Boolean = false
        var isFalling: Boolean = false
    }

    @SubscribeEvent
    fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return

        val player = event.player
        val uuid = player.getUniqueID()
        val data = playerData.computeIfAbsent(uuid) { k: UUID? ->
            val newData = PlayerMovementData()
            newData.lastX = player.posX
            newData.lastY = player.posY
            newData.lastZ = player.posZ
            newData.wasOnGround = player.onGround
            newData
        }


        // 计算瞬时动量
        val motionX = player.posX - data.lastX
        val motionY = player.posY - data.lastY
        val motionZ = player.posZ - data.lastZ


        // 更新位置
        data.lastX = player.posX
        data.lastY = player.posY
        data.lastZ = player.posZ


        // 累计动量用于平均值计算
        data.totalMotionX += motionX
        data.totalMotionY += motionY
        data.totalMotionZ += motionZ
        data.tickCount++


        // 检测跳跃开始
        if (!data.wasJumping && player.isAirBorne && player.onGround) {
            logMovement(
                player,
                "JUMP_START",
                motionX,
                motionY,
                motionZ,
                player.fallDistance.toDouble(),
                data.currentAirTime
            )
            data.wasJumping = true
        }


        // 空中状态处理
        if (!player.onGround) {
            data.currentAirTime++
            if (data.currentAirTime > data.maxAirTime) {
                data.maxAirTime = data.currentAirTime
            }
            data.isFalling = motionY < 0 // 下落状态检测
        } else {
            if (data.currentAirTime > 0) {
                logMovement(
                    player,
                    "LANDED",
                    motionX,
                    motionY,
                    motionZ,
                    player.fallDistance.toDouble(),
                    data.currentAirTime
                )
            }
            data.currentAirTime = 0
            data.isFalling = false
            data.wasJumping = false
        }


        // 更新地面状态
        data.wasOnGround = player.onGround
    }

    @SubscribeEvent
    fun onLivingUpdate(event: LivingUpdateEvent) {
        if (event.entity !is EntityPlayer) return

        val player = event.entity as EntityPlayer
        val data = playerData.get(player.getUniqueID())
        if (data == null) return


        // 记录空中转向
        if (!player.onGround && (abs(player.moveStrafing) > 0.1f || abs(player.moveForward) > 0.1f)) {
            logMovement(
                player, "AIR_STRAFE",
                player.motionX, player.motionY, player.motionZ,
                player.fallDistance.toDouble(), data.currentAirTime
            )
        }
    }

    @SubscribeEvent
    fun onPlayerFall(event: LivingFallEvent) {
        if (event.entity !is EntityPlayer) return

        val player = event.entity as EntityPlayer
        val data = playerData.get(player.getUniqueID())
        if (data == null) return


        // 记录最大下落距离
        if (event.distance > data.maxFallDistance) {
            data.maxFallDistance = event.distance.toDouble()
        }

        logMovement(
            player, "FALL_IMPACT",
            player.motionX, player.motionY, player.motionZ,
            event.distance.toDouble(), data.currentAirTime
        )
    }

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return


        // 定期报告所有玩家数据
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastReportTime > REPORT_INTERVAL) {
            for (entry in playerData.entries) {
                val player = Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(entry.key)
                if (player != null) {
                    reportMovementData(player, entry.value)
                    // 重置周期数据
                    entry.value.totalMotionX = 0.0
                    entry.value.totalMotionY = 0.0
                    entry.value.totalMotionZ = 0.0
                    entry.value.tickCount = 0
                    entry.value.maxAirTime = 0
                }
            }
            lastReportTime = currentTime
        }
    }

    private fun logMovement(
        player: EntityPlayer, type: String?,
        motionX: Double, motionY: Double, motionZ: Double,
        fallDistance: Double, airTime: Int
    ) {
        // 使用玩家名称的纯文本形式
        val playerName: String? = player.commandSenderEntity.name
        val message = String.format(
            "[MOVEMENT] %s: %s | Momentum: %.4f,%.4f,%.4f | Fall: %.2f | AirTime: %d",
            playerName,
            type,
            motionX * 20,  // 转换为每秒单位
            motionY * 20,
            motionZ * 20,
            fallDistance,
            airTime
        )
        Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(message))
    }

    private fun reportMovementData(player: EntityPlayer, data: PlayerMovementData) {
        val playerName: String? = player.commandSenderEntity.name


        // 计算平均动量
        val avgX = if (data.tickCount > 0) (data.totalMotionX / data.tickCount) * 20 else 0.0
        val avgY = if (data.tickCount > 0) (data.totalMotionY / data.tickCount) * 20 else 0.0
        val avgZ = if (data.tickCount > 0) (data.totalMotionZ / data.tickCount) * 20 else 0.0

        val report = String.format(
            "[MOVEMENT_REPORT] %s: | AvgMomentum: %.4f,%.4f,%.4f | MaxFall: %.2f | MaxAirTime: %d",
            playerName,
            avgX, avgY, avgZ,
            data.maxFallDistance,
            data.maxAirTime
        )
        Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(report))


        // 重置最大下落距离
        data.maxFallDistance = 0.0
    }
}