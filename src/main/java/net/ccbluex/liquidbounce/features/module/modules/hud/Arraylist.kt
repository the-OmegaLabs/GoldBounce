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
import net.ccbluex.liquidbounce.utils.render.ColorUtils.interpolateColorsCyclic
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

object Arraylist : Module("Arraylist", Category.HUD) {
    private val side by ListValue("Side", arrayOf("Right", "Left"), "Right")
    private val x by IntegerValue("X", 5, 0..1000)
    private val y by IntegerValue("Y", 25, 0..1000)

    private val textR by FloatValue("Text-R", 1f, 0f..1f)
    private val textG by FloatValue("Text-G", 1f, 0f..1f)
    private val textB by FloatValue("Text-B", 1f, 0f..1f)
    private val textA by FloatValue("Text-Alpha", 1f, 0f..1f)
    private val textColor get() = Color(textR, textG, textB, textA)
    private val nameSpaced by BoolValue("SpacedName", true)
    private val displayTag by BoolValue("DisplayTag", true)

    private val background by BoolValue("Background", true)
    private val backgroundR by FloatValue("Background-R", 0.05f, 0f..1f) { background }
    private val backgroundG by FloatValue("Background-G", 0.05f, 0f..1f) { background }
    private val backgroundB by FloatValue("Background-B", 0.05f, 0f..1f) { background }
    private val backgroundA by FloatValue("Background-Alpha", 0.8f, 0f..1f) { background }
    private val backgroundColor get() = Color(backgroundR, backgroundG, backgroundB, backgroundA)
    private val round by BoolValue("Round-Corners", true) { background }
    private val roundRadius by IntegerValue("Round-Radius", 4, 0..10) { round }

    private val customBgSize by BoolValue("CustomBackgroundSize", false) { background }
    private val bgWidth by IntegerValue("BackgroundWidth", 100, 20..500) { customBgSize && background }
    private val bgHeight by IntegerValue("BackgroundHeight", 15, 10..50) { customBgSize && background }
    private val bgPaddingH by IntegerValue("BackgroundHPadding", 4, 0..20) { background && !customBgSize }
    private val bgPaddingV by IntegerValue("BackgroundVPadding", 2, 0..10) { background && !customBgSize }

    private val glow by BoolValue("Glow", true)
    private val glowRadius by IntegerValue("Glow-Radius", 6, 0..20) { glow }
    private val glowR by FloatValue("Glow-R", 0.3f, 0f..1f) { glow }
    private val glowG by FloatValue("Glow-G", 0.6f, 0f..1f) { glow }
    private val glowB by FloatValue("Glow-B", 1f, 0f..1f) { glow }
    private val glowA by FloatValue("Glow-Alpha", 0.6f, 0f..1f) { glow }
    private val glowColor get() = Color(glowR, glowG, glowB, glowA)

    private val font by FontValue("Font", Fonts.font35)
    private val spacing by IntegerValue("Line-Spacing", 3, 0..10)

    private val textColorMode by ListValue("TextColorMode", arrayOf("Custom", "Gradient"), "Gradient")
    private val maxGradientColors by IntegerValue("GradientColors", 4, 2..4) { textColorMode == "Gradient" }
    private val gradientSpeed by FloatValue("GradientSpeed", 0.5f, 0.1f..5f) { textColorMode == "Gradient" }

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

    private val gradientColors: Array<Color>
        get() = when (maxGradientColors) {
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

        val colors = gradientColors
        val timeOffset = (System.currentTimeMillis() % 10000) / 10000f * gradientSpeed

        var totalHeight = 0f
        modules.forEach { module ->
            val text = getDisplayString(module)
            val textWidth = font.getStringWidth(text).toFloat()
            val textHeight = font.FONT_HEIGHT.toFloat()

            val (rectWidth, rectHeight) = if (background && customBgSize) {
                Pair(bgWidth.toFloat(), bgHeight.toFloat())
            } else {
                val width = if (customBgSize) bgWidth.toFloat() else textWidth + bgPaddingH * 2f
                val height = if (customBgSize) bgHeight.toFloat() else textHeight + bgPaddingV * 2f
                Pair(width, height)
            }

            totalHeight += rectHeight + if (totalHeight > 0) spacing.toFloat() else 0f
        }

        var yPos = initialY
        modules.forEach { module ->
            val text = getDisplayString(module)
            val textWidth = font.getStringWidth(text).toFloat()
            val textHeight = font.FONT_HEIGHT.toFloat()

            val (rectWidth, rectHeight) = if (background && customBgSize) {
                Pair(bgWidth.toFloat(), bgHeight.toFloat())
            } else {
                val width = if (background) textWidth + bgPaddingH * 2f else textWidth
                val height = if (background) textHeight + bgPaddingV * 2f else textHeight
                Pair(width, height)
            }

            val rectX = if (side == "Right") screenW - rectWidth - x else x.toFloat()
            val rectY = yPos

            val positionInList = (yPos - initialY) / totalHeight
            val gradientProgress = (positionInList + timeOffset) % 1f
            val color = when {
                textColorMode == "Gradient" -> interpolateColorsCyclic(colors, gradientProgress)
                else -> textColor
            }

            if (glow) {
                GlowUtils.drawGlow(
                    rectX, rectY,
                    rectWidth, rectHeight,
                    glowRadius, glowColor
                )
            }

            if (background) {
                RenderUtils.drawRoundedRect(
                    rectX, rectY,
                    rectX + rectWidth, rectY + rectHeight,
                    backgroundColor.rgb,
                    if (round) roundRadius.toFloat() else 0f
                )
            }

            val textX = if (side == "Right") {
                rectX + rectWidth - textWidth - (if (background) bgPaddingH.toFloat() else 0f)
            } else {
                rectX + (if (background) bgPaddingH.toFloat() else 0f)
            }

            val textY = rectY + (rectHeight - textHeight) / 2f

            font.drawString(text, textX, textY, color.rgb, false)

            yPos += rectHeight + spacing
        }
    }
}