/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.utils.skid.slack.RenderUtil
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.EntityUtils.getHealth
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawScaledCustomSizeModalRect
import net.ccbluex.liquidbounce.utils.skid.moonlight.render.ColorUtil
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.ScaledResolution
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
    private val mode by choices("Mode", arrayOf("New", "Classic", "Classic2", "Hide"), "New")
    private val roundedValue by boolean("Rounded", false)
    private val followTarget by boolean("Follow Target", false)
    private var resetPos by boolean("Reset Position", false)

    // 原有配置选项
    private val textRed by int("Text-R", 255, 0..255)
    private val textGreen by int("Text-G", 255, 0..255)
    private val textBlue by int("Text-B", 255, 0..255)
    private val textAlpha by int("Text-Alpha", 255, 0..255)
    private val absorption by boolean("Absorption", true)
    private val healthFromScoreboard by boolean("HealthFromScoreboard", true)
    private val vanishDelay by int("VanishDelay", 300, 0..500)
    // 在配置选项区域添加
    private val showDistance by boolean("ShowDistance", true) { mode == "Classic2" }
    private val verticalHealth by boolean("VerticalHealth", true) { mode == "Classic2" }

    // 新增状态变量
    private var posX = -1.0
    private var posY = -1.0
    private var ticksSinceAttack = 0
    private var target: EntityPlayer? = null
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
            "classic2" -> renderClassic2Mode()
            "new" -> renderNewMode()
        }

        glPopMatrix()

        return Border(posX.toFloat(), posY.toFloat(), 162f, 50f)
    }

    // 修改updateTarget()
    private fun updateTarget() {
        ticksSinceAttack++
        target = ((KillAura.target as? EntityPlayer) ?: if (ticksSinceAttack > 20) null else lastTarget) as EntityPlayer?
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
                LOGGER.error("Position calculation failed: ${e.stackTraceToString()}")
                posX = -1.0 // 触发位置重置
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
        target?.let { player ->
            val healthPercent = getHealth(player, healthFromScoreboard, absorption) / player.maxHealth
            val color = if (player.hurtTime > 0) Color(
                255,
                (255 + -(player.hurtTime * 20)).coerceAtMost(255),
                (255 + -(player.hurtTime * 20)).coerceAtMost(255)
            ) else Color.WHITE

            // 背景渲染
            if (roundedValue) {
                RenderUtils.drawRoundedRect(0f, 0f, 120f, 40f, 6, Color(0, 0, 0, 120).rgb.toFloat())
            } else {
                drawRect(0, 0, 120, 40, Color(0, 0, 0, 120).rgb)
            }

            // 安全获取皮肤
            mc.netHandler.getPlayerInfo(player.uniqueID)?.let {
                drawHead(it.locationSkin, 5, 5, 30, 30, color)
            }

            // 血条逻辑
            val barWidth = (70 * healthPercent).toInt()
            if (roundedValue) {
                RenderUtils.drawRoundedRect(40f, 20f, 70f, 15f, 2, Color(255, 255, 255, 120).rgb.toFloat())
                RenderUtils.drawRoundedRect(40f, 20f, barWidth.toFloat(), 15f, 2, ColorUtils.rainbow().rgb.toFloat())
            } else {
                drawRect(40, 20, 70, 15, Color(255, 255, 255, 120).rgb)
                drawRect(40, 20, barWidth, 15, ColorUtils.rainbow().rgb)
            }

            // 文字渲染
            Fonts.font40.drawString(
                player.name,
                36f,
                5f,
                Color(textRed, textGreen, textBlue, textAlpha).rgb
            )
            Fonts.font35.drawString(
                "${decimalFormat.format(healthPercent * 100)}%",
                40 + 35 - Fonts.font35.getStringWidth("${healthPercent * 100}%") / 2,
                27, // 调整垂直居中
                Color.WHITE.rgb
            )
        }
    }
    private fun renderClassic2Mode() {
        target?.let { player ->
            val healthPercent = getHealth(player, healthFromScoreboard, absorption) / player.maxHealth
            val distance = mc.thePlayer.getDistanceToEntityBox(player).roundToInt()

            // 背景渲染
            RenderUtils.drawRoundedRect(0f, 0f, 100f, 50f, 5, Color(30, 30, 30, 180).rgb.toFloat())

            // 左侧垂直血条
            val barHeight = (40 * healthPercent).toInt()
            RenderUtils.drawRoundedRect(5f, 5f, 8f, 40f, 2, Color(80, 80, 80).rgb.toFloat())
            RenderUtils.drawRoundedRect(5f, 5f + (40 - barHeight), 8f, barHeight.toFloat(), 2,
                ColorUtil.interpolateColor(Color.RED, Color.GREEN, healthPercent).toFloat()
            )

            // 玩家信息
            mc.netHandler.getPlayerInfo(player.uniqueID)?.let {
                drawHead(it.locationSkin, 18, 5, 25, 25, Color.WHITE)
            }

            // 文字信息
            Fonts.font35.drawString(player.name, 48f, 8f, Color.WHITE.rgb)
            Fonts.font35.drawString("§7${distance}m", 48f, 20f, Color(170, 170, 170).rgb)
            Fonts.font35.drawString(
                "§c❤ ${decimalFormat.format(player.health)}",
                48f,
                32f,
                Color(230, 80, 80).rgb
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