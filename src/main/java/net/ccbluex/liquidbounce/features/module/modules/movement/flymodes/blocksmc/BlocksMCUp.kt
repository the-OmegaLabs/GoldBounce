package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.blocksmc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.features.module.modules.world.AntiVoid
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.KeyBinding
import net.minecraft.potion.Potion
import org.lwjgl.input.Keyboard


object BlocksMCUp : FlyMode("BlocksMCUp") {
    var i = 0
    var floatPos: Double = 0.0
    var offGroundTicks = 0

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f;
    }

    override fun onEnable() {
        i = 0
        floatPos = mc.thePlayer.posY
    }
    override fun onUpdate() {
        if (!mc.thePlayer.onGround) {
            offGroundTicks++
        } else {
            offGroundTicks = 0
        }
    }
    override fun onMotion(event: MotionEvent) {
        if (event.eventState.stateName == "PRE") {
            if (i == 6) {
                mc.thePlayer.posY = floatPos + 0.42
            }
            if (mc.thePlayer.onGround && i == 5) {
                ++i
                if (MovementUtils.isMoving()) {
                    mc.thePlayer.jump()
                    MovementUtils.strafe(if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 0.6F else 0.49F)
                }
            } else if (!mc.thePlayer.onGround && i == 5) {
                LiquidBounce.moduleManager.getModule(Fly::class.java).state = false
            } else if (offGroundTicks !== 6 || AntiVoid.checkVoid()) {
                if (offGroundTicks === 1) {
                    mc.timer.timerSpeed = 1.05f
                    MovementUtils.strafe(MovementUtils.speed * 1.08F)
                } else if (offGroundTicks === 2) {
                    mc.timer.timerSpeed = 1.15f
                    MovementUtils.strafe(MovementUtils.speed * 1.08F)
                } else if (offGroundTicks === 3) {
                    mc.timer.timerSpeed = 1.25f
                    MovementUtils.strafe(MovementUtils.speed * 1.06F)
                } else if (offGroundTicks >= 4) {
                    mc.timer.timerSpeed = 2.5f
                    MovementUtils.strafe(MovementUtils.speed * 1.02F)
                }
            }
            if (offGroundTicks >= 10) {
                LiquidBounce.moduleManager.getModule(Fly::class.java).state = false
            }
            if (i < 5) {
                ++i
            }
            if (i < 4) {
                mc.timer.timerSpeed = 0.5f
                mc.thePlayer.setSprinting(true)
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, false)
            } else {
                KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindForward.keyCode,
                    Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode)
                )
            }
            MovementUtils.strafe()
            if (mc.thePlayer.hurtTime <= 0) return
            MovementUtils.strafe(0.4F)
        }
    }
}