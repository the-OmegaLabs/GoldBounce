package net.ccbluex.liquidbounce.features.module.modules.hud

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

object Logo : Module("Logo",Category.HUD) {
    val mode by ListValue("Mode", arrayOf("FanArt","Bounce","Sketch","Static","Duck","Raven"), "Duck")
    val xoffset by int("XOffset", 10, -100..100)
    val yoffset by int("YOffset", 10, -100..100)
    val colorR by int("ColorR", 255, 0..255)
    val colorG by int("ColorG", 255, 0..255)
    val colorB by int("ColorB", 255, 0..255)
    val colorA by int("ColorAlpha", 255, 0..255)
    val color = Color(colorR, colorG, colorB, colorA)
    val scaledX = xoffset
    val scaledY = ScaledResolution(mc).scaledHeight + yoffset
    @EventTarget
    fun onRender2D(event: Render2DEvent){
        when (mode){

        }
    }
}