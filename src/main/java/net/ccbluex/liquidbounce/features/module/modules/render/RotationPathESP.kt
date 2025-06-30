package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.Vec3d
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.*
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

object RotationPathESP : Module("RotationPathESP", Category.RENDER) {

    // Basic settings
    private val showTarget by _boolean("ShowTarget", true)
    private val showLine by _boolean("ShowLine", true)
    private val lineWidth by floatValue("LineWidth", 2f, 0.5f..5f)
    private val lineColorR by floatValue("LineColorR", 1F,0F..1F)
    private val lineColorG by floatValue("LineColorG", 0F,0F..1F)
    private val lineColorB by floatValue("LineColorB", 0F,0F..1F)
    private val lineColorAlpha by floatValue("LineColorAlpha", 1F,0F..1F)
    private val lineColor = Color(lineColorR, lineColorG, lineColorB, lineColorAlpha)

    // Dot settings
    private val drawDots by _boolean("DrawDots", true)
    private val dotSpacing by floatValue("DotSpacing", 1f, 0.1f..5f)
    private val dotSize by floatValue("DotSize", 0.1f, 0.05f..0.5f)
    private val dotColorR by floatValue("DotColorR", 1F,0F..1F)
    private val dotColorG by floatValue("DotColorG", 0F,0F..1F)
    private val dotColorB by floatValue("DotColorB", 0F,0F..1F)
    private val dotColorAlpha by floatValue("DotColorAlpha", 1F,0F..1F)
    private val dotColor = Color(dotColorR, dotColorG, dotColorB, dotColorAlpha)

    // Indicator settings
    private val offScreenIndicator by _boolean("OffScreenIndicator", true)
    private val indicatorSize by floatValue("IndicatorSize", 8f, 4f..20f)
    private val indicatorColorR by floatValue("IndicatorColorR", 1F,0F..1F)
    private val indicatorColorG by floatValue("IndicatorColorG", 0F,0F..1F)
    private val indicatorColorB by floatValue("IndicatorColorB", 0F,0F..1F)
    private val indicatorColorAlpha by floatValue("IndicatorColorAlpha", 1F,0F..1F)

    private val indicatorColor = Color(indicatorColorR, indicatorColorG, indicatorColorB, indicatorColorAlpha)

    // Hit detection
    private val detectHit by _boolean("DetectHit", true)
    private val hitBoxSize by floatValue("HitBoxSize", 0.2f, 0.1f..1f)
    private val hitColorR by floatValue("HitColorR", 1F,0F..1F)
    private val hitColorG by floatValue("HitColorG", 0F,0F..1F)
    private val hitColorB by floatValue("HitColorB", 0F,0F..1F)
    private val hitColorAlpha by floatValue("HitColorAlpha", 1F,0F..1F)
    private val hitColor = Color(hitColorR, hitColorG, hitColorB, hitColorAlpha)

    // Alpha settings
    private val alpha by floatValue("Alpha", 0.8f, 0f..1f)

    private var lastTarget: Vec3d? = null
    private var hitPosition: Vec3d? = null
    @Suppress("NOTHING_TO_INLINE")
    inline fun Vec3.scale(factor: Double) = Vec3(
        xCoord * factor,
        yCoord * factor,
        zCoord * factor
    )
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val rotation = RotationUtils.targetRotation ?: return
        val player = mc.thePlayer ?: return

        val eyePos = Vec3d(player.posX, player.posY + player.eyeHeight, player.posZ)
        val targetVec = eyePos.add(
            Vec3d(
                RotationUtils.getVectorForRotation(rotation).xCoord * 50.0,
                RotationUtils.getVectorForRotation(rotation).yCoord * 50.0,
                RotationUtils.getVectorForRotation(rotation).zCoord * 50.0
            )
        )
        lastTarget = targetVec
        hitPosition = null

        // Perform collision detection
        if (detectHit) {
            detectCollision(eyePos, targetVec)
        }

        GlStateManager.pushMatrix()
        prepareGL()

        // Draw main path
        if (showLine) {
            drawPathLine(eyePos, targetVec)
        }

        // Draw dots along path
        if (drawDots) {
            drawPathDots(eyePos, targetVec)
        }

        // Draw hit box
        hitPosition?.let { drawHitBox(it) }

        // Draw target box
        if (showTarget) {
            drawTargetBox(targetVec)
        }

        restoreGL()
        GlStateManager.popMatrix()
    }

    private fun detectCollision(start: Vec3d, end: Vec3d) {
        val world = mc.theWorld ?: return
        var closestHit: MovingObjectPosition? = null

        // Check entities
        for (entity in world.loadedEntityList) {
            if (entity == mc.thePlayer || !entity.isEntityAlive) continue
            val bb = entity.entityBoundingBox ?: continue
            val intercept = bb.calculateIntercept(start.toVec3(), end.toVec3())

            intercept?.let {
                val hitVec = Vec3d(it.hitVec)
                val distance = hitVec.distanceTo(start)
                if (closestHit == null || distance < closestHit!!.hitVec.distanceTo(start.toVec3())) {
                    closestHit = MovingObjectPosition(entity, hitVec.toVec3())
                }
            }
        }

        // Check blocks
        val blockHit = world.rayTraceBlocks(start.toVec3(), end.toVec3(), false, true, false)
        if (blockHit != null) {
            val hitVec = Vec3d(blockHit.hitVec)
            val distance = hitVec.distanceTo(start)
            if (closestHit == null || distance < closestHit!!.hitVec.distanceTo(start.toVec3())) {
                closestHit = blockHit
            }
        }

        closestHit?.let {
            hitPosition = Vec3d(it.hitVec)
        }
    }

    private fun drawPathLine(start: Vec3d, end: Vec3d) {
        GL11.glLineWidth(lineWidth)
        GL11.glBegin(GL11.GL_LINES)
        applyColor(lineColor)
        GL11.glVertex3d(
            start.x - mc.renderManager.renderPosX,
            start.y - mc.renderManager.renderPosY,
            start.z - mc.renderManager.renderPosZ
        )
        GL11.glVertex3d(
            end.x - mc.renderManager.renderPosX,
            end.y - mc.renderManager.renderPosY,
            end.z - mc.renderManager.renderPosZ
        )
        GL11.glEnd()
    }

    private fun drawPathDots(start: Vec3d, end: Vec3d) {
        val distance = start.distanceTo(end)
        val steps = (distance / dotSpacing).toInt()
        val stepVec = end.subtract(start).normalize().scale(dotSpacing.toDouble())

        var current = start.add(stepVec)
        repeat(steps) {
            val box = AxisAlignedBB(
                current.x - dotSize/2, current.y - dotSize/2, current.z - dotSize/2,
                current.x + dotSize/2, current.y + dotSize/2, current.z + dotSize/2
            ).offset(
                -mc.renderManager.renderPosX,
                -mc.renderManager.renderPosY,
                -mc.renderManager.renderPosZ
            )
            RenderUtils.drawFilledBox(box, dotColorR,  dotColorG, dotColorB, dotColorAlpha)
            current = current.add(stepVec)
        }
    }

    private fun drawHitBox(pos: Vec3d) {
        val box = AxisAlignedBB(
            pos.x - hitBoxSize/2, pos.y - hitBoxSize/2, pos.z - hitBoxSize/2,
            pos.x + hitBoxSize/2, pos.y + hitBoxSize/2, pos.z + hitBoxSize/2
        ).offset(
            -mc.renderManager.renderPosX,
            -mc.renderManager.renderPosY,
            -mc.renderManager.renderPosZ
        )
        RenderUtils.drawFilledBox(box, hitColorR, hitColorG, hitColorB, hitColorAlpha)
    }

    private fun drawTargetBox(pos: Vec3d) {
        val box = AxisAlignedBB(
            pos.x - 0.1, pos.y - 0.1, pos.z - 0.1,
            pos.x + 0.1, pos.y + 0.1, pos.z + 0.1
        ).offset(
            -mc.renderManager.renderPosX,
            -mc.renderManager.renderPosY,
            -mc.renderManager.renderPosZ
        )
        RenderUtils.drawFilledBox(box)
    }

    private fun prepareGL() {
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
    }

    private fun restoreGL() {
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
    }

    private fun applyColor(color: Color) {
        GL11.glColor4f(
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f * alpha
        )
    }
}