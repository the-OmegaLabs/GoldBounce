package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import java.awt.Color

object NLCrosshair : Module("NLCrosshair", Category.RENDER, hideModule = false) {
    private val lineThickness by FloatValue("LineThickness", 1.5f, 0.5f..5.0f)
    private val lineLength by FloatValue("LineLength", 20f, 5f..1000f)
    private val colorRed by FloatValue("Red", 255f, 0f..255f)
    private val colorGreen by FloatValue("Green", 255f, 0f..255f)
    private val colorBlue by FloatValue("Blue", 255f, 0f..255f)
    private val colorAlpha by FloatValue("Alpha", 255f, 0f..255f)
    private val dynamicColor by BoolValue("DynamicColor", false)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val resolution = ScaledResolution(mc)
        val centerX = resolution.scaledWidth / 2
        val centerY = resolution.scaledHeight / 2
        val color = if (dynamicColor) {
            Color.getHSBColor((System.currentTimeMillis() % 1000) / 1000f, 1f, 1f)
        } else {
            Color(colorRed.toInt(), colorGreen.toInt(), colorBlue.toInt(), colorAlpha.toInt())
        }

        drawLine(centerX - lineLength.toInt(), centerY, centerX + lineLength.toInt(), centerY, lineThickness, color)
        drawLine(centerX, centerY - lineLength.toInt(), centerX, centerY + lineLength.toInt(), lineThickness, color)
    }

    private fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, thickness: Float, color: Color) {
        val tessellator = net.minecraft.client.renderer.Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer

        net.minecraft.client.renderer.GlStateManager.pushMatrix()
        net.minecraft.client.renderer.GlStateManager.disableTexture2D()
        net.minecraft.client.renderer.GlStateManager.enableBlend()
        net.minecraft.client.renderer.GlStateManager.blendFunc(770, 771)
        net.minecraft.client.renderer.GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        GL11.glLineWidth(thickness)  // Replaced GLStateManager.glLineWidth with GL11.glLineWidth
        worldrenderer.begin(1, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION)
        worldrenderer.pos(x1.toDouble(), y1.toDouble(), 0.0).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        tessellator.draw()

        net.minecraft.client.renderer.GlStateManager.enableTexture2D()
        net.minecraft.client.renderer.GlStateManager.popMatrix()
    }

}
