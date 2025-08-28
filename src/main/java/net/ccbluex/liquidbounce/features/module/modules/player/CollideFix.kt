package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import kotlin.math.max

// 此模块为空壳 实际修复 by wawa
object CollideFix : Module("CollideFix", Category.PLAYER) {
    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if (mc.thePlayer.isOnLadder) {
            if (!mc.thePlayer.isSneaking) {
                if (mc.thePlayer.motionY in 0.0..0.06) {
                    mc.thePlayer.motionY = max(mc.thePlayer.motionY, 0.06)
                }
            }
        }
    }
}