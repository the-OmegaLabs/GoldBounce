/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.utils.skid.slack.RenderUtil
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.EntityUtils.getHealth
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawScaledCustomSizeModalRect
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.debugFPS
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * A Target HUD
 */
@ElementInfo(name = "Target")
class Target : Element() {

    private var width = 0f
    private var height = 0f

    private val isRendered: Boolean
        get() = width > 0f || height > 0f

    private var alphaText = 0
    private var alphaBackground = 0
    private var alphaBorder = 0

    private val isAlpha: Boolean
        get() = alphaBorder > 0 || alphaBackground > 0 || alphaText > 0

    private var delayCounter = 0
     // 新增配置选项
    private val mode by choices("Mode", arrayOf("New", "Classic", "Classic2"), "New")
    private val roundedValue by boolean("Rounded", false)
    private val followTarget by boolean("Follow Target", false)
    private var resetPos by boolean("Reset Position", false)

    // 原有配置选项
    private val roundedRectRadius by float("Rounded-Radius", 3F, 0F..5F)
    private val borderStrength by float("Border-Strength", 3F, 1F..5F)
    private val backgroundMode by choices("Background-Color", arrayOf("Custom", "Rainbow"), "Custom")
    private val backgroundRed by int("Background-R", 0, 0..255) { backgroundMode == "Custom" }
    private val backgroundGreen by int("Background-G", 0, 0..255) { backgroundMode == "Custom" }
    private val backgroundBlue by int("Background-B", 0, 0..255) { backgroundMode == "Custom" }
    private val backgroundAlpha by int("Background-Alpha", 255, 0..255) { backgroundMode == "Custom" }
    private val borderMode by choices("Border-Color", arrayOf("Custom", "Rainbow"), "Custom")
    private val borderRed by int("Border-R", 0, 0..255) { borderMode == "Custom" }
    private val borderGreen by int("Border-G", 0, 0..255) { borderMode == "Custom" }
    private val borderBlue by int("Border-B", 0, 0..255) { borderMode == "Custom" }
    private val borderAlpha by int("Border-Alpha", 255, 0..255) { borderMode == "Custom" }
    private val textRed by int("Text-R", 255, 0..255)
    private val textGreen by int("Text-G", 255, 0..255)
    private val textBlue by int("Text-B", 255, 0..255)
    private val textAlpha by int("Text-Alpha", 255, 0..255)
    private val rainbowX by float("Rainbow-X", -1000F, -2000F..2000F) { backgroundMode == "Rainbow" }
    private val rainbowY by float("Rainbow-Y", -1000F, -2000F..2000F) { backgroundMode == "Rainbow" }
    private val fadeSpeed by float("FadeSpeed", 2F, 1F..9F)
    private val absorption by boolean("Absorption", true)
    private val healthFromScoreboard by boolean("HealthFromScoreboard", true)
    private val animation by choices("Animation", arrayOf("Smooth", "Fade"), "Fade")
    private val animationSpeed by float("AnimationSpeed", 0.2F, 0.05F..1F)
    private val vanishDelay by int("VanishDelay", 300, 0..500)

    // 新增状态变量
    private var posX = -1.0
    private var posY = -1.0
    private var ticksSinceAttack = 0
    private var target: EntityLivingBase? = null
    private val decimalFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))
    private var easingHealth = 0F
    private var lastTarget: EntityLivingBase? = null
    fun getTargetEntity(): EntityLivingBase{
        val target = KillAura.target ?: if (delayCounter >= vanishDelay) mc.thePlayer else lastTarget ?: mc.thePlayer
        return target as EntityLivingBase
    }
    override fun drawElement(): Border {
        assumeNonVolatile = true

        // 目标更新逻辑
        if (resetPos) {
            posX = -1.0
            posY = -1.0
            resetPos = false
        }

        updateTarget()
        handlePosition()

        // 渲染主逻辑
        glPushMatrix()
        glTranslatef(posX.toFloat(), posY.toFloat(), 0f)

        when (mode.lowercase()) {
            "classic" -> renderClassicMode()
            "new" -> renderNewMode()
        }

        glPopMatrix()

        return Border(posX.toFloat(), posY.toFloat(), 162f, 50f)
    }
    private fun updateTarget() {
        ticksSinceAttack++
        target = (KillAura.target as? EntityPlayer) ?: if (ticksSinceAttack > 20) null else lastTarget
        if (mc.currentScreen is GuiChat) {
            target = mc.thePlayer
            ticksSinceAttack = 18
        }
        target?.let { lastTarget = it }
    }

    private fun handlePosition() {
        val sr = ScaledResolution(mc)
        if (posX == -1.0 || posY == -1.0) {
            posX = sr.scaledWidth / 2.0 + 50
            posY = sr.scaledHeight / 2.0 - 20
        }

        if (followTarget && target != null && target != mc.thePlayer) {
            try {
                val pos4 = RenderUtil.getProjectedEntity(
                    (target as? EntityPlayer) ?: return@handlePosition,
                    mc.timer.renderPartialTicks.toDouble(),
                    0.7
                )

                posX = MathHelper.clamp_double(pos4.x.toDouble(), 0.0, sr.scaledWidth - 162.0)
                posY = MathHelper.clamp_double(pos4.y.toDouble(), 0.0, sr.scaledHeight - 50.0)
            } catch (e: Exception) {
                // 处理异常
            }
        }
    }
    private fun drawHead(skin: ResourceLocation, x: Int, y: Int, width: Int, height: Int, color: Color) {
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        mc.textureManager.bindTexture(skin)
        drawScaledCustomSizeModalRect(x, y, 8f, 8f, 8, 8, width, height, 64f, 64f)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }
    private fun renderClassicMode() {
        target?.let {
            val healthPercent = getHealth(it, healthFromScoreboard, absorption) / it.maxHealth
            val color = if (it.hurtTime > 0) Color(255, (255 + -(it.hurtTime * 20)).coerceAtMost(255),
                (255 + -(it.hurtTime * 20)).coerceAtMost(255)) else Color.WHITE

            // 背景
            if (roundedValue) {
                RenderUtils.drawRoundedRect(0f, 0f, 120f, 40f, 6, Color(0, 0, 0, 120).rgb.toFloat())
            } else {
                drawRect(0, 0, 120, 40, Color(0, 0, 0, 120).rgb)
            }

            // 皮肤
            drawHead(mc.netHandler.getPlayerInfo(target!!.uniqueID).locationSkin, 5, 5, 30, 30, color)

            // 血条
            val barWidth = (70 * healthPercent).toInt()
            if (roundedValue) {
                RenderUtils.drawRoundedRect(40f, 20f, 70f, 15f, 2, Color(255, 255, 255, 120).rgb.toFloat())
                RenderUtils.drawRoundedRect(40f, 20f, barWidth.toFloat(), 15f, 2, ColorUtils.rainbow().rgb.toFloat())
            } else {
                drawRect(40, 20, 70, 15, Color(255, 255, 255, 120).rgb)
                drawRect(40, 20, barWidth, 15, ColorUtils.rainbow().rgb)
            }

            // 文字
            Fonts.font40.drawString(it.name, 36f, 5f, Color(textRed, textGreen, textBlue, textAlpha).rgb)
            Fonts.font35.drawString("${decimalFormat.format(healthPercent * 100)}%",
                40 + 35 - Fonts.font35.getStringWidth("${healthPercent * 100}%") / 2,
                20 + 7,
                Color.WHITE.rgb
            )
        }
    }
     private fun renderNewMode() {
        (target as? EntityPlayer)?.let { player ->
            val healthPercent = getHealth(target!!, healthFromScoreboard, absorption) / target!!.maxHealth
            val color = ColorUtils.rainbow()

            // 背景
            RenderUtils.drawRoundedRect(0f, 0f, 162f, 40f, 10, Color(0, 0, 0, 150).rgb.toFloat())

            // 皮肤
            mc.netHandler.getPlayerInfo(player.uniqueID)?.let {
                drawHead(it.locationSkin, 5, 5, 30, 30, Color.WHITE)
            }
            // 血条
            val barWidth = (95 * healthPercent).toInt()
            RenderUtils.drawRoundedRect(40f, 23f, 95f, 9f, 3, Color(151, 151, 151, 40).rgb.toFloat())
            RenderUtils.drawRoundedRect(40f, 23f, barWidth.toFloat(), 9f, 3, color.rgb.toFloat())
            RenderUtil.drawRoundedRectBorder(40f, 23f, 40f + 95f, 32f, 3f, Color(230, 230, 230, 200).rgb, 1f)
            RenderUtil.drawRoundedRectBorder(39f, 22f, 136f, 33f, 3f, Color(30, 30, 30, 100).rgb, 1f)

            // 文字
            Fonts.font40.drawString(target!!.name, 40f, 9f, Color.WHITE.rgb)
            Fonts.font35.drawString(decimalFormat.format(target!!.health), 139f, 24f, Color.WHITE.rgb)
        }
    }

}