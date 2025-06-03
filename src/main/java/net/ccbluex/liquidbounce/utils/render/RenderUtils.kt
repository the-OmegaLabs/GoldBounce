/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils.render

import co.uk.hexeption.utils.OutlineUtils
import net.ccbluex.liquidbounce.features.module.modules.settings.Interface.overrideGlow
import net.ccbluex.liquidbounce.features.module.modules.settings.Interface.overrideRoundedRectShadow
import net.ccbluex.liquidbounce.features.module.modules.settings.Interface.overrideStrength
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils.disableFastRender
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.ImageUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.setColour
import net.ccbluex.liquidbounce.utils.skid.moonlight.math.MathUtils.interpolate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.*
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_MULTISAMPLE
import org.lwjgl.opengl.GL14
import org.lwjgl.util.glu.GLU
import java.awt.Color
import java.awt.image.BufferedImage
import javax.vecmath.Vector2f
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object RenderUtils : MinecraftInstance() {
    val glCapMap = mutableMapOf<Int, Boolean>()
    val DISPLAY_LISTS_2D = IntArray(4)
    var deltaTime = 0
    fun drawHead(
        skin: ResourceLocation?,
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float,
        color: Color
    ) {
        glPushMatrix()
        val texture: ResourceLocation = skin ?: mc.thePlayer.locationSkin

        glColor(color)
        mc.textureManager.bindTexture(texture)
        drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight)
        glColor(Color.WHITE)
        glPopMatrix()
    }


    /**
     * Draw gradient rect.
     *
     * @param left       the left
     * @param top        the top
     * @param right      the right
     * @param bottom     the bottom
     * @param startColor the start color
     * @param endColor   the end color
     */
    fun drawGradientRect(
        left: Number, top: Number, right: Number, bottom: Number, startColor: Int, endColor: Int, zLevel: Float
    ) {
        val a1 = (startColor shr 24 and 255) / 255f
        val r1 = (startColor shr 16 and 255) / 255f
        val g1 = (startColor shr 8 and 255) / 255f
        val b1 = (startColor and 255) / 255f
        val a2 = (endColor shr 24 and 255) / 255f
        val r2 = (endColor shr 16 and 255) / 255f
        val g2 = (endColor shr 8 and 255) / 255f
        val b2 = (endColor and 255) / 255f

        pushMatrix()
        disableTexture2D()
        enableBlend()
        disableAlpha()
        tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        shadeModel(GL_SMOOTH)

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.worldRenderer

        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        buffer.pos(right.toDouble(), top.toDouble(), zLevel.toDouble()).color(r2, g2, b2, a2).endVertex()
        buffer.pos(left.toDouble(), top.toDouble(), zLevel.toDouble()).color(r1, g1, b1, a1).endVertex()
        buffer.pos(left.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(r1, g1, b1, a1).endVertex()
        buffer.pos(right.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(r2, g2, b2, a2).endVertex()
        tessellator.draw()

        shadeModel(GL_FLAT)
        disableBlend()
        enableAlpha()
        enableTexture2D()
        popMatrix()
    }
    fun deltaTimeNormalized(ticks: Int = 50) = (deltaTime / ticks.toDouble()).coerceAtMost(1.0)
    inline fun withClipping(main: () -> Unit, toClip: () -> Unit) {
        disableFastRender()
        OutlineUtils.checkSetupFBO()
        glPushMatrix()

        disableAlpha()

        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_ALWAYS, 1, 1)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glStencilMask(1)
        glClear(GL_STENCIL_BUFFER_BIT)

        main()

        glStencilFunc(GL_EQUAL, 1, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glStencilMask(0)

        toClip()

        glStencilMask(0xFF)
        glDisable(GL_STENCIL_TEST)

        enableAlpha()

        glPopMatrix()
    }
    init {
        for (i in DISPLAY_LISTS_2D.indices) {
            DISPLAY_LISTS_2D[i] = glGenLists(1)
        }

        glNewList(DISPLAY_LISTS_2D[0], GL_COMPILE)
        quickDrawRect(-7f, 2f, -4f, 3f)
        quickDrawRect(4f, 2f, 7f, 3f)
        quickDrawRect(-7f, 0.5f, -6f, 3f)
        quickDrawRect(6f, 0.5f, 7f, 3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[1], GL_COMPILE)
        quickDrawRect(-7f, 3f, -4f, 3.3f)
        quickDrawRect(4f, 3f, 7f, 3.3f)
        quickDrawRect(-7.3f, 0.5f, -7f, 3.3f)
        quickDrawRect(7f, 0.5f, 7.3f, 3.3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[2], GL_COMPILE)
        quickDrawRect(4f, -20f, 7f, -19f)
        quickDrawRect(-7f, -20f, -4f, -19f)
        quickDrawRect(6f, -20f, 7f, -17.5f)
        quickDrawRect(-7f, -20f, -6f, -17.5f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[3], GL_COMPILE)
        quickDrawRect(7f, -20f, 7.3f, -17.5f)
        quickDrawRect(-7.3f, -20f, -7f, -17.5f)
        quickDrawRect(4f, -20.3f, 7.3f, -20f)
        quickDrawRect(-7.3f, -20.3f, -4f, -20f)
        glEndList()
    }

    fun customRotatedObject2D(oXpos: Float, oYpos: Float, oWidth: Float, oHeight: Float, rotate: Float) {
        glTranslated((oXpos + oWidth / 2.0f).toDouble(), (oYpos + oHeight / 2.0f).toDouble(), 0.0)
        glRotated(rotate.toDouble(), 0.0, 0.0, 1.0)
        glTranslated((-oXpos - oWidth / 2.0f).toDouble(), (-oYpos - oHeight / 2.0f).toDouble(), 0.0)
    }
    fun drawTargetESP2D(
        x: Float,
        y: Float,
        color: Color,
        color2: Color,
        color3: Color,
        color4: Color,
        scale: Float,
        index: Int
    ) {
        var x = x
        var y = y
        val millis = System.currentTimeMillis() + index.toLong() * 400L
        val angle = MathHelper.clamp_double((sin(millis.toDouble() / 150.0) + 1.0) / 2.0 * 30.0, 0.0, 30.0)
        val scaled = MathHelper.clamp_double((sin(millis.toDouble() / 500.0) + 1.0) / 2.0, 0.8, 1.0)
        var rotate = MathHelper.clamp_double((sin(millis.toDouble() / 1000.0) + 1.0) / 2.0 * 360.0, 0.0, 360.0)
        val size = 128.0f * scale * scaled.toFloat()
        val x2 = (size / 2.0f.let { x -= it; x }) + size
        val y2 = (size / 2.0f.let { y -= it; y }) + size
        pushMatrix()
        customRotatedObject2D(
            x,
            y,
            size,
            size,
            (45.0 - (angle - 15.0).let { rotate += it; rotate }).toFloat()
        )
        glDisable(3008)
        depthMask(false)
        enableBlend()
        shadeModel(7425)
        tryBlendFuncSeparate(770, 1, 1, 0)
        drawESPImage(
            ResourceLocation("liquidbounce/rectangle.png"),
            x.toDouble(),
            y.toDouble(),
            x2.toDouble(),
            y2.toDouble(),
            color,
            color2,
            color3,
            color4
        )
        tryBlendFuncSeparate(770, 771, 1, 0)
        resetColor()
        shadeModel(7424)
        depthMask(true)
        glEnable(3008)
        popMatrix()
    }
    fun targetESPSPos(entity: EntityLivingBase): Vector2f? {
        val entityRenderer: EntityRenderer = mc.entityRenderer
        val partialTicks: Float = mc.timer.renderPartialTicks
        val scaleFactor = ScaledResolution(mc).getScaleFactor()
        val x: Double = interpolate(entity.prevPosX, entity.posX, partialTicks)
        val y: Double = interpolate(entity.prevPosY, entity.posY, partialTicks)
        val z: Double = interpolate(entity.prevPosZ, entity.posZ, partialTicks)
        val height = (entity.height / (if (entity.isChild()) 1.75f else 1.0f) / 2.0f).toDouble()
        val width = 0.0
        val aabb = AxisAlignedBB(x - 0.0, y, z - 0.0, x + 0.0, y + height, z + 0.0)
        val vectors: Array<Vector3d?> = arrayOf<Vector3d?>(
            Vector3d(aabb.minX, aabb.minY, aabb.minZ),
            Vector3d(aabb.minX, aabb.maxY, aabb.minZ),
            Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
            Vector3d(aabb.maxX, aabb.maxY, aabb.minZ),
            Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
            Vector3d(aabb.minX, aabb.maxY, aabb.maxZ),
            Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
            Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)
        )
        entityRenderer.setupCameraTransform(partialTicks, 0)
        var position: Vector4d? = null
        val vecs3 = vectors
        val vecLength = vectors.size
        for (vecI in 0 until vecLength) {
            var vector = vecs3[vecI]
            vector = project2D(
                scaleFactor,
                vector!!.x - mc.getRenderManager().viewerPosX,
                vector.y - mc.getRenderManager().viewerPosY,
                vector.z - mc.getRenderManager().viewerPosZ
            )
            if (vector == null || !(vector.z >= 0.0) || !(vector.z < 1.0)) continue
            if (position == null) {
                position = Vector4d(vector.x, vector.y, vector.z, 0.0)
            }
            position.x = min(vector.x, position.x)
            position.y = min(vector.y, position.y)
            position.z = max(vector.x, position.z)
            position.w = max(vector.y, position.w)
        }
        entityRenderer.setupOverlayRendering()
        if (position != null) {
            return Vector2f(position.x.toFloat(), position.y.toFloat())
        }
        return null
    }
    fun project2D(scaleFactor: Int, x: Double, y: Double, z: Double): Vector3d? {
        val viewport = GLAllocation.createDirectIntBuffer(16)
        val modelView = GLAllocation.createDirectFloatBuffer(16)
        val projection = GLAllocation.createDirectFloatBuffer(16)
        val vector = GLAllocation.createDirectFloatBuffer(4)
        glGetFloat(2982, modelView)
        glGetFloat(2983, projection)
        glGetInteger(2978, viewport)
        return if (GLU.gluProject(
                x.toFloat(),
                y.toFloat(),
                z.toFloat(),
                modelView,
                projection,
                viewport,
                vector
            )
        ) Vector3d(
            (vector.get(0) / scaleFactor.toFloat()).toDouble(),
            ((Display.getHeight().toFloat() - vector.get(1)) / scaleFactor.toFloat()).toDouble(),
            vector.get(2).toDouble()
        ) else null
    }
    private fun drawESPImage(
        resource: ResourceLocation?,
        x: Double,
        y: Double,
        x2: Double,
        y2: Double,
        c: Color,
        c2: Color,
        c3: Color,
        c4: Color
    ) {
        mc.getTextureManager().bindTexture(resource)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.getWorldRenderer()
        bufferbuilder.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR)
        bufferbuilder.pos(x, y2, 0.0).tex(0.0, 1.0).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha())
            .endVertex()
        bufferbuilder.pos(x2, y2, 0.0).tex(1.0, 1.0).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha())
            .endVertex()
        bufferbuilder.pos(x2, y, 0.0).tex(1.0, 0.0).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha())
            .endVertex()
        bufferbuilder.pos(x, y, 0.0).tex(0.0, 0.0).color(c4.getRed(), c4.getGreen(), c4.getBlue(), c4.getAlpha())
            .endVertex()
        shadeModel(7425)
        depthMask(false)
        tessellator.draw()
        depthMask(true)
        shadeModel(7424)
    }
    fun drawTargetCapsule(entity: Entity, rad: Double, shade: Boolean, color: Color) {
        glPushMatrix()
        glDisable(3553)
        glEnable(2848)
        glEnable(2832)
        glEnable(3042)
        glBlendFunc(770, 771)
        glHint(3154, 4354)
        glHint(3155, 4354)
        glHint(3153, 4354)
        glDepthMask(false)
        alphaFunc(516, 0.0f)
        if (shade) {
            glShadeModel(7425)
        }
        disableCull()
        glBegin(5)
        val mc = Minecraft.getMinecraft()
        val x: Double =
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks.toDouble() - mc.renderManager.renderPosX
        val y: Double =
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks.toDouble() - mc.renderManager.renderPosY + sin(
                System.currentTimeMillis().toDouble() / 200.0
            ) + 1.0
        val z: Double =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks.toDouble() - mc.renderManager.renderPosZ
        var i = 0.0f
        while (i.toDouble() < Math.PI * 2) {
            val vecX = x + rad * cos(i.toDouble())
            val vecZ = z + rad * sin(i.toDouble())
            val c = color
            if (shade) {
                glColor4f(
                    c.getRed().toFloat() / 255.0f,
                    c.getGreen().toFloat() / 255.0f,
                    c.getBlue().toFloat() / 255.0f,
                    0.0f
                )
                glVertex3d(vecX, y - cos(System.currentTimeMillis().toDouble() / 200.0) / 2.0, vecZ)
                glColor4f(
                    c.getRed().toFloat() / 255.0f,
                    c.getGreen().toFloat() / 255.0f,
                    c.getBlue().toFloat() / 255.0f,
                    c.getAlpha().toFloat() / 255.0f
                )
            }
            glVertex3d(vecX, y, vecZ)
            i += 0.09817477f
        }
        glEnd()
        if (shade) {
            glShadeModel(7424)
        }
        glDepthMask(true)
        glEnable(2929)
        alphaFunc(516, 0.1f)
        enableCull()
        glDisable(2848)
        glDisable(2848)
        glEnable(2832)
        glEnable(3553)
        glPopMatrix()
        glColor3f(255.0f, 255.0f, 255.0f)
    }

    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 0xFF) / 255.0f
        val f2 = (col1 shr 16 and 0xFF) / 255.0f
        val f3 = (col1 shr 8 and 0xFF) / 255.0f
        val f4 = (col1 and 0xFF) / 255.0f
        val f5 = (col2 shr 24 and 0xFF) / 255.0f
        val f6 = (col2 shr 16 and 0xFF) / 255.0f
        val f7 = (col2 shr 8 and 0xFF) / 255.0f
        val f8 = (col2 and 0xFF) / 255.0f
        glEnable(3042)
        glDisable(3553)
        glBlendFunc(770, 771)
        glEnable(2848)
        glShadeModel(7425)
        glPushMatrix()
        glBegin(7)
        glColor4f(f2, f3, f4, f)
        glVertex2d(left, top)
        glVertex2d(left, bottom)
        glColor4f(f6, f7, f8, f5)
        glVertex2d(right, bottom)
        glVertex2d(right, top)
        glEnd()
        glPopMatrix()
        glEnable(3553)
        glDisable(3042)
        glDisable(2848)
        glShadeModel(7424)
    }
    fun setGLCap(cap: Int, flag: Boolean) {
        glCapMap[cap] = glGetBoolean(cap)
        if (flag) {
            glEnable(cap)
        } else {
            glDisable(cap)
        }
    }
    private fun revertGLCap(cap: Int) {
        val origCap: Boolean = glCapMap[cap] == true
        if (origCap) {
            glEnable(cap)
        } else {
            glDisable(cap)
        }
    }

    fun revertAllCaps() {
        val localIterator: Iterator<*> = glCapMap.keys.iterator()
        while (localIterator.hasNext()) {
            val cap = localIterator.next() as Int
            revertGLCap(cap)
        }
    }
    fun drawRoundedGradientOutlineCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        width: Float,
        radius: Float,
        color: Int,
        color2: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        ColorUtils.setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0f
        y *= 2.0f
        x1 *= 2.0f
        y1 *= 2.0f
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        ColorUtils.setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        var i: Int
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        ColorUtils.setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        ColorUtils.setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        ColorUtils.setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y + radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        glEnd()
        glLineWidth(1f)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()


        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        ColorUtils.setColour(-1)
    }
    fun drawRoundedGradientRectCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        radius: Float,
        color: Int,
        color2: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        x1 *= 2.0.toFloat()
        y1 *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        var i: Int
        i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y + radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()


        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        setColour(-1)
    }
    fun drawBlockBox(blockPos: BlockPos, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager

        val (x, y, z) = blockPos.toVec() - renderManager.renderPos

        var axisAlignedBB = AxisAlignedBB.fromBounds(x, y, z, x + 1.0, y + 1.0, z + 1.0)

        getBlock(blockPos)?.let { block ->
            val player = mc.thePlayer

            val (x, y, z) = player.interpolatedPosition(player.lastTickPos)

            val f = 0.002F.toDouble()

            block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)

            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(f, f, f).offset(-x, -y, -z)
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color.red, color.green, color.blue, if (color.alpha != 255) color.alpha else if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)

        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL_LINE_SMOOTH)
            glColor(color)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        resetColor()
        glDepthMask(true)
        resetCaps()
    }
    fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        // Lower Rectangle
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
    }

    fun drawEntityBox(entity: Entity, color: Color, outline: Boolean) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)

        val (x, y, z) = entity.interpolatedPosition(entity.lastTickPos) - mc.renderManager.renderPos
        val entityBox = entity.hitBox

        val axisAlignedBB = AxisAlignedBB.fromBounds(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )

        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawPosBox(x: Double, y: Double, z: Double, width: Float, height: Float, color: Color, outline: Boolean) {
        val (adjustedX, adjustedY, adjustedZ) = Vec3(x, y, z) - mc.renderManager.renderPos

        val axisAlignedBB = AxisAlignedBB.fromBounds(
            adjustedX - width / 2, adjustedY, adjustedZ - width / 2,
            adjustedX + width / 2, adjustedY + height, adjustedZ + width / 2
        )

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)

        glDepthMask(false)

        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)

        resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawBacktrackBox(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(2f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color.red, color.green, color.blue, 90)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun drawAxisAlignedBB(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(2f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun drawPlatform(y: Double, color: Color, size: Double) {
        val renderY = y - mc.renderManager.renderPosY
        drawAxisAlignedBB(AxisAlignedBB.fromBounds(size, renderY + 0.02, size, -size, renderY, -size), color)
    }
    fun loadGlTexture(bufferedImage: BufferedImage): Int {
        val textureId = glGenTextures()

        glBindTexture(GL_TEXTURE_2D, textureId)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        glTexImage2D(
            GL_TEXTURE_2D, 0, GL_RGBA, bufferedImage.width, bufferedImage.height,
            0, GL_RGBA, GL_UNSIGNED_BYTE, ImageUtils.readImageToBuffer(bufferedImage)
        )

        glBindTexture(GL_TEXTURE_2D, 0)

        return textureId
    }
    fun drawPlatform(entity: Entity, color: Color) {
        val (x, y, z) = entity.interpolatedPosition(entity.lastTickPos) - mc.renderManager.renderPos
        val axisAlignedBB = entity.entityBoundingBox.offset(-entity.posX, -entity.posY, -entity.posZ).offset(x, y, z)

        drawAxisAlignedBB(
            AxisAlignedBB.fromBounds(
                axisAlignedBB.minX,
                axisAlignedBB.maxY + 0.2,
                axisAlignedBB.minZ,
                axisAlignedBB.maxX,
                axisAlignedBB.maxY + 0.26,
                axisAlignedBB.maxZ
            ), color
        )
    }
    fun drawFilledBox(box: AxisAlignedBB, red: Float, green: Float, blue: Float, alpha: Float) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.worldRenderer

        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.color(red, green, blue, alpha)

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        buffer.pos(box.minX, box.minY, box.minZ).endVertex()
        buffer.pos(box.minX, box.maxY, box.minZ).endVertex()
        buffer.pos(box.maxX, box.minY, box.minZ).endVertex()
        buffer.pos(box.maxX, box.maxY, box.minZ).endVertex()

        tessellator.draw()

        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }

    fun drawFilledBox(axisAlignedBB: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) = drawRect(x, y, x2, y2, color.rgb)

    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, borderColor: Int, rectColor: Int) {
        drawRect(x, y, x2, y2, rectColor)
        drawBorder(x, y, x2, y2, width, borderColor)
    }

    fun drawBorderedRect(x: Int, y: Int, x2: Int, y2: Int, width: Int, borderColor: Int, rectColor: Int) {
        drawRect(x, y, x2, y2, rectColor)
        drawBorder(x, y, x2, y2, width, borderColor)
    }

    fun drawRoundedBorderRect(
        x: Float,
        y: Float,
        x2: Float,
        y2: Float,
        width: Float,
        color1: Int,
        color2: Int,
        radius: Float
    ) {
        drawRoundedRect(x, y, x2, y2, color1, radius)
        drawRoundedBorder(x, y, x2, y2, width, color2, radius)
    }

    fun drawRoundedBorderRectInt(
        x: Int,
        y: Int,
        x2: Int,
        y2: Int,
        width: Int,
        color1: Int,
        color2: Int,
        radius: Float
    ) {
        drawRoundedRectInt(x, y, x2, y2, color1, radius)
        drawRoundedBorderInt(x, y, x2, y2, width.toFloat(), color2, radius)
    }

    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawBorder(x: Int, y: Int, x2: Int, y2: Int, width: Int, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glLineWidth(width.toFloat())
        glBegin(GL_LINE_LOOP)
        glVertex2i(x2, y)
        glVertex2i(x, y)
        glVertex2i(x, y2)
        glVertex2i(x2, y2)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawRoundedBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int, radius: Float) {
        drawRoundedBordered(x, y, x2, y2, color, width, radius)
    }

    fun drawRoundedBorderInt(x: Int, y: Int, x2: Int, y2: Int, width: Float, color: Int, radius: Float) {
        drawRoundedBordered(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), color, width, radius)
    }

    fun drawRoundedBordered(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: Int,
        width: Float,
        radius: Float,
        bottom: Boolean = true
    ) {
        val segments = 18
        val step = 90.0 / segments

        val alpha = (color ushr 24 and 0xFF) / 255.0f
        val red = (color ushr 16 and 0xFF) / 255.0f
        val green = (color ushr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(width)

        glColor4f(red, green, blue, alpha)

        if (bottom) glBegin(GL_LINE_LOOP) else glBegin(GL_LINE_STRIP)

        val radiusD = radius.toDouble()

        val corners = listOf(
            Triple(newX2 - radiusD, newY2 - radiusD, 0.0),
            Triple(newX2 - radiusD, newY1 + radiusD, 90.0),
            Triple(newX1 + radiusD, newY1 + radiusD, 180.0),
            Triple(newX1 + radiusD, newY2 - radiusD, 270.0)
        )

        for ((cx, cy, startAngle) in corners) {
            for (i in 0..segments) {  // 修改循环条件
                val angle = Math.toRadians(startAngle + i * step)  // 使用新的步长
                val x = cx + radiusD * sin(angle)
                val y = cy + radiusD * cos(angle)
                glVertex2d(x, y)
            }
        }

        glEnd()

        resetColor()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
    }

    fun drawRoundedBorderedWithoutBottom(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: Int,
        width: Float,
        radius: Float
    ) = drawRoundedBordered(x1, y1, x2, y2, color, width, radius, false)

    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float) {
        glBegin(GL_QUADS)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2f(x2, y)
        glVertex2f(x, y)
        glVertex2f(x, y2)
        glVertex2f(x2, y2)
        glEnd()
        glColor4f(1f, 1f, 1f, 1f)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun drawRect(x: Int, y: Int, x2: Int, y2: Int, color: Int) {
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2i(x2, y)
        glVertex2i(x, y)
        glVertex2i(x, y2)
        glVertex2i(x2, y2)
        glEnd()
        glColor4f(1f, 1f, 1f, 1f)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    /**
     * Like [.drawRect], but without setup
     */
    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glPushMatrix()
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
        glColor4f(1f, 1f, 1f, 1f)
        glPopMatrix()
    }

    fun quickDrawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        quickDrawRect(x, y, x2, y2, color2)
        glColor(color1)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
    }

    fun drawLoadingCircle(x: Float, y: Float) {
        for (i in 0..3) {
            val rot = (System.nanoTime() / 5000000 * i % 360).toInt()
            drawCircle(x, y, (i * 10).toFloat(), rot - 180, rot)
        }
    }

    fun drawRoundedRect(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, radius: Float) {
        val alpha = (color ushr 24 and 0xFF) / 255.0f
        val red = (color ushr 16 and 0xFF) / 255.0f
        val green = (color ushr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius)
    }

    fun drawRoundedRect2(x1: Float, y1: Float, x2: Float, y2: Float, color: Color, radius: Float) {
        val alpha = color.alpha / 255.0f
        val red = color.red / 255.0f
        val green = color.green / 255.0f
        val blue = color.blue / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius)
    }

    fun drawRoundedRect3(x1: Float, y1: Float, x2: Float, y2: Float, color: Float, radius: Float) {
        val intColor = color.toInt()
        val alpha = (intColor ushr 24 and 0xFF) / 255.0f
        val red = (intColor ushr 16 and 0xFF) / 255.0f
        val green = (intColor ushr 8 and 0xFF) / 255.0f
        val blue = (intColor and 0xFF) / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius)
    }


    fun drawRoundedRectInt(x1: Int, y1: Int, x2: Int, y2: Int, color: Int, radius: Float) {
        val alpha = (color ushr 24 and 0xFF) / 255.0f
        val red = (color ushr 16 and 0xFF) / 255.0f
        val green = (color ushr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius)
    }

    fun drawRoundedRectangle(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        radius: Float
    ) {
        val segments = 36
        val step = 90.0 / segments

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        glPushMatrix()
        glEnable(GL_BLEND)
        glEnable(GL_MULTISAMPLE)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_TRIANGLE_FAN)

        val radiusD = radius.toDouble()

        // Draw corners
        val corners = arrayOf(
            Triple(newX2 - radiusD, newY2 - radiusD, 0.0),
            Triple(newX2 - radiusD, newY1 + radiusD, 90.0),
            Triple(newX1 + radiusD, newY1 + radiusD, 180.0),
            Triple(newX1 + radiusD, newY2 - radiusD, 270.0)
        )

        for ((cx, cy, startAngle) in corners) {
            for (i in 0..segments) {  // 增加细分段数
                val angle = Math.toRadians(startAngle + i * step)
                val x = cx + radiusD * sin(angle)
                val y = cy + radiusD * cos(angle)
                glVertex2d(x, y)
            }
        }
        if (overrideRoundedRectShadow.get()){
            var pAlpha = 255F
            var rr = 0F
            var rg = 0F
            var rb = 0F
            if (overrideGlow.get()){
                pAlpha = alpha
                rr = red
                rg = green
                rb = blue
            }
            GlowUtils.drawGlow(x1, y1, max(x2,x1)-min(x2,x1), max(y2,y1)-min(y2,y1), overrideStrength.get(), Color(rr, rg, rb, pAlpha))
        }
        glEnd()

        resetColor()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glDisable(GL_MULTISAMPLE)
        glPopMatrix()
    }
    private fun quickPolygonCircle(x: Float, y: Float, xRadius: Float, yRadius: Float, start: Int, end: Int) {
        var i = end
        while (i >= start) {
            glVertex2d(x + sin(i * Math.PI / 180.0) * xRadius, y + cos(i * Math.PI / 180.0) * yRadius)
            i -= 2  // 从4度步长改为2度步长
        }
    }
    fun orderPoints(x1: Float, y1: Float, x2: Float, y2: Float): FloatArray {
        val newX1 = min(x1, x2)
        val newY1 = min(y1, y2)
        val newX2 = max(x1, x2)
        val newY2 = max(y1, y2)
        return floatArrayOf(newX1, newY1, newX2, newY2)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor(Color.WHITE)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(2f)
        glBegin(GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            val rad = i.toRadians()
            glVertex2f(
                x + cos(rad) * (radius * 1.001f),
                y + sin(rad) * (radius * 1.001f)
            )
            i -= 360 / 90f
        }
        glEnd()
        glDisable(GL_LINE_SMOOTH)
        enableTexture2D()
        disableBlend()
    }

    fun drawFilledCircle(xx: Int, yy: Int, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float
        glPushAttrib(GL_ENABLE_BIT)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)
        for (i in 0 until sections) {
            x = (radius * sin(i * dAngle)).toFloat()
            y = (radius * cos(i * dAngle)).toFloat()
            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            glVertex2f(xx + x, yy + y)
        }
        resetColor()
        glEnd()
        glPopAttrib()
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        GL14.glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(
            x.toFloat(),
            y.toFloat(),
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            width.toFloat(),
            height.toFloat()
        )
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glPopAttrib()
        glPopMatrix()
    }
    fun drawImage(
        image: ResourceLocation?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Color = Color.WHITE
    ) {
        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        GL14.glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f
        )

        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(
            x.toFloat(),
            y.toFloat(),
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            width.toFloat(),
            height.toFloat()
        )

        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glPopAttrib()
        glPopMatrix()
    }
    /**
     * Draws a textured rectangle at z = 0. Args: x, y, u, v, width, height, textureWidth, textureHeight
     */
    fun drawModalRectWithCustomSizedTexture(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        width: Float,
        height: Float,
        textureWidth: Float,
        textureHeight: Float
    ) {
        val f = 1f / textureWidth
        val f1 = 1f / textureHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height.
     */
    fun drawTexturedModalRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int, zLevel: Float) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), zLevel.toDouble())
            .tex((textureX.toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble())
            .endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), (textureY.toFloat() * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), zLevel.toDouble())
            .tex((textureX.toFloat() * f).toDouble(), (textureY.toFloat() * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) =
        glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f)

    fun glColor(color: Color) = glColor(color.red, color.green, color.blue, color.alpha)

    fun glColor(hex: Int) =
        glColor(hex shr 16 and 0xFF, hex shr 8 and 0xFF, hex and 0xFF, hex shr 24 and 0xFF)

    fun draw2D(entity: EntityLivingBase, posX: Double, posY: Double, posZ: Double, color: Int, backgroundColor: Int) {
        glPushMatrix()
        glTranslated(posX, posY, posZ)
        glRotated(-mc.renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
        glScaled(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        glTranslated(0.0, 21 + -(entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 12, 0.0)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glColor4f(1f, 1f, 1f, 1f)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    fun draw2D(blockPos: BlockPos, color: Int, backgroundColor: Int) {
        val renderManager = mc.renderManager
        val (x, y, z) = blockPos.getVec().offset(EnumFacing.DOWN, 0.5) - renderManager.renderPos
        glPushMatrix()
        glTranslated(x, y, z)
        glRotated(-renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
        glScaled(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        glTranslated(0.0, 9.0, 0.0)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glColor4f(1f, 1f, 1f, 1f)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    fun renderNameTag(string: String, x: Double, y: Double, z: Double) {
        val renderManager = mc.renderManager
        val (x, y, z) = Vec3(x, y, z) - renderManager.renderPos

        glPushMatrix()
        glTranslated(x, y, z)
        glNormal3f(0f, 1f, 0f)
        glRotatef(-renderManager.playerViewY, 0f, 1f, 0f)
        glRotatef(renderManager.playerViewX, 1f, 0f, 0f)
        glScalef(-0.05f, -0.05f, 0.05f)
        setGlCap(GL_LIGHTING, false)
        setGlCap(GL_DEPTH_TEST, false)
        setGlCap(GL_BLEND, true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        val width = Fonts.font35.getStringWidth(string) / 2
        drawRect(-width - 1, -1, width + 1, Fonts.font35.FONT_HEIGHT, Int.MIN_VALUE)
        Fonts.font35.drawString(string, -width.toFloat(), 1.5f, Color.WHITE.rgb, true)
        resetCaps()
        resetColor()
        glPopMatrix()
    }

    fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glVertex2d(x, y)
        glVertex2d(x1, y1)
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    fun makeScissorBox(x: Float, y: Float, x2: Float, y2: Float) {
        val scaledResolution = ScaledResolution(mc)
        val factor = scaledResolution.scaleFactor
        glScissor(
            (x * factor).toInt(),
            ((scaledResolution.scaledHeight - y2) * factor).toInt(),
            ((x2 - x) * factor).toInt(),
            ((y2 - y) * factor).toInt()
        )
    }
    @JvmStatic
    fun drawShadow(x: Float, y: Float, width: Float, height: Float) {
        drawTexturedRect(x - 9, y - 9, 9f, 9f, "paneltopleft")
        drawTexturedRect(x - 9, y + height, 9f, 9f, "panelbottomleft")
        drawTexturedRect(x + width, y + height, 9f, 9f, "panelbottomright")
        drawTexturedRect(x + width, y - 9, 9f, 9f, "paneltopright")
        drawTexturedRect(x - 9, y, 9f, height, "panelleft")
        drawTexturedRect(x + width, y, 9f, height, "panelright")
        drawTexturedRect(x, y - 9, width, 9f, "paneltop")
        drawTexturedRect(x, y + height, width, 9f, "panelbottom")
    }

    fun drawTexturedRect(x: Float, y: Float, width: Float, height: Float, image: String) {
        glPushMatrix()
        val enableBlend = glIsEnabled(GL_BLEND)
        val disableAlpha = !glIsEnabled(GL_ALPHA_TEST)
        if (!enableBlend) glEnable(GL_BLEND)
        if (!disableAlpha) glDisable(GL_ALPHA_TEST)
        mc.textureManager.bindTexture(ResourceLocation("liquidbounce/ui/shadow/$image.png"))
        color(1f, 1f, 1f, 1f)
        drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width, height)
        if (!enableBlend) glDisable(GL_BLEND)
        if (!disableAlpha) glEnable(GL_ALPHA_TEST)
        glPopMatrix()
    }
    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float) {
        glBegin(GL_POLYGON)

        val xRadius = min((x1 - x) * 0.5, radius.toDouble()).toFloat()
        val yRadius = min((y1 - y) * 0.5, radius.toDouble()).toFloat()
        quickPolygonCircle(x + xRadius, y + yRadius, xRadius, yRadius, 180, 270)
        quickPolygonCircle(x1 - xRadius, y + yRadius, xRadius, yRadius, 90, 180)
        quickPolygonCircle(x1 - xRadius, y1 - yRadius, xRadius, yRadius, 0, 90)
        quickPolygonCircle(x + xRadius, y1 - yRadius, xRadius, yRadius, 270, 360)

        glEnd()
    }
    fun customRotatedObject2D(oXpos: Float, oYpos: Float, oWidth: Float, oHeight: Float, rotate: Double) {
        translate((oXpos + oWidth / 2).toDouble(), (oYpos + oHeight / 2).toDouble(), 0.0)
        glRotated(rotate, 0.0, 0.0, 1.0)
        translate(-(oXpos + oWidth / 2).toDouble(), -(oYpos + oHeight / 2).toDouble(), 0.0)
    }
    @JvmStatic
    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float, color: Int) {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        val hasCull = glIsEnabled(GL_CULL_FACE)
        glDisable(GL_CULL_FACE)

        glColor(color)
        drawRoundedCornerRect(x, y, x1, y1, radius)

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        setGlState(GL_CULL_FACE, hasCull)
    }
    /**
     * GL CAP MANAGER
     *
     *
     * TODO: Remove gl cap manager and replace by something better
     */

    fun resetCaps() = glCapMap.forEach { (cap, state) -> setGlState(cap, state) }

    fun enableGlCap(cap: Int) = setGlCap(cap, true)

    fun enableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, true)
    }

    fun disableGlCap(cap: Int) = setGlCap(cap, true)

    fun disableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, false)
    }

    fun setGlCap(cap: Int, state: Boolean) {
        glCapMap[cap] = glGetBoolean(cap)
        setGlState(cap, state)
    }

    fun setGlState(cap: Int, state: Boolean) = if (state) glEnable(cap) else glDisable(cap)

    fun drawScaledCustomSizeModalRect(
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float
    ) {
        val f = 1f / tileWidth
        val f1 = 1f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldRenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }
}