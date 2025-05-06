package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

object WaterMark : Module("WaterMark", Category.RENDER) {

    // Configuration variables
    private var posX = 0
    private var posY = 0
    private val bgColor = Color(0, 0, 0, 120)
    private val textColor = Color.WHITE

    private var animationProgress = 0f
    private var animationSpeed = 0.05f

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = ScaledResolution(mc)
        posX = sr.scaledWidth / 2
        posY = sr.scaledHeight / 10

        val fps = Minecraft.getDebugFPS()
        val ping = mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID)?.responseTime ?: 0

        val watermarkText = "Obai | ${mc.session.username} | ${fps}fps | ${ping}ms"

        val logoWidth = 20
        val textWidth = Fonts.font35.getStringWidth(watermarkText)
        val totalWidth = textWidth + logoWidth + 40
        val height = 30

        animationProgress = if (animationProgress < 1f) {
            (animationProgress + animationSpeed).coerceAtMost(1f)
        } else {
            1f
        }

        val animatedWidth = totalWidth * animationProgress

        // Enable blur effect (simulate by alpha blending)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        // Draw rounded rectangle background
        RenderUtils.drawRoundedRect(
            (posX - animatedWidth / 2).toFloat(),
            posY.toFloat(),
            (posX + animatedWidth / 2).toFloat(),
            (posY + height).toFloat(),
            bgColor.rgb,
            15f

        )

        // Draw logo (placeholder as a circle for now)
        GlStateManager.color(1f, 1f, 1f, 1f)
        RenderUtils.drawCircle((posX - animatedWidth / 2 + 15).toFloat(), (posY + height / 2).toFloat(),8f,0,360)

        // Draw text
        Fonts.fontHonor40.drawString(
            watermarkText,
            (posX - animatedWidth / 2 + logoWidth + 10).toFloat(),
            (posY + (height - Fonts.font35.FONT_HEIGHT) / 2).toFloat(),
            textColor.rgb
        )

        // Disable blending
        GL11.glDisable(GL11.GL_BLEND)
    }
    private fun drawShadow(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float) {
        val shadowColor = Color(0, 0, 0, 100).rgb
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        RenderUtils.drawRoundedRect(x1 - 5, y1 - 5, x2 + 5, y2 + 5, shadowColor, radius)
        GL11.glDisable(GL11.GL_BLEND)
    }
    override fun onDisable() {
        // Reset animation progress
        animationProgress = 0f
    }
}
