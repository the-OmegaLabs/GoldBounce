/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.skid.moonlight.render.ColorUtils
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.font
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import java.awt.Color

/**
 * CustomHUD effects element
 *
 * Shows a list of active potion effects
 */
@ElementInfo(name = "Effects")
class Effects(
    x: Double = 2.0, y: Double = 10.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    private val font by font("Font", Fonts.font35)
    private val shadow by boolean("Shadow", true)
    fun formatDuration(duration: Int): String {
        val seconds = duration / 20
        return when {
            seconds >= 3600 -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
            seconds >= 60 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    /**
     * Draw element
     */
    override fun drawElement(): Border {
        var y = 0F
        var maxWidth = 0F

        assumeNonVolatile = true

        // 绘制半透明背景
        RenderUtils.drawRoundedRect(
            -110F, -2F,
            0F, mc.thePlayer.activePotionEffects.size * 18F + 4F,
            5,
            Color(30, 30, 30, 180).rgb.toFloat()
        )

        for ((index, effect) in mc.thePlayer.activePotionEffects.withIndex()) {
            val potion = Potion.potionTypes.getOrNull(effect.potionID) ?: continue

            // 等级计算
            val level = effect.amplifier + 1
            val levelText = if (level > 10) "10+" else level.toString()

            // 时间格式化
            val durationText = formatDuration(effect.duration)

            // 构建显示文本
            val displayText = "${I18n.format(potion.name)} §8Lv.$levelText §7| §f$durationText"

            // 动态计算宽度
            val textWidth = font.getStringWidth(displayText).toFloat()
            if (textWidth > maxWidth) maxWidth = textWidth

            // 渐变色绘制
            font.drawString(
                displayText,
                -textWidth - 4F,
                y + index * 18F,
                ColorUtils.interpolateColor(
                    if (potion.isBadEffect) Color(220, 50, 50).rgb else Color(100, 200, 100).rgb,
                    Color.WHITE.rgb,
                    0.3f
                ),
                shadow
            )

            // 绘制等级标识
            RenderUtils.drawRoundedRect(
                -textWidth - 24F,
                y + index * 18F + 3F,
                -textWidth - 16F,
                y + index * 18F + 11F,
                3,
                if (potion.isBadEffect) Color(180, 40, 40).rgb.toFloat() else Color(80, 180, 80).rgb.toFloat()
            )
        }

        assumeNonVolatile = false

        return Border(
            -110F,       // x1 = left
            -2F,         // y1 = top
            -maxWidth - 28F,  // x2 = right
            mc.thePlayer.activePotionEffects.size * 18F - 2F  // y2 = bottom
        )

    }

}