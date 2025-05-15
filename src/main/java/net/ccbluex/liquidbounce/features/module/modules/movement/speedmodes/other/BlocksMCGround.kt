/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.playerOnGroundTicks
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.skid.crosssine.MovementUtils.setMotion

object BlocksMCGround : SpeedMode("BlocksMCGround") {

    override fun onMotion() {
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