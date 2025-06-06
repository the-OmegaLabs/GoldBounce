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
    // 默认位置优化
    private val side by ListValue("Side", arrayOf("Right", "Left"), "Right")
    private val x by IntegerValue("X", 5, 0..1000)  // 更靠近边缘
    private val y by IntegerValue("Y", 25, 0..1000)  // 从顶部向下偏移25px

    // 文本颜色设置 - 使用更协调的默认值
    private val textR by FloatValue("Text-R", 1f, 0f..1f)
    private val textG by FloatValue("Text-G", 1f, 0f..1f)
    private val textB by FloatValue("Text-B", 1f, 0f..1f)
    private val textA by FloatValue("Text-Alpha", 1f, 0f..1f)
    private val textColor get() = Color(textR, textG, textB, textA)
    private val nameSpaced by BoolValue("SpacedName", true)  // 默认启用空格分隔
    private val displayTag by BoolValue("DisplayTag", true)

    // 发光效果设置 - 使用更柔和的默认值
    private val glow by BoolValue("Glow", true)
    private val glowRadius by IntegerValue("Glow-Radius", 6, 0..20) { glow }  // 更小的半径
    private val glowR by FloatValue("Glow-R", 0.3f, 0f..1f) { glow }
    private val glowG by FloatValue("Glow-G", 0.6f, 0f..1f) { glow }
    private val glowB by FloatValue("Glow-B", 1f, 0f..1f) { glow }
    private val glowA by FloatValue("Glow-Alpha", 0.6f, 0f..1f) { glow }  // 更透明的发光
    private val glowColor get() = Color(glowR, glowG, glowB, glowA)

    // 背景设置 - 使用更深的背景
    private val background by BoolValue("Background", true)
    private val backgroundR by FloatValue("Background-R", 0.05f, 0f..1f) { background }
    private val backgroundG by FloatValue("Background-G", 0.05f, 0f..1f) { background }
    private val backgroundB by FloatValue("Background-B", 0.05f, 0f..1f) { background }
    private val backgroundA by FloatValue("Background-Alpha", 0.8f, 0f..1f) { background }
    private val backgroundColor get() = Color(backgroundR, backgroundG, backgroundB, backgroundA)
    private val round by BoolValue("Round-Corners", true) { background }
    private val roundRadius by IntegerValue("Round-Radius", 4, 0..10) { round }

    // 使用更小尺寸的默认字体
    private val font by FontValue("Font", Fonts.font35)
    private val spacing by IntegerValue("Line-Spacing", 3, 0..10)  // 增加行间距

    // 渐变设置优化
    private val textColorMode by ListValue("TextColorMode", arrayOf("Custom", "Gradient"), "Gradient")
    private val maxGradientColors by IntegerValue("GradientColors", 4, 2..4) { textColorMode == "Gradient" }
    private val gradientSpeed by FloatValue("GradientSpeed", 0.5f, 0.1f..5f) { textColorMode == "Gradient" }  // 更慢的渐变速度

    // 使用协调的彩虹色渐变
    private val gradientColor1R by FloatValue("Gradient-R1", 1f, 0f..1f) { textColorMode == "Gradient" }
    private val gradientColor1G by FloatValue("Gradient-G1", 0f, 0f..1f) { textColorMode == "Gradient" }
    private val gradientColor1B by FloatValue("Gradient-B1", 0f, 0f..1f) { textColorMode == "Gradient" }

    private val gradientColor2R by FloatValue("Gradient-R2", 1f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 2 }
    private val gradientColor2G by FloatValue("Gradient-G2", 1f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 2 }
    private val gradientColor2B by FloatValue("Gradient-B2", 0f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 2 }

    private val gradientColor3R by FloatValue("Gradient-R3", 0f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 3 }
    private val gradientColor3G by FloatValue("Gradient-G3", 1f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 3 }
    private val gradientColor3B by FloatValue("Gradient-B3", 0f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 3 }

    private val gradientColor4R by FloatValue("Gradient-R4", 0.5f, 0f..1f) { textColorMode == "Gradient" && maxGradientColors >= 4 }
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
        val screenH = sr.scaledHeight
        val initialY = y.toFloat()

        // 计算渐变颜色数组 - 使用更协调的默认值
        val gradientColors = when(maxGradientColors) {
            2 -> arrayOf(
                Color(0.8f, 0.2f, 0.2f),  // 红色
                Color(0.2f, 0.8f, 0.2f)   // 绿色
            )
            3 -> arrayOf(
                Color(0.8f, 0.2f, 0.2f),  // 红色
                Color(0.8f, 0.8f, 0.2f),  // 黄色
                Color(0.2f, 0.8f, 0.2f)   // 绿色
            )
            else -> arrayOf(
                Color(0.8f, 0.2f, 0.2f),  // 红色
                Color(0.8f, 0.8f, 0.2f),  // 黄色
                Color(0.2f, 0.8f, 0.2f),  // 绿色
                Color(0.2f, 0.2f, 0.8f)   // 蓝色
            )
        }

        // 计算渐变偏移 - 使用基于时间的平滑过渡
        val timeOffset = (System.currentTimeMillis() * gradientSpeed / 1000f) % 1f

        // 计算列表总高度（用于平滑渐变）
        val totalHeight = modules.sumOf { font.FONT_HEIGHT + spacing } - spacing

        var yPos = initialY
        for ((index, module) in modules.withIndex()) {
            val text = getDisplayString(module)
            val textWidth = font.getStringWidth(text).toFloat()
            val textHeight = font.FONT_HEIGHT.toFloat()

            // 计算当前模块在列表中的位置（0-1）
            val positionInList = (yPos - initialY) / totalHeight
            // 计算渐变进度 - 使用平滑的sin函数
            val gradientProgress = (Math.sin(2 * Math.PI * (positionInList + timeOffset)) * 0.5 + 0.5)

            // 计算当前颜色 - 确保颜色过渡平滑
            val color = when {
                textColorMode == "Gradient" -> ColorUtils.interpolateColors(gradientColors, gradientProgress.toFloat())
                else -> textColor
            }

            // 矩形宽度根据文本宽度，增加更多内边距
            val rectWidth = textWidth + 8f  // 增加左右内边距
            val rectHeight = textHeight + 4f // 增加上下内边距
            val rectX = if (side == "Right") screenW - rectWidth - x else x.toFloat()
            val rectY = yPos - 1f  // 微调垂直位置

            // 先绘制发光效果（在背景下方）
            if (glow) {
                GlowUtils.drawGlow(
                    rectX, rectY,
                    rectWidth, rectHeight,
                    glowRadius, glowColor
                )
            }

            // 然后绘制背景
            if (background) {
                RenderUtils.drawRoundedRect(
                    rectX, rectY,
                    rectX + rectWidth, rectY + rectHeight,
                    backgroundColor.rgb,
                    if (round) roundRadius.toFloat() else 0f
                )
            }

            // 绘制文本 - 使用垂直居中
            val textX = if (side == "Right") screenW - textWidth - x - 4f else x.toFloat() + 4f
            val textY = rectY + (rectHeight - textHeight) / 2f  // 垂直居中

            font.drawString(text, textX, textY, color.rgb, false)

            yPos += textHeight + spacing
        }
    }
}