/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.settings

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.*

object Camera : Module("Camera", Category.SETTINGS, gameDetecting = false, hideModule = false) {
    val motionCamera = BoolValue("MotionCamera", true)
    val interpolation = FloatValue("MotionInterpolation", 0.05f, 0.01f..0.5f) { motionCamera.get()}
    @EventTarget
    override fun onEnable() {
        LiquidBounce.moduleManager.getModule(Camera::class.java).state = false
    }
}
