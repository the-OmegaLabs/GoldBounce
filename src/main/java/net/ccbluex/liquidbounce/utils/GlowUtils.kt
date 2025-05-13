package net.ccbluex.liquidbounce.utils

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureUtil
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.image.BufferedImage
import com.jhlabs.image.GaussianFilter

object GlowUtils {
    private val shadowCache = HashMap<Int?, Int?>()

    fun drawGlow(x: Float, y: Float, width: Float, height: Float, blurRadius: Int, color: Color) {
        var x = x
        var y = y
        var width = width
        var height = height
        GL11.glPushMatrix()
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f)

        width = width + blurRadius * 2
        height = height + blurRadius * 2
        x = x - blurRadius
        y = y - blurRadius

        val _X = x - 0.25f
        val _Y = y + 0.25f

        val identifier = (width * height + width + color.hashCode() * blurRadius + blurRadius).toInt()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GlStateManager.enableBlend()

        var texId: Int? = -1
        if (shadowCache.containsKey(identifier)) {
            texId = shadowCache.get(identifier)

            texId?.let { GlStateManager.bindTexture(it) }
        } else {
            if (width <= 0) width = 1f
            if (height <= 0) height = 1f
            val original = BufferedImage(width.toInt(), height.toInt(), BufferedImage.TYPE_INT_ARGB_PRE)

            val g = original.getGraphics()
            g.setColor(color)
            g.fillRect(blurRadius, blurRadius, (width - blurRadius * 2).toInt(), (height - blurRadius * 2).toInt())
            g.dispose()

            val op: GaussianFilter = GaussianFilter(blurRadius.toFloat())

            val blurred: BufferedImage = op.filter(original, null)


            texId = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), blurred, true, false)

            shadowCache.put(identifier, texId)
        }

        GL11.glColor4f(1f, 1f, 1f, 1f)

        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0f, 0f) // top left
        GL11.glVertex2f(_X, _Y)

        GL11.glTexCoord2f(0f, 1f) // bottom left
        GL11.glVertex2f(_X, _Y + height)

        GL11.glTexCoord2f(1f, 1f) // bottom right
        GL11.glVertex2f(_X + width, _Y + height)

        GL11.glTexCoord2f(1f, 0f) // top right
        GL11.glVertex2f(_X + width, _Y)
        GL11.glEnd()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()

        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glPopMatrix()
    }
}