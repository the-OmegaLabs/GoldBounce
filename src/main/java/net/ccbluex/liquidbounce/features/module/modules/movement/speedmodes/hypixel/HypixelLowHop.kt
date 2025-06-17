/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.hypixel

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.glide
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.airTicks
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.minecraft.potion.Potion

/**
 * Working on Hypixel (Watchdog)
 * Tested on: play.hypixel.net
 * Credit: @LiquidSquid / Nextgen, with logic from Faiths Client
 */
object HypixelLowHop : SpeedMode("HypixelLowHop") {

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        // FastStop logic from Faiths client
        if (!player.isMoving) {
            player.motionX = 0.0
            player.motionZ = 0.0
            return
        }

        // Force sprinting from Faiths client
        player.isSprinting = true

        if (player.onGround) {
            // Jump and let onJump handle the speed
            player.tryJump()
            return
        } else {
            // MotionY-based speed boost from Faiths' updateEventHandler
            val motionY = player.motionY
            if (motionY < 0.1 && motionY > 0.01) {
                player.motionX *= 1.005
                player.motionZ *= 1.005
            }
            if (motionY < 0.005 && motionY > 0.0) {
                player.motionX *= 1.005
                player.motionZ *= 1.005
            }
            if (motionY < 0.001 && motionY > -0.03) {
                if (player.isPotionActive(Potion.moveSpeed)) {
                    player.motionX *= 1.005
                    player.motionZ *= 1.005
                } else {
                    player.motionX *= 1.002
                    player.motionZ *= 1.002
                }
            }

            // Merged logic for air ticks from both original code and Faiths client
            when (airTicks) {
                4 -> {
                    // From original GoldBounce code
                    player.motionY -= 0.03
                }
                5 -> {
                    // From Faiths: strafe. From original GoldBounce: Y motion adjustment.
                    strafe(0.315f)
                    player.motionY -= 0.1905189780583944
                }
                6 -> {
                    // From Faiths: strafe. From original GoldBounce: Y motion adjustment.
                    strafe()
                    player.motionY *= 1.01
                }
                7 -> {
                    // From original GoldBounce code
                    if (glide) player.motionY /= 1.5
                }
            }

            // Kept from original code for longer glides
            if (airTicks >= 7 && glide) {
                strafe(speed = speed.coerceAtLeast(0.281F), strength = 0.7)
            }

            // Kept from original code for damage boost
            if (player.hurtTime == 9) {
                strafe()
            }
        }
    }

    override fun onJump(event: JumpEvent) {
        val player = mc.thePlayer ?: return
        if (!player.isMoving) {
            event.cancelEvent() // Don't jump if not moving
            return
        }
        // Logic from Faiths client's onGround jump (case 0): strafe with a fixed high value
        strafe(0.485f)
    }
}