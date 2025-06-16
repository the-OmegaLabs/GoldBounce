package net.ccbluex.liquidbounce.utils

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureUtil
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.awt.Color
import java.awt.image.BufferedImage
import com.jhlabs.image.GaussianFilter
import java.util.LinkedHashMap

object GlowUtils {
    private val shadowCache = object : LinkedHashMap<String, Int>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Int>): Boolean {
            return if (size > MAX_CACHE_SIZE) {
                GL11.glDeleteTextures(eldest.value)
                true
            } else {
                false
            }
        }
    }

    private const val MAX_CACHE_SIZE = 50

    fun drawGlow(x: Float, y: Float, width: Float, height: Float, blurRadius: Int, color: Color) {
        var drawX = x
        var drawY = y
        val drawW = width + blurRadius * 2
        val drawH = height + blurRadius * 2
        drawX -= blurRadius
        drawY -= blurRadius
        val texW = if (drawW.toInt() > 0) drawW.toInt() else 1
        val texH = if (drawH.toInt() > 0) drawH.toInt() else 1
        val key = "${texW}x${texH}_r${blurRadius}_c${color.rgb}"
        GL11.glPushMatrix()
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        var texId: Int? = shadowCache[key]
        if (texId == null) {
            val original = BufferedImage(texW, texH, BufferedImage.TYPE_INT_ARGB_PRE)
            val g = original.graphics
            val innerW = width.toInt().coerceAtLeast(1)
            val innerH = height.toInt().coerceAtLeast(1)
            g.color = color
            g.fillRect(blurRadius, blurRadius, innerW, innerH)
            g.dispose()
            val op = GaussianFilter(blurRadius.toFloat())
            val blurred = op.filter(original, null)
            val genId = TextureUtil.glGenTextures()
            texId = TextureUtil.uploadTextureImageAllocate(genId, blurred, true, false)
            texId.let {
                GlStateManager.bindTexture(it)
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
                try {
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
                } catch (_: NoSuchFieldError) {
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP)
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP)
                }
                shadowCache[key] = it
            }
        } else {
            texId.let { GlStateManager.bindTexture(it) }
        }
        texId.let { GlStateManager.bindTexture(it) }
        GL11.glColor4f(1f, 1f, 1f, 1f)
        val alignedX = drawX - 0.25f
        val alignedY = drawY + 0.25f
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0f, 0f)
        GL11.glVertex2f(alignedX, alignedY)
        GL11.glTexCoord2f(0f, 1f)
        GL11.glVertex2f(alignedX, alignedY + texH)
        GL11.glTexCoord2f(1f, 1f)
        GL11.glVertex2f(alignedX + texW, alignedY + texH)
        GL11.glTexCoord2f(1f, 0f)
        GL11.glVertex2f(alignedX + texW, alignedY)
        GL11.glEnd()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glPopMatrix()
    }

    fun clearCache() {
        for (texId in shadowCache.values) {
            GL11.glDeleteTextures(texId)
        }
        shadowCache.clear()
    }
}
