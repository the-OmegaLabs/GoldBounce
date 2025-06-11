/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.settings

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

object Camera : Module("Camera", Category.SETTINGS, gameDetecting = false, hideModule = false, canBeEnabled = false) {
    val 呵呵臭贝贝是不是想抄我码子 = "你给我操我就让你抄码子嘻嘻"

    val motionCamera = BoolValue("MotionCamera", true)
    val interpolation = FloatValue("MotionInterpolation", 0.05f, 0.01f..0.5f) { motionCamera.get()}
    override fun onEnable() {
        LiquidBounce.moduleManager.getModule(Camera::class.java).state = false
    }
    override val tag
        get() = interpolation.get().toString()
}
