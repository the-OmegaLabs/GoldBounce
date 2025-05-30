/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.bmcDamageBoost
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.bmcLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.damageLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.fullStrafe
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.groundSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.playerOnGroundTicks
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.safeY
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.airTicks
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.skid.crosssine.MovementUtils.setMotion
import net.minecraft.potion.Potion

object BlocksMCHop : SpeedMode("BlocksMCHop") {

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInWater || player.isInLava || player.isInWeb || player.isOnLadder) return

        if (player.isMoving) {
            if (player.onGround) {
                player.tryJump()
            } else {
                if (airTicks == 4 && player.hurtTime == 0 && player.motionY > 0) {
                    player.motionY = -0.09800000190734863
                }

                if (fullStrafe) {
                    strafe(speed - 0.004F)
                } else if (airTicks >= 6) {
                    strafe()
                }
            }

            if (bmcDamageBoost && player.hurtTime > 0) {
                strafe(0.5f)
            }
        }
    }

    override fun onMotion() {
        if (groundSpeed){
            if (playerOnGroundTicks % 15 === 0) {
                strafe(0.2F)
            }
            if (playerOnGroundTicks % 15 === 1) {
                setMotion(0.1822)
            }
            if (mc.thePlayer.hurtTime > 0) {
                strafe(0.5F)
            }
        }
    }

    override fun onTick() {
        if (mc.thePlayer?.isSneaking == true)
            return
        if (mc.thePlayer.onGround){
            playerOnGroundTicks++
        } else {
            playerOnGroundTicks = 0
        }
    }
}