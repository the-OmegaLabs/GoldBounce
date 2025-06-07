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
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.glDepthMask
import org.lwjgl.opengl.GL11.glDisable
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

    private val shadowValue = BoolValue("Shadow", true)
    private val roundingValue = FloatValue("Rounding", 5f, 0f..10f)
    private val shadowRadius = FloatValue("ShadowRadius", 6f, 0f..20f)
    private val bgAlpha = FloatValue("BackgroundAlpha", 0.8f, 0f..1f)
    private val scaleValue = FloatValue("Scale", 1f, 1f..5f)
    private val fontValue = FontValue("Font", Fonts.font40)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || entity == mc.thePlayer || entity.isInvisible) continue

            renderModernTag(entity,
                ColorUtils.stripColor(entity.displayName.unformattedText) ?: entity.displayName.unformattedText,
                entity.health
            )
        }
    }

    private fun renderModernTag(entity: EntityLivingBase, name: String, health: Float) {
        pushMatrix()
        disableDepth()
        enableBlend()
        disableLighting()
        val timer = mc.timer
        val renderManager = mc.renderManager
        val interpolatedPos = entity.getLerpedPos(timer.renderPartialTicks)
        translate(
            interpolatedPos.xCoord - renderManager.viewerPosX,
            interpolatedPos.yCoord + entity.eyeHeight + 0.6 - renderManager.viewerPosY,
            interpolatedPos.zCoord - renderManager.viewerPosZ
        )
        rotate(-renderManager.playerViewY, 0f, 1f, 0f)
        rotate(renderManager.playerViewX, 1f, 0f, 0f)
        val distance = mc.thePlayer.getDistanceToEntity(entity)

        // 修复缩放计算：使用反比例确保距离越远标签越小
        val baseScale = 0.01f
        val minDistance = 5f
        val adjustedDistance = distance.coerceAtLeast(minDistance).coerceAtMost(6F)
        val scale = (baseScale * scaleValue.get()) / (adjustedDistance * 0.3f)

        scale(-scale, -scale, scale)

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
        enableDepth()
        popMatrix()
    }

    private fun disableDepth() {
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
    }
}