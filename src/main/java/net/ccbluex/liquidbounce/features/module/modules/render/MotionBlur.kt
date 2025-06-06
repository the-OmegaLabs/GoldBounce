package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.float
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import op.wawa.opacketfix.features.hytpacket.CustomPacket
import org.lwjgl.opengl.GL11


object MotionBlur : Module("MotionBlur", Category.RENDER) {
    val blurAmount by float("BlurAmount", 7f, 0.0f..10.0f)
    private var framebuffer: Framebuffer? = null
    private var framebuffer_: Framebuffer? = null

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen == null && mc.theWorld != null) {
            if (OpenGlHelper.isFramebufferEnabled()) {
                val width = mc.framebuffer.framebufferWidth
                val height = mc.framebuffer.framebufferHeight

                GlStateManager.matrixMode(5889)
                GlStateManager.loadIdentity()
                GlStateManager.ortho(0.0, width.toDouble(), height.toDouble(), 0.0, 2000.0, 4000.0)
                GlStateManager.matrixMode(5888)
                GlStateManager.loadIdentity()
                GlStateManager.translate(0f, 0f, -2000f)
                framebuffer = checkFramebufferSizes(framebuffer, width, height)
                framebuffer_ = checkFramebufferSizes(framebuffer_, width, height)
                framebuffer_!!.framebufferClear()
                framebuffer_!!.bindFramebuffer(true)
                OpenGlHelper.glBlendFunc(770, 771, 0, 1)
                GlStateManager.disableLighting()
                GlStateManager.disableFog()
                GlStateManager.disableBlend()
                mc.framebuffer.bindFramebufferTexture()
                GlStateManager.color(1f, 1f, 1f, 1f)
                drawTexturedRectNoBlend(0.0f, 0.0f, width.toFloat(), height.toFloat(), 0.0f, 1.0f, 0.0f, 1.0f, 9728)
                GlStateManager.enableBlend()
                framebuffer!!.bindFramebufferTexture()
                GlStateManager.color(1f, 1f, 1f, blurAmount / 10f - 0.1f)
                drawTexturedRectNoBlend(0f, 0f, width.toFloat(), height.toFloat(), 0f, 1f, 1f, 0f, 9728)
                mc.framebuffer.bindFramebuffer(true)
                framebuffer_!!.bindFramebufferTexture()
                GlStateManager.color(1f, 1f, 1f, 1f)
                GlStateManager.enableBlend()
                OpenGlHelper.glBlendFunc(770, 771, 1, 771)
                drawTexturedRectNoBlend(0.0f, 0.0f, width.toFloat(), height.toFloat(), 0.0f, 1.0f, 0.0f, 1.0f, 9728)
                val tempBuff = this.framebuffer
                framebuffer = this.framebuffer_
                framebuffer_ = tempBuff
            }
        }
    }

    private fun checkFramebufferSizes(framebuffer: Framebuffer?, width: Int, height: Int): Framebuffer {
        var framebuffer = framebuffer
        if (framebuffer == null || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            if (framebuffer == null) {
                framebuffer = Framebuffer(width, height, true)
            } else {
                framebuffer.createBindFramebuffer(width, height)
            }

            framebuffer.setFramebufferFilter(9728)
        }

        return framebuffer
    }

    fun drawTexturedRectNoBlend(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        uMin: Float,
        uMax: Float,
        vMin: Float,
        vMax: Float,
        filter: Int
    ) {
        GlStateManager.enableTexture2D()
        GL11.glTexParameteri(3553, 10241, filter)
        GL11.glTexParameteri(3553, 10240, filter)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), ((y + height).toDouble()), 0.0).tex(uMin.toDouble(), vMax.toDouble()).endVertex()
        worldrenderer.pos(((x + width).toDouble()), ((y + height).toDouble()), 0.0).tex(uMax.toDouble(),
            vMax.toDouble()
        ).endVertex()
        worldrenderer.pos(((x + width).toDouble()), y.toDouble(), 0.0).tex(uMax.toDouble(), vMin.toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(uMin.toDouble(), vMin.toDouble()).endVertex()
        tessellator.draw()
        GL11.glTexParameteri(3553, 10241, 9728)
        GL11.glTexParameteri(3553, 10240, 9728)
    }

    override val tag: String
        get() = blurAmount.toString()
}