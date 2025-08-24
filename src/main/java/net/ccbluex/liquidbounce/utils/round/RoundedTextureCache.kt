package net.ccbluex.liquidbounce.utils.round

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import org.lwjgl.opengl.GL11

object RoundedTextureCache {
    private data class Key(val w: Int, val h: Int, val argb: Int, val radius: Int, val scale: Int)
    private val cache = ConcurrentHashMap<Key, ResourceLocation>()

    /**
     * 获取带圆角的纹理（缓存）。scale = 超采样倍数（2 或 3），越大越平滑但生成慢/占内存。
     */
    fun getRoundedTexture(w: Int, h: Int, color: Color, radius: Int, scale: Int = 2): ResourceLocation {
        val key = Key(w.coerceAtLeast(1), h.coerceAtLeast(1), color.rgb, radius.coerceAtLeast(0), scale.coerceAtLeast(1))
        cache[key]?.let { return it }
        val hiW = key.w * key.scale
        val hiH = key.h * key.scale
        val hiRadius = key.radius * key.scale

        val hiImg = BufferedImage(hiW.coerceAtLeast(1), hiH.coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB)
        val gHi = hiImg.createGraphics()
        try {
            gHi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            gHi.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
            gHi.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            gHi.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

            // 透明背景先清空
            gHi.composite = java.awt.AlphaComposite.Src
            gHi.color = Color(0, 0, 0, 0)
            gHi.fillRect(0, 0, hiW, hiH)

            // 绘制圆角矩形（高分辨率）
            gHi.color = color
            val arc = hiRadius.coerceAtMost(kotlin.math.min(hiW, hiH) / 2) * 2
            gHi.fillRoundRect(0, 0, hiW, hiH, arc, arc)
        } finally {
            gHi.dispose()
        }

        val dstImg = BufferedImage(key.w, key.h, BufferedImage.TYPE_INT_ARGB)
        val gDst = dstImg.createGraphics()
        try {
            gDst.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            gDst.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            gDst.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            gDst.drawImage(hiImg, 0, 0, key.w, key.h, null)
        } finally {
            gDst.dispose()
        }

        val dyn = DynamicTexture(dstImg)
        val rl = Minecraft.getMinecraft().textureManager.getDynamicTextureLocation(
            "rounded_${key.w}_${key.h}_${key.argb}_${key.radius}_${key.scale}", dyn
        )

        try {
            Minecraft.getMinecraft().textureManager.bindTexture(rl)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        } catch (e: Exception) {

        }

        cache[key] = rl
        return rl
    }

    fun clearCache() {
        cache.clear()
    }
}
