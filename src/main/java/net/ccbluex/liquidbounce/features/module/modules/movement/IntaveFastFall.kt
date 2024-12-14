package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.float

object IntaveFastFall : Module("FastFall", Category.MOVEMENT, hideModule = false) {

    private val timerSpeed = float("TimerSpeed", 1F, 0F..5F)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // If player is on the ground, slow down the timer
        if (mc.thePlayer.onGround) {
            mc.timer.timerSpeed = 0.2F
        } else {
            // If player is in the air, increase the timer speed
            mc.timer.timerSpeed = 5F
        }
    }

    override fun onEnable() {
        super.onEnable()
        // Set the default timer speed when module is enabled
        mc.timer.timerSpeed = 1F
    }

    override fun onDisable() {
        super.onDisable()
        // Reset timer speed to normal when module is disabled
        mc.timer.timerSpeed = 1F
    }
}
