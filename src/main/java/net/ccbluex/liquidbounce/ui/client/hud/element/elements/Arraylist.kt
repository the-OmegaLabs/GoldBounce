/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.misc.GameDetector
import net.ccbluex.liquidbounce.features.module.modules.settings.CustomTag
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Horizontal
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Vertical
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.ColorSettingsFloat
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientFontShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowFontShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.ccbluex.liquidbounce.utils.render.toColorArray
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager.resetColor
import java.awt.Color

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(
    x: Double = 1.0, y: Double = 2.0, scale: Float = 1F,
    side: Side = Side(Horizontal.RIGHT, Vertical.UP),
) : Element(x, y, scale, side) {

    private val textColorMode by choices("Text-Color", arrayOf("Custom", "Random", "Rainbow", "Gradient"), "Custom")
    private val textColors = ColorSettingsInteger(this, "Text", withAlpha = false)
    { textColorMode == "Custom" }.with(255, 255, 0)

    private val gradientTextSpeed by floatValue("Text-Gradient-Speed", 1f, 0.5f..10f)
    { textColorMode == "Gradient" }

    private val maxTextGradientColors by intValue("Max-Text-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { textColorMode == "Gradient" }
    private val textGradColors = ColorSettingsFloat.create(this, "Text-Gradient")
    { textColorMode == "Gradient" && it <= maxTextGradientColors }

    private val rectMode by choices("Rect", arrayOf("None", "Left", "Right", "Outline"), "None")
    private val roundedRectRadius by floatValue("RoundedRect-Radius", 0F, 0F..2F)
    { rectMode !in setOf("None", "Outline") }
    private val rectColorMode by choices("Rect-Color", arrayOf("Custom", "Random", "Rainbow", "Gradient"), "Rainbow")
    { rectMode != "None" }
    private val rectColors = ColorSettingsInteger(this, "Rect", zeroAlphaCheck = true, applyMax = true)
    { isCustomRectSupported }

    private val gradientRectSpeed by floatValue("Rect-Gradient-Speed", 1f, 0.5f..10f)
    { isCustomRectGradientSupported }

    private val maxRectGradientColors by intValue("Max-Rect-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { isCustomRectGradientSupported }
    private val rectGradColors = ColorSettingsFloat.create(this, "Rect-Gradient")
    { isCustomRectGradientSupported && it <= maxRectGradientColors }

    private val roundedBackgroundRadius by floatValue("RoundedBackGround-Radius", 0F, 0F..5F)
    { bgColors.color().alpha > 0 }

    private val backgroundMode by choices(
        "Background-Color", arrayOf("Custom", "Random", "Rainbow", "Gradient"),
        "Custom"
    )
    private val bgColors = ColorSettingsInteger(this, "Background", zeroAlphaCheck = true)
    { backgroundMode == "Custom" }.with(a = 0)

    private val gradientBackgroundSpeed by floatValue("Background-Gradient-Speed", 1f, 0.5f..10f)
    { backgroundMode == "Gradient" }

    private val maxBackgroundGradientColors by intValue("Max-Background-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { backgroundMode == "Gradient" }
    private val bgGradColors = ColorSettingsFloat.create(this, "Background-Gradient")
    { backgroundMode == "Gradient" && it <= maxBackgroundGradientColors }

    private fun isColorModeUsed(value: String) = textColorMode == value || rectMode == value || backgroundMode == value

    private val saturation by floatValue("Random-Saturation", 0.9f, 0f..1f) { isColorModeUsed("Random") }
    private val brightness by floatValue("Random-Brightness", 1f, 0f..1f) { isColorModeUsed("Random") }
    private val rainbowX by floatValue("Rainbow-X", -1000F, -2000F..2000F) { isColorModeUsed("Rainbow") }
    private val rainbowY by floatValue("Rainbow-Y", -1000F, -2000F..2000F) { isColorModeUsed("Rainbow") }
    private val gradientX by floatValue("Gradient-X", -1000F, -2000F..2000F) { isColorModeUsed("Gradient") }
    private val gradientY by floatValue("Gradient-Y", -1000F, -2000F..2000F) { isColorModeUsed("Gradient") }
    private val shadow by _boolean("Shadow", true)
    private val shadowRadius by intValue("ShadowRadius", 10, 0..20)
    private val tags by _boolean("Tags", true)
    private val tagsStyle by object : ListValue("TagsStyle", arrayOf("[]", "()", "<>", "-", "|", "Space"), "Space") {
        override fun isSupported() = tags

        // onUpdate - updates tag onInit and onChanged
        override fun onUpdate(value: String) = updateTagDetails()
    }
    private val tagsCase by choices("TagsCase", arrayOf("Normal", "Uppercase", "Lowercase"), "Normal") { tags }
    private val tagsArrayColor by object : BoolValue("TagsArrayColor", false) {
        override fun isSupported() = tags
        override fun onUpdate(value: Boolean) = updateTagDetails()
    }

    private val font by font("Font", Fonts.font40)
    private val textShadow by _boolean("ShadowText", true)
    private val moduleCase by choices("ModuleCase", arrayOf("Normal", "Uppercase", "Lowercase"), "Normal")
    private val space by floatValue("Space", 0F, 0F..5F)
    private val textHeight by floatValue("TextHeight", 11F, 1F..20F)
    private val textY by floatValue("TextY", 1.5F, 0F..20F)

    private val animation by choices("Animation", arrayOf("Slide", "Smooth"), "Smooth") { tags }
    private val animationSpeed by floatValue("AnimationSpeed", 0.2F, 0.01F..1F) { animation == "Smooth" }

    companion object {
        val spacedModules by _boolean("SpacedModules", false)
        val inactiveStyle by choices("InactiveModulesStyle", arrayOf("Normal", "Color", "Hide"), "Color")
        { GameDetector.state }
    }

    private var x2 = 0
    private var y2 = 0F

    private lateinit var tagPrefix: String

    private lateinit var tagSuffix: String

    private var modules = emptyList<Module>()

    private val inactiveColor = Color(255, 255, 255, 100).rgb

    private val isCustomRectSupported
        get() = rectMode != "None" && rectColorMode == "Custom"

    private val isCustomRectGradientSupported
        get() = rectMode != "None" && rectColorMode == "Gradient"

    init {
        updateTagDetails()
    }

    fun updateTagDetails() {
        val pair: Pair<String, String> = when (tagsStyle) {
            "[]", "()", "<>" -> tagsStyle[0].toString() to tagsStyle[1].toString()
            "-", "|" -> tagsStyle[0] + " " to ""
            else -> "" to ""
        }

        tagPrefix = (if (tagsArrayColor) " " else " §7") + pair.first
        tagSuffix = pair.second
    }

    private fun getDisplayString(module: Module): String {
        val moduleName = when (moduleCase) {
            "Uppercase" -> module.getName().uppercase()
            "Lowercase" -> module.getName().lowercase()
            else -> module.getName()
        }

        var tag = module.tag ?: ""

        tag = when (tagsCase) {
            "Uppercase" -> tag.uppercase()
            "Lowercase" -> tag.lowercase()
            else -> tag
        }

        val moduleTag = if (tags && !module.tag.isNullOrEmpty()) tagPrefix + tag + tagSuffix else ""

        return moduleName + moduleTag
    }

    override fun drawElement(): Border? {
        AWTFontRenderer.assumeNonVolatile = true

        // Slide animation - update every render
        val delta = deltaTime

        for (module in moduleManager.modules) {
            val shouldShow = (module.inArray && module.state && (inactiveStyle != "Hide" || module.isActive))
            if (!shouldShow && module.slide <= 0f)
                continue
            var displayString : String
            if (module.name == "CustomTag"){
                displayString = CustomTag.custom.get()
            }else{
                displayString = getDisplayString(module)
            }
            val width = font.getStringWidth(displayString)

            when (animation) {
                "Slide" -> {
                    // If modules become inactive because they only work when in game, animate them as if they got disabled
                    module.slideStep += if (shouldShow) delta / 4F else -delta / 4F
                    if (shouldShow) {
                        if (module.slide < width) {
                            module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                        }
                    } else {
                        module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                    }

                    module.slide = module.slide.coerceIn(0F, width.toFloat())
                    module.slideStep = module.slideStep.coerceIn(0F, width.toFloat())
                }

                "Smooth" -> {
                    val target = if (shouldShow) width.toDouble() else -width / 5.0
                    module.slide =
                        AnimationUtil.base(module.slide.toDouble(), target, animationSpeed.toDouble()).toFloat()
                }
            }
        }
        // Draw arraylist
        val textCustomColor = textColors.color(1).rgb
        val rectCustomColor = rectColors.color().rgb
        val backgroundCustomColor = bgColors.color().rgb
        val textSpacer = textHeight + space

        val rainbowOffset = System.currentTimeMillis() % 10000 / 10000F
        val rainbowX = if (rainbowX == 0f) 0f else 1f / rainbowX
        val rainbowY = if (rainbowY == 0f) 0f else 1f / rainbowY

        val gradientOffset = System.currentTimeMillis() % 10000 / 10000F
        val gradientX = if (gradientX == 0f) 0f else 1f / gradientX
        val gradientY = if (gradientY == 0f) 0f else 1f / gradientY

        modules.forEachIndexed { index, module ->
            var yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                    if (side.vertical == Vertical.DOWN) index + 1 else index
            if (animation == "Smooth") {
                module.yAnim = AnimationUtil.base(module.yAnim.toDouble(), yPos.toDouble(), 0.2).toFloat()
                yPos = module.yAnim
            }
            val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

            val markAsInactive = inactiveStyle == "Color" && !module.isActive

            var displayString: String
            if (module.name == "CustomTag"){
                displayString = CustomTag.custom.get()
            }else{
                displayString = getDisplayString(module)
            }
            val displayStringWidth = font.getStringWidth(displayString)

            val previousDisplayString = getDisplayString(modules[(if (index > 0) index else 1) - 1])
            val previousDisplayStringWidth = font.getStringWidth(previousDisplayString)

            when (side.horizontal) {
                Horizontal.RIGHT, Horizontal.MIDDLE -> {
                    val xPos = -module.slide - 2

                    GradientShader.begin(
                        !markAsInactive && backgroundMode == "Gradient",
                        gradientX,
                        gradientY,
                        bgGradColors.toColorArray(maxBackgroundGradientColors),
                        gradientBackgroundSpeed,
                        gradientOffset
                    ).use {
                        RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                            drawRoundedRect(
                                xPos - if (rectMode == "Right") 5 else 2,
                                yPos,
                                if (rectMode == "Right") -3F else 0F,
                                yPos + textSpacer,
                                when (backgroundMode) {
                                    "Gradient" -> 0
                                    "Rainbow" -> 0
                                    "Random" -> moduleColor
                                    else -> backgroundCustomColor
                                },
                                roundedBackgroundRadius
                            )
                            if (shadow){
                                GlowUtils.drawGlow(
                                    xPos - if (rectMode == "Right") 5 else 2,
                                    yPos,
                                    if (rectMode == "Right") -3F else 0F,
                                    yPos + textSpacer,
                                    shadowRadius,
                                    Color.BLACK
                                )
                            }
                        }
                    }

                    GradientFontShader.begin(
                        !markAsInactive && textColorMode == "Gradient",
                        gradientX,
                        gradientY,
                        textGradColors.toColorArray(maxTextGradientColors),
                        gradientTextSpeed,
                        gradientOffset
                    ).use {
                        RainbowFontShader.begin(
                            !markAsInactive && textColorMode == "Rainbow",
                            rainbowX,
                            rainbowY,
                            rainbowOffset
                        ).use {
                            font.drawString(
                                displayString, xPos - if (rectMode == "Right") 3 else 0, yPos + textY,
                                if (markAsInactive) inactiveColor
                                else when (textColorMode) {
                                    "Gradient" -> 0
                                    "Rainbow" -> 0
                                    "Random" -> moduleColor
                                    else -> textCustomColor
                                },
                                textShadow
                            )
                        }
                    }

                    GradientShader.begin(
                        !markAsInactive && isCustomRectGradientSupported,
                        gradientX,
                        gradientY,
                        rectGradColors.toColorArray(maxRectGradientColors),
                        gradientRectSpeed,
                        gradientOffset
                    ).use {
                        if (rectMode != "None") {
                            RainbowShader.begin(
                                !markAsInactive && rectColorMode == "Rainbow",
                                rainbowX,
                                rainbowY,
                                rainbowOffset
                            ).use {
                                val rectColor =
                                    if (markAsInactive) inactiveColor
                                    else when (rectColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        else -> rectCustomColor
                                    }

                                when (rectMode) {
                                    "Left" -> drawRoundedRect(
                                        xPos - 5,
                                        yPos,
                                        xPos - 2,
                                        yPos + textSpacer,
                                        rectColor,
                                        roundedRectRadius
                                    )

                                    "Right" -> drawRoundedRect(
                                        -3F,
                                        yPos,
                                        0F,
                                        yPos + textSpacer,
                                        rectColor,
                                        roundedRectRadius
                                    )

                                    "Outline" -> {
                                        drawRect(-1F, yPos - 1F, 0F, yPos + textSpacer, rectColor)
                                        drawRect(xPos - 3, yPos, xPos - 2, yPos + textSpacer, rectColor)

                                        if (module == modules.first()) {
                                            drawRect(xPos - 3, yPos - 1F, 0F, yPos, rectColor)
                                        }

                                        drawRect(
                                            xPos - 3 - (previousDisplayStringWidth - displayStringWidth),
                                            yPos,
                                            xPos - 2,
                                            yPos + 1,
                                            rectColor
                                        )

                                        if (module == modules.last()) {
                                            drawRect(xPos - 3, yPos + textSpacer, 0F, yPos + textSpacer + 1, rectColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Horizontal.LEFT -> {
                    val width = font.getStringWidth(displayString)
                    val xPos = -(width - module.slide) + if (rectMode == "Left") 5 else 2

                    GradientShader.begin(
                        !markAsInactive && backgroundMode == "Gradient",
                        gradientX,
                        gradientY,
                        bgGradColors.toColorArray(maxBackgroundGradientColors),
                        gradientBackgroundSpeed,
                        gradientOffset
                    ).use {
                        RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                            drawRoundedRect(
                                0F, yPos, xPos + width + if (rectMode == "Right") 5 else 2, yPos + textSpacer,
                                when (backgroundMode) {
                                    "Gradient" -> 0
                                    "Rainbow" -> 0
                                    "Random" -> moduleColor
                                    else -> backgroundCustomColor
                                },
                                roundedBackgroundRadius
                            )
                            if (shadow){
                                GlowUtils.drawGlow(
                                    xPos.toFloat(),
                                    yPos.toFloat(),
                                    (xPos + width + if (rectMode == "Right") 5 else 2).toFloat(),
                                    (yPos + textSpacer).toFloat(),
                                    shadowRadius,
                                    Color.BLACK
                                )
                            }
                        }
                    }

                    GradientFontShader.begin(
                        !markAsInactive && textColorMode == "Gradient",
                        gradientX,
                        gradientY,
                        textGradColors.toColorArray(maxTextGradientColors),
                        gradientTextSpeed,
                        gradientOffset
                    ).use {
                        RainbowFontShader.begin(
                            !markAsInactive && textColorMode == "Rainbow",
                            rainbowX,
                            rainbowY,
                            rainbowOffset
                        ).use {
                            font.drawString(
                                displayString, xPos, yPos + textY,
                                if (markAsInactive) inactiveColor
                                else when (textColorMode) {
                                    "Gradient" -> 0
                                    "Rainbow" -> 0
                                    "Random" -> moduleColor
                                    else -> textCustomColor
                                },
                                textShadow
                            )
                        }
                    }

                    GradientShader.begin(
                        !markAsInactive && isCustomRectGradientSupported,
                        gradientX,
                        gradientY,
                        rectGradColors.toColorArray(maxRectGradientColors),
                        gradientRectSpeed,
                        gradientOffset
                    ).use {
                        RainbowShader.begin(
                            !markAsInactive && rectColorMode == "Rainbow",
                            rainbowX,
                            rainbowY,
                            rainbowOffset
                        ).use {
                            if (rectMode != "None") {
                                val rectColor =
                                    if (markAsInactive) inactiveColor
                                    else when (rectColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        else -> rectCustomColor
                                    }

                                when (rectMode) {
                                    "Left" -> drawRoundedRect(
                                        0F,
                                        yPos - 1,
                                        3F,
                                        yPos + textSpacer,
                                        rectColor,
                                        roundedRectRadius
                                    )

                                    "Right" -> drawRoundedRect(
                                        xPos + width + 2,
                                        yPos,
                                        xPos + width + 2 + 3,
                                        yPos + textSpacer,
                                        rectColor,
                                        roundedRectRadius
                                    )

                                    "Outline" -> {
                                        drawRect(-1F, yPos - 1F, 0F, yPos + textSpacer, rectColor)
                                        drawRect(
                                            xPos + width + 2,
                                            yPos - 1F,
                                            xPos + width + 3,
                                            yPos + textSpacer,
                                            rectColor
                                        )

                                        if (module == modules.first()) {
                                            drawRect(xPos + width + 2, yPos - 1, xPos + width + 3, yPos, rectColor)
                                            drawRect(-1F, yPos - 1, xPos + width + 2, yPos, rectColor)
                                        }

                                        drawRect(
                                            xPos + width + 2,
                                            yPos - 1,
                                            xPos + width + 3 + (previousDisplayStringWidth - displayStringWidth),
                                            yPos,
                                            rectColor
                                        )

                                        if (module == modules.last()) {
                                            drawRect(
                                                xPos + width + 2,
                                                yPos + textSpacer,
                                                xPos + width + 3,
                                                yPos + textSpacer + 1,
                                                rectColor
                                            )
                                            drawRect(
                                                -1F,
                                                yPos + textSpacer,
                                                xPos + width + 2,
                                                yPos + textSpacer + 1,
                                                rectColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Draw border
        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT)
                    Border(0F, -1F, 20F, 20F)
                else
                    Border(0F, -1F, -20F, 20F)
            }

            for (module in modules) {
                when (side.horizontal) {
                    Horizontal.RIGHT, Horizontal.MIDDLE -> {
                        val xPos = -module.slide.toInt() - 2
                        if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                    }

                    Horizontal.LEFT -> {
                        val xPos = module.slide.toInt() + 14
                        if (x2 == Int.MIN_VALUE || xPos > x2) x2 = xPos
                    }
                }
            }

            y2 = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size

            return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Vertical.DOWN) 1F else 0F)
        }

        AWTFontRenderer.assumeNonVolatile = false
        resetColor()
        return null
    }

    override fun updateElement() {
        modules = moduleManager.modules
            .filter { it.inArray && it.slide > 0 && !it.hideModuleValues.get() }
            .sortedWith(compareByDescending<Module> { it.name == "CustomTag" }
                .thenByDescending { font.getStringWidth(getDisplayString(it)) })
    }

}
