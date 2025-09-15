/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import kotlinx.coroutines.channels.BroadcastChannel
import lombok.Getter
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import scala.annotation.switch
import java.awt.Color
import kotlin.math.roundToInt

private fun Entity.getLerpedPos(partialTicks: Float): Vec3 {
    return Vec3(
        this.lastTickPosX + (this.posX - this.lastTickPosX) * partialTicks,
        this.lastTickPosY + (this.posY - this.lastTickPosY) * partialTicks,
        this.lastTickPosZ + (this.posZ - this.lastTickPosZ) * partialTicks
    )
}

object NameTags : Module("NameTags", Category.RENDER) {
    private val mode = ListValue("Mode", arrayOf("Opai", "Rise", "Simple"), "Opai")
    private val shadowValue = BoolValue("Shadow", true)
    private val roundingValue = FloatValue("Rounding", 5f, 0f..10f)
    private val shadowRadius = FloatValue("ShadowRadius", 6f, 0f..20f)
    private val bgAlpha = FloatValue("BackgroundAlpha", 0.8f, 0f..1f)
    private val scaleValue = FloatValue("Scale", 1f, 1f..5f)
    private val fontValue = FontValue("Font", Fonts.font40)
    private val BASE_SCALE = 0.016f
    private val MIN_DISTANCE = 2.0f
    private val MAX_SCALE = 0.08f
    private val MIN_SCALE = 0.008f

    // 用于记录玩家受击时间的Map
    private val hurtTimeMap = mutableMapOf<EntityLivingBase, Long>()

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || entity == mc.thePlayer || entity.isInvisible) continue
            when (mode.get()) {
                "Opai" -> {
                    renderModernTag(entity,
                        ColorUtils.stripColor(entity.displayName.unformattedText) ?: entity.displayName.unformattedText,
                        entity.health
                    )
                }
                "Rise" -> {
                    renderRiseTag(entity,
                        ColorUtils.stripColor(entity.displayName.unformattedText) ?: entity.displayName.unformattedText,
                        entity.health
                    )
                }
                "Simple" -> {
                    renderSimpleTag(entity,
                        ColorUtils.stripColor(entity.displayName.unformattedText) ?: entity.displayName.unformattedText
                    )
                }
            }
        }
    }
    private fun computeScale(distance: Float, base: Float = BASE_SCALE): Float {
        val d = distance.coerceAtLeast(MIN_DISTANCE)
        val s = base * scaleValue.get() / d
        return s.coerceIn(MIN_SCALE, MAX_SCALE)
    }
    private fun renderRiseTag(entity: EntityLivingBase, name: String, health: Float) {
        val timer = mc.timer
        val renderManager = mc.renderManager
        val interpolatedPos = entity.getLerpedPos(timer.renderPartialTicks)
        pushMatrix()
        translate(
            interpolatedPos.xCoord - renderManager.viewerPosX,
            interpolatedPos.yCoord + entity.eyeHeight + 0.6 - renderManager.viewerPosY,
            interpolatedPos.zCoord - renderManager.viewerPosZ
        )
        rotate(-renderManager.playerViewY, 0f, 1f, 0f)
        rotate(renderManager.playerViewX, 1f, 0f, 0f)
        val distance = mc.thePlayer.getDistanceToEntity(entity).coerceAtLeast(1f)
        val scale = computeScale(distance, 0.016f)
        scale(-scale, -scale, scale)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        enableBlend()
        disableLighting()
        val font = fontValue.get()
        val healthText = "HP: %.1f".format(health)
        val nameWidth = font.getStringWidth(name)
        val healthWidth = font.getStringWidth(healthText)
        val maxWidth = nameWidth.coerceAtLeast(healthWidth)
        val padding = 8f
        val lineSpacing = 4f
        val totalWidth = maxWidth + padding * 2
        val totalHeight = font.FONT_HEIGHT * 2 + lineSpacing + padding * 2
        if (shadowValue.get()) {
            GlowUtils.drawGlow(
                -totalWidth / 2f - 4f, -totalHeight / 2f - 4f,
                totalWidth + 8f, totalHeight + 8f,
                shadowRadius.get().toInt(),
                Color(0, 0, 0, 150)
            )
        }
        val bgColor = Color(0, 0, 0, (180 * bgAlpha.get()).toInt())
        RenderUtils.drawRoundedRect(
            -totalWidth / 2f, -totalHeight / 2f,
            totalWidth / 2f, totalHeight / 2f,
            bgColor.rgb, roundingValue.get()
        )
        font.drawString(
            name,
            -nameWidth / 2f,
            -totalHeight / 2f + padding,
            Color.WHITE.rgb,
            false
        )
        val healthColor = when {
            health > 15f -> Color(0x00, 0xFF, 0x00)
            health > 10f -> Color(0xFF, 0xFF, 0x00)
            health > 5f -> Color(0xFF, 0xAA, 0x00)
            else -> Color(0xFF, 0x00, 0x00)
        }
        font.drawString(
            healthText,
            -healthWidth / 2f,
            -totalHeight / 2f + padding + font.FONT_HEIGHT + lineSpacing,
            healthColor.rgb,
            false
        )
        enableLighting()
        disableBlend()
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        popMatrix()
    }

    private fun renderSimpleTag(entity: EntityLivingBase, name: String) {
        val timer = mc.timer
        val renderManager = mc.renderManager
        val interpolatedPos = entity.getLerpedPos(timer.renderPartialTicks)
        pushMatrix()
        translate(
            interpolatedPos.xCoord - renderManager.viewerPosX,
            interpolatedPos.yCoord + entity.eyeHeight + 0.5 - renderManager.viewerPosY,
            interpolatedPos.zCoord - renderManager.viewerPosZ
        )
        rotate(-renderManager.playerViewY, 0f, 1f, 0f)
        rotate(renderManager.playerViewX, 1f, 0f, 0f)
        val distance = mc.thePlayer.getDistanceToEntity(entity).coerceAtLeast(1f)
        val scale = computeScale(distance, 0.02f)
        scale(-scale, -scale, scale)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        enableBlend()
        disableLighting()
        if (entity.hurtTime > 0) {
            hurtTimeMap[entity] = System.currentTimeMillis()
        }
        val currentTime = System.currentTimeMillis()
        val lastHurtTime = hurtTimeMap[entity] ?: 0L
        val timeSinceHurt = currentTime - lastHurtTime
        val fadeTime = 1000L
        val color = if (timeSinceHurt < fadeTime) {
            val factor = timeSinceHurt.toFloat() / fadeTime
            val red = 255
            val green = (255 * factor).toInt()
            val blue = (255 * factor).toInt()
            Color(red, green, blue)
        } else {
            Color.WHITE
        }
        val nameWidth = mc.fontRendererObj.getStringWidth(name)
        if (shadowValue.get()) {
            RenderUtils.drawRect(
                -nameWidth / 2f - 2f,
                -2f,
                nameWidth / 2f + 2f,
                mc.fontRendererObj.FONT_HEIGHT + 2f,
                Color(0, 0, 0, (100 * bgAlpha.get()).toInt()).rgb
            )
        }
        mc.fontRendererObj.drawString(
            name,
            -nameWidth / 2F,
            0F,
            color.rgb,
            false
        )
        enableLighting()
        disableBlend()
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        popMatrix()
    }

    private fun renderModernTag(entity: EntityLivingBase, name: String, health: Float) {
        val timer = mc.timer
        val renderManager = mc.renderManager
        val interpolatedPos = entity.getLerpedPos(timer.renderPartialTicks)
        pushMatrix()
        translate(
            interpolatedPos.xCoord - renderManager.viewerPosX,
            interpolatedPos.yCoord + entity.eyeHeight + 0.6 - renderManager.viewerPosY,
            interpolatedPos.zCoord - renderManager.viewerPosZ
        )
        rotate(-renderManager.playerViewY, 0f, 1f, 0f)
        rotate(renderManager.playerViewX, 1f, 0f, 0f)
        val distance = mc.thePlayer.getDistanceToEntity(entity).coerceAtLeast(1f)
        val scale = computeScale(distance, 0.016f)
        scale(-scale, -scale, scale)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        enableBlend()
        disableLighting()
        val font = fontValue.get()
        val distanceText = "${distance.roundToInt()}m"
        val healthText = "%.1f".format(health)
        val nameWidth = font.getStringWidth(name)
        val healthWidth = font.getStringWidth(healthText)
        val distanceWidth = font.getStringWidth(distanceText)
        val padding = 4f
        val elementSpacing = 2f
        val leftWidth = healthWidth + padding * 2
        val middleWidth = nameWidth + padding * 2
        val rightWidth = distanceWidth + padding * 2
        val totalWidth = leftWidth + middleWidth + rightWidth + elementSpacing * 2
        val height = font.FONT_HEIGHT + padding * 2
        if (shadowValue.get()) {
            GlowUtils.drawGlow(
                -totalWidth / 2 - 4f, -4f,
                totalWidth + 8f, height + 8f,
                shadowRadius.get().toInt(),
                Color(0, 0, 0, 100))
        }
        val bgColor = Color(30, 30, 30, (200 * bgAlpha.get()).toInt())
        RenderUtils.drawRoundedRect(
            -totalWidth / 2f, 0f,
            -totalWidth / 2f + leftWidth, height,
            bgColor.rgb, roundingValue.get()
        )
        RenderUtils.drawRoundedRect(
            -totalWidth / 2f + leftWidth + elementSpacing, 0f,
            -totalWidth / 2f + leftWidth + elementSpacing + middleWidth, height,
            bgColor.rgb, roundingValue.get()
        )
        RenderUtils.drawRoundedRect(
            -totalWidth / 2f + leftWidth + elementSpacing * 2 + middleWidth, 0f,
            -totalWidth / 2f + leftWidth + elementSpacing * 2 + middleWidth + rightWidth, height,
            bgColor.rgb, roundingValue.get()
        )
        val kiwiColor = Color(0x7F, 0xCF, 0x00)
        val grayColor = Color(0xAA, 0xAA, 0xAA)
        font.drawString(
            healthText,
            -totalWidth / 2f + padding,
            padding,
            kiwiColor.rgb,
            false
        )
        font.drawString(
            name,
            -totalWidth / 2f + leftWidth + elementSpacing + padding,
            padding,
            Color.WHITE.rgb,
            false
        )
        font.drawString(
            distanceText,
            -totalWidth / 2f + leftWidth + elementSpacing * 2 + middleWidth + padding,
            padding,
            grayColor.rgb,
            false
        )
        enableLighting()
        disableBlend()
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        popMatrix()
    }
}
