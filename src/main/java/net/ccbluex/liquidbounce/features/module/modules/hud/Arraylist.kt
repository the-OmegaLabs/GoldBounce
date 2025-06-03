package net.ccbluex.liquidbounce.features.module.modules.hud

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.settings.CustomTag
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Arraylist.Companion.inactiveStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

object Arraylist : Module("Arraylist", Category.HUD) {
    private val side by ListValue("Side", arrayOf("Right", "Left"), "Right")
    private val x by IntegerValue("X", 10, 0..1000)
    private val y by IntegerValue("Y", 10, 0..1000)

    // 文本颜色设置
    private val textR by FloatValue("Text-R", 1f, 0f..1f)
    private val textG by FloatValue("Text-G", 1f, 0f..1f)
    private val textB by FloatValue("Text-B", 1f, 0f..1f)
    private val textA by FloatValue("Text-Alpha", 1f, 0f..1f)
    private val textColor get() = Color(textR, textG, textB, textA)
    private val nameSpaced by BoolValue("SpacedName", false)
    private val displayTag by BoolValue("DisplayTag", true)

    // 发光效果设置
    private val glow by BoolValue("Glow", true)
    private val glowRadius by IntegerValue("Glow-Radius", 8, 0..20) { glow }
    private val glowR by FloatValue("Glow-R", 0.4f, 0f..1f) { glow }
    private val glowG by FloatValue("Glow-G", 0.8f, 0f..1f) { glow }
    private val glowB by FloatValue("Glow-B", 1f, 0f..1f) { glow }
    private val glowA by FloatValue("Glow-Alpha", 0.8f, 0f..1f) { glow }
    private val glowColor get() = Color(glowR, glowG, glowB, glowA)

    // 背景设置
    private val background by BoolValue("Background", true)
    private val backgroundR by FloatValue("Background-R", 0.1f, 0f..1f) { background }
    private val backgroundG by FloatValue("Background-G", 0.1f, 0f..1f) { background }
    private val backgroundB by FloatValue("Background-B", 0.1f, 0f..1f) { background }
    private val backgroundA by FloatValue("Background-Alpha", 0.8f, 0f..1f) { background }
    private val backgroundColor get() = Color(backgroundR, backgroundG, backgroundB, backgroundA)
    private val round by BoolValue("Round-Corners", true) { background }
    private val roundRadius by IntegerValue("Round-Radius", 4, 0..10) { round }

    private val font by FontValue("Font", Fonts.fontBold40)
    private val spacing by IntegerValue("Line-Spacing", 2, 0..10)

    // 新增渐变相关配置
    private val textColorMode by ListValue("TextColorMode", arrayOf("Custom", "Gradient"), "Custom")
    private val maxGradientColors by IntegerValue("GradientColors", 2, 2..4) { textColorMode == "Gradient" }
    private val gradientSpeed by FloatValue("GradientSpeed", 1f, 0.1f..5f) { textColorMode == "Gradient" }
    
    // 渐变颜色配置
    private val gradientColor1R by FloatValue("Gradient-R1", 1f, 0f..1f) { textColorMode == "Gradient" }
    private val gradientColor1G by FloatValue("Gradient-G1", 0f, 0f..1f) { textColorMode == "Gradient" }
    private val gradientColor1B by FloatValue("Gradient-B1", 0f, 0f..1f) { textColorMode == "Gradient" }
    
    private val gradientColor2R by FloatValue("Gradient-R2", 0f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 2 }
    private val gradientColor2G by FloatValue("Gradient-G2", 1f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 2 }
    private val gradientColor2B by FloatValue("Gradient-B2", 0f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 2 }
    
    private val gradientColor3R by FloatValue("Gradient-R3", 0f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 3 }
    private val gradientColor3G by FloatValue("Gradient-G3", 0f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 3 }
    private val gradientColor3B by FloatValue("Gradient-B3", 1f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 3 }
    
    private val gradientColor4R by FloatValue("Gradient-R4", 1f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 4 }
    private val gradientColor4G by FloatValue("Gradient-G4", 0f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 4 }
    private val gradientColor4B by FloatValue("Gradient-B4", 1f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 4 }

    private fun getDisplayString(module: Module): String {
        var displayName = if (module.name == "CustomTag") CustomTag.custom.get() else module.name
        if (nameSpaced) {
            displayName = if (module.name == "CustomTag") CustomTag.custom.get() else module.spacedName
        }
        if (displayTag) {
            return if (!module.tag.isNullOrEmpty()) "$displayName §7${module.tag}" else displayName
        }
        return displayName
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val modules = LiquidBounce.moduleManager.modules
            .filter { it.inArray && (it.state && inactiveStyle != "Hide") }
            .sortedByDescending { font.getStringWidth(getDisplayString(it)) }

        val sr = ScaledResolution(mc)
        val screenW = sr.scaledWidth
        val initialY = y.toFloat()
        
        // 计算渐变颜色数组
        val gradientColors = when(maxGradientColors) {
            2 -> arrayOf(
                Color(gradientColor1R, gradientColor1G, gradientColor1B),
                Color(gradientColor2R, gradientColor2G, gradientColor2B)
            )
            3 -> arrayOf(
                Color(gradientColor1R, gradientColor1G, gradientColor1B),
                Color(gradientColor2R, gradientColor2G, gradientColor2B),
                Color(gradientColor3R, gradientColor3G, gradientColor3B)
            )
            else -> arrayOf(
                Color(gradientColor1R, gradientColor1G, gradientColor1B),
                Color(gradientColor2R, gradientColor2G, gradientColor2B),
                Color(gradientColor3R, gradientColor3G, gradientColor3B),
                Color(gradientColor4R, gradientColor4G, gradientColor4B)
            )
        }

        // 计算渐变偏移
        val totalHeight = modules.sumOf { font.FONT_HEIGHT + spacing } - spacing
        val gradientOffset = (System.currentTimeMillis() % (10000 / gradientSpeed)) / (10000 / gradientSpeed)

        var yPos = initialY
        for ((index, module) in modules.withIndex()) {
            val text = getDisplayString(module)
            val textWidth = font.getStringWidth(text).toFloat()
            val textHeight = font.FONT_HEIGHT.toFloat()

            // 计算当前颜色
            val progress = (yPos - initialY + gradientOffset * totalHeight) % totalHeight / totalHeight
            val color = when {
                textColorMode == "Gradient" -> ColorUtils.interpolateColors(gradientColors, progress)
                else -> textColor
            }

            // 矩形宽度根据文本宽度
            val rectWidth = textWidth + 4f
            val rectHeight = textHeight + 2f
            val rectX = if (side == "Right") screenW - rectWidth - x else x.toFloat()
            val rectY = yPos

            if (background) {
                RenderUtils.drawRoundedRect(
                    rectX, rectY,
                    rectX + rectWidth, rectY + rectHeight,
                    backgroundColor.rgb,
                    if (round) roundRadius.toFloat() else 0f
                )
            }

            if (glow) {
                GlowUtils.drawGlow(
                    rectX, rectY,
                    rectWidth, rectHeight,
                    glowRadius, glowColor
                )
            }

            // 绘制文本，带内边距2px及1px垂直偏移
            val textX = if (side == "Right") screenW - textWidth - x + 2f else x.toFloat() + 2f
            val textY = yPos + 1f
            font.drawString(text, textX, textY, color.rgb, false)

            yPos += textHeight + spacing
        }
    }
}
