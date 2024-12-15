/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.*

object Camera : Module("Camera", Category.RENDER, gameDetecting = false, hideModule = false) {
    val nobob = BoolValue("NoBob", false)
    val motionCamera = true
    val interpolation = FloatValue("MotionInterpolation", 0.05f, 0.01f..0.5f)
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if(nobob.get()){
            mc.thePlayer?.distanceWalkedModified = -1f
        }
    }
}
