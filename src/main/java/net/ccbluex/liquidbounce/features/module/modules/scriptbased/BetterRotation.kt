package net.ccbluex.liquidbounce.features.module.modules.scriptbased

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object BetterRotation : Module("BetterRotation", Category.PLAYER, gameDetecting = false, hideModule = false) {
    val desc = TextValue("Author","bzym2,RN_RANDOM_NAME(base version)")
    val versionDesc = TextValue("Version","3")
    private var lastRenderYaw = 0f
    private var lastYaw = 0f
    private var allowedToUpdate = false
    private var predictedPosition: Vec3? = null
    private val coolDownTimer = TickTimer()
    private var simulatedPlayer: SimulatedPlayer? = null

    private data class SimulatedPlayer(
        var posX: Double,
        var posY: Double,
        var posZ: Double,
        var motionX: Double,
        var motionY: Double,
        var motionZ: Double,
        val gravity: Double = -0.08
    )

    @EventTarget
    fun onTick(event: TickEvent) {
        // 更新模拟玩家
        mc.thePlayer?.let { player ->
            simulatedPlayer = createSimulatedPlayer(player)
            predictedPosition = updatePlayer(5)
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        coolDownTimer.reset()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val player = mc.thePlayer ?: return
        val currentYawHead = RotationUtils.serverRotation.yaw
        val key = mc.gameSettings

        if (coolDownTimer.hasTimePassed(400)) {
            if (key.keyBindForward.isKeyDown || key.keyBindBack.isKeyDown ||
                key.keyBindLeft.isKeyDown || key.keyBindRight.isKeyDown) {

                predictedPosition?.let { pos ->
                    val targetYaw = RotationUtils.toRotation(pos, false).yaw
                    val yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - currentYawHead)
                        .coerceIn(-45f, 45f)

                    lastRenderYaw = smoothTransition(lastRenderYaw, currentYawHead + yawDiff, 0.33f)
                    player.renderYawOffset = lastRenderYaw
                    allowedToUpdate = true
                }
            } else {
                val currentRenderYaw = MathHelper.wrapAngleTo180_float(player.renderYawOffset)
                val yawDiff = shortestYawPath(currentRenderYaw, currentYawHead)

                val targetYaw = when {
                    yawDiff > 45 -> currentYawHead - 45
                    yawDiff < -45 -> currentYawHead + 45
                    else -> lastRenderYaw
                }

                lastRenderYaw = smoothTransition(lastRenderYaw, targetYaw, 0.33f)
                player.renderYawOffset = lastRenderYaw

                if (abs(shortestYawPath(lastYaw, currentYawHead)) < 45 && allowedToUpdate) {
                    player.renderYawOffset = lastYaw
                }
            }
        } else {
            lastRenderYaw = smoothTransition(lastRenderYaw, currentYawHead, 0.08f)
            player.renderYawOffset = lastRenderYaw
            lastYaw = lastRenderYaw
        }
    }

    // 核心预测逻辑
    private fun createSimulatedPlayer(player: net.minecraft.entity.player.EntityPlayer): SimulatedPlayer {
        return SimulatedPlayer(
            player.posX,
            player.posY,
            player.posZ,
            player.motionX,
            player.motionY,
            player.motionZ
        )
    }

    private fun updatePlayer(ticks: Int): Vec3? {
        simulatedPlayer?.let { sp ->
            // 处理运动预测
            if (mc.thePlayer.movementInput.jump) {
                sp.motionY = 0.4
            }

            val strafe = mc.thePlayer.movementInput.moveStrafe
            val forward = mc.thePlayer.movementInput.moveForward

            if (strafe != 0f || forward != 0f) {
                val multiplier = if (mc.thePlayer.isSprinting) 1.3f else 1f
                val frictionFactor = mc.thePlayer.jumpMovementFactor * multiplier

                val sinYaw = sin(Math.toRadians(mc.thePlayer.rotationYaw.toDouble()))
                val cosYaw = cos(Math.toRadians(mc.thePlayer.rotationYaw.toDouble()))

                sp.motionX += (strafe * cosYaw - forward * sinYaw) * frictionFactor
                sp.motionZ += (forward * cosYaw + strafe * sinYaw) * frictionFactor

                sp.motionX *= 0.91
                sp.motionZ *= 0.91
            }

            // 更新位置
            sp.posX += sp.motionX * ticks
            sp.posZ += sp.motionZ * ticks
            sp.posY += sp.motionY * ticks

            return Vec3(sp.posX, sp.posY, sp.posZ)
        }
        return null
    }

    // 辅助函数
    private fun shortestYawPath(current: Float, target: Float): Float {
        var diff = (target - current) % 360
        if (diff < -180) diff += 360
        if (diff > 180) diff -= 360
        return diff
    }

    private fun smoothTransition(current: Float, target: Float, factor: Float): Float {
        val delta = MathHelper.wrapAngleTo180_float(target - current)
        return current + delta * factor
    }

    override fun onDisable() {
        lastRenderYaw = 0f
        lastYaw = 0f
        allowedToUpdate = false
    }
}