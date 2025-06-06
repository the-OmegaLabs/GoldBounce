package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.CPSCounter
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

@ElementInfo(name = "CPSCounter")
class CPSHudCounter : Element() {
    private val rightValue = BoolValue("Right Click", false)
    override fun drawElement(): Border {
        val font = Fonts.font35
        val string: String = "CPS ${CPSCounter.getCPS(CPSCounter.MouseButton.LEFT)}" + if (rightValue.get()) " | ${
            CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT)
        }" else ""
        RenderUtils.drawRoundedRect(
            0F,
            0F,
            10F + font.getStringWidth(string),
            10F + font.FONT_HEIGHT,
            Color(0, 0, 0, 120).rgb,
            5F
        )
        GlowUtils.drawGlow(0F, 0F, 10F + font.getStringWidth(string), 10F + font.FONT_HEIGHT, 8, Color(0, 0, 0, 120))
        font.drawString(string, 5F, 6F, Color(255, 255, 255).rgb, true)
        return Border(0F, 0F, 10F + font.getStringWidth(string), 10F + font.FONT_HEIGHT)
    }
}