package net.ccbluex.liquidbounce.features.module.modules.hud

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.floor

object Logo : Module("Logo",Category.HUD) {
    val mode by ListValue("Mode", arrayOf("ESound", "Bounce", "Sketch", "Static", "Tenna", "Nigga"), "Tenna")
    val xoffset by int("XOffset", 10, -100..100)
    val yoffset by int("YOffset", 10, -100..100)
    val colorR by int("ColorR", 255, 0..255)
    val colorG by int("ColorG", 255, 0..255)
    val colorB by int("ColorB", 255, 0..255)
    val colorA by int("ColorAlpha", 255, 0..255)
    val color = Color(colorR, colorG, colorB, colorA)
    var scaledX = 0
    var scaledY = 0
    var tennaFrame = 1
    var speedCounter = 0
    @EventTarget
    fun onRender2D(event: Render2DEvent){
        scaledX = xoffset
        scaledY = yoffset
        when (mode){
            "Bounce" -> {
                RenderUtils.drawImage(ResourceLocation("liquidbounce/icons/bouncerect.png"), scaledX, scaledY, 100, 100)
            }

            "Static" -> {
                Fonts.minecraftFont.drawStringWithShadow("G", scaledX.toFloat(), scaledY.toFloat(), Color.YELLOW.rgb)
                Fonts.minecraftFont.drawStringWithShadow(
                    "oldBounce", scaledX.toFloat() + Fonts.minecraftFont.getStringWidth("G"), scaledY.toFloat(),
                    Color.WHITE.rgb
                )
            }

            "Sketch" -> {
                RenderUtils.drawImage(ResourceLocation("liquidbounce/icons/sketch.png"), scaledX, scaledY, 99, 23)
            }

            "ESound" -> {
                RenderUtils.drawImage(ResourceLocation("liquidbounce/icons/esound.png"), scaledX, scaledY, 100, 100)
            }

            "Nigga" -> {
                RenderUtils.drawImage(ResourceLocation("liquidbounce/icons/blocksmc.png"), scaledX, scaledY, 100, 100)
            }
            "Tenna" -> {
                if (tennaFrame == 187) {
                    tennaFrame = 1
                }
                RenderUtils.drawImage(ResourceLocation("liquidbounce/icons/tenna/${(ceil(tennaFrame/2.0)).toInt()}.png"), scaledX, scaledY, 88, 146)
                tennaFrame++
            }
        }
    }
}