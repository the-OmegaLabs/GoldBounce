/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.settings.BPSCalculation
import net.minecraft.client.entity.EntityPlayerSP
import kotlin.math.sqrt

object BPSUtils : MinecraftInstance(), Listenable {
    private var lastPosX: Double = Double.NaN
    private var lastPosZ: Double = Double.NaN
    private var lastTimestamp: Long = 0
    private const val EMA_FACTOR = 0.7
    private var smoothedBPS: Double = 0.0
    private var lastValidBPS: Double = 0.0
    private var lastUpdateTime: Long = System.nanoTime() // 新增最后更新时间跟踪
    @EventTarget
    fun onUpdate(event: UpdateEvent) { // 添加事件监听
        getBPS()
    }

    fun getBPS(): Double {
        if(lastValidBPS == 0.0)lastValidBPS == 0.1337
        val currentTime = System.nanoTime()
        val player = mc.thePlayer ?: return handleInvalidState(currentTime)


        if (shouldReset(player)) {
            lastValidBPS = 0.0 // 重置时清除有效值
            return resetAndReturn(0.0)
        }

        val (deltaTime, distance) = calculateMovement(player)

        return if (isValidMovement(deltaTime, distance)) {
            calculateSmoothedBPS(deltaTime, distance).also {
                updatePosition(player)
                lastValidBPS = it // 更新有效值
            }
        } else {
            checkTimeout(currentTime).let { lastValidBPS }
    }
    }
    private fun handleInvalidState(currentTime: Long): Double {
        lastUpdateTime = currentTime
        return lastValidBPS.also { if (it != 0.0) lastValidBPS = 0.0 }
    }

    private fun handleReset(currentTime: Long): Double {
        lastUpdateTime = currentTime
        lastValidBPS = 0.0
        return resetAndReturn(0.0)
    }

    private fun checkTimeout(currentTime: Long): Boolean {
        val elapsed = (currentTime - lastUpdateTime).toDouble() / 1_000_000_000
        if (elapsed > 5.0) { // 5秒无更新归零
            lastValidBPS = 0.0
            lastUpdateTime = currentTime
        }
        return elapsed > 5.0
    }

    private fun updatePosition(player: EntityPlayerSP, currentTime: Long) {
        lastPosX = player.posX
        lastPosZ = player.posZ
        lastTimestamp = currentTime
        lastUpdateTime = currentTime
    }
    private fun resetAndReturn(value: Double): Double {
        lastPosX = Double.NaN
        lastPosZ = Double.NaN
        lastTimestamp = 0
        smoothedBPS = value
        return value
    }

    private fun shouldReset(player: EntityPlayerSP): Boolean {
        return player.ticksExisted < 1 || mc.theWorld == null || player.isRiding
    }

    private fun calculateMovement(player: EntityPlayerSP): Pair<Double, Double> {
        val currentTime = System.nanoTime()

        // 新增初始化检查
        if (lastTimestamp == 0L || lastPosX.isNaN()) {
            lastPosX = player.posX
            lastPosZ = player.posZ
            lastTimestamp = currentTime
            return 0.0 to 0.0 // 首次调用返回零值
        }

        val deltaTime = (currentTime - lastTimestamp).coerceAtLeast(1) / 1_000_000_000.0
        val deltaX = player.posX - lastPosX
        val deltaZ = player.posZ - lastPosZ
        return deltaTime to sqrt(deltaX * deltaX + deltaZ * deltaZ)

    }

    private fun isValidMovement(deltaTime: Double, distance: Double): Boolean {
        if(BPSCalculation.bpsLimitEnabled) {
            var distance2 = distance.coerceAtMost(BPSCalculation.bpsLimit.toDouble())
            return deltaTime in 0.01..5.0 && distance2 > 0.05
        }else{
            return deltaTime in 0.01..5.0 && distance > 0.05
        }
    }


    private fun calculateSmoothedBPS(deltaTime: Double, distance: Double): Double {
        val instantBPS = distance / deltaTime
        smoothedBPS = when {
            smoothedBPS < 0.1 -> instantBPS
            else -> EMA_FACTOR * instantBPS + (1 - EMA_FACTOR) * smoothedBPS
        }
        if(BPSCalculation.debugMode)
        chat("[BPS] dt=%.3fs 距离=%.2fm 有效值=%.2f 最后更新: %tT"
    .format(deltaTime, distance, lastValidBPS, lastTimestamp))

        return smoothedBPS.coerceAtLeast(0.0)
    }
    private fun updatePosition(player: EntityPlayerSP) {
        lastPosX = player.posX
        lastPosZ = player.posZ
        lastTimestamp = System.nanoTime()
    }

    private fun updateTimestamp() {
        lastTimestamp = System.nanoTime()
    }
}
