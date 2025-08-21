/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ClientUtils.logger
import net.ccbluex.liquidbounce.utils.RotationUtils.isEntityHeightVisible
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.value.*
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.util.BlockPos
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

object ProphuntESP : Module("ProphuntESP", Category.RENDER, gameDetecting = false) {
    private val mode by choices("Mode", arrayOf("Box", "OtherBox", "Glow"), "OtherBox")
    private val glowRenderScale by floatValue("Glow-Renderscale", 1f, 0.5f..2f) { mode == "Glow" }
    private val glowRadius by intValue("Glow-Radius", 4, 1..5) { mode == "Glow" }
    private val glowFade by intValue("Glow-Fade", 10, 0..30) { mode == "Glow" }
    private val glowTargetAlpha by floatValue("Glow-Target-Alpha", 0f, 0f..1f) { mode == "Glow" }

    private val colorRainbow by _boolean("Rainbow", false)
    private val colorRed by intValue("R", 0, 0..255) { !colorRainbow }
    private val colorGreen by intValue("G", 90, 0..255) { !colorRainbow }
    private val colorBlue by intValue("B", 255, 0..255) { !colorRainbow }

    private val maxRenderDistance by object : IntegerValue("MaxRenderDistance", 50, 1..200) {
        override fun onUpdate(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
    }

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    private val onLook by _boolean("OnLook", false)
    private val maxAngleDifference by floatValue("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by _boolean("ThruBlocks", true)

    private val color
        get() = if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)

    private val blocks = ConcurrentHashMap<BlockPos, Long>()

    fun recordBlock(blockPos: BlockPos) {
        blocks[blockPos] = System.currentTimeMillis()
    }

    override fun onDisable() {
        blocks.clear()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (mode != "Box" && mode != "OtherBox") break
            if (entity !is EntityFallingBlock) continue
            if (onLook && !EntityUtils.isLookingOnEntities(entity, maxAngleDifference.toDouble())) continue
            if (!thruBlocks && !isEntityHeightVisible(entity)) continue
            val distanceSquared = mc.thePlayer.getDistanceSqToEntity(entity)

            if (distanceSquared <= maxRenderDistanceSq) {
                drawEntityBox(entity, color, mode == "Box")
            }
        }

        val now = System.currentTimeMillis()

        with(blocks.entries.iterator()) {
            while (hasNext()) {
                val (pos, time) = next()

                if (now - time > 2000L) {
                    remove()
                    continue
                }

                drawBlockBox(pos, color, mode == "Box")
            }
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.theWorld == null || mode != "Glow")
            return

        GlowShader.startDraw(event.partialTicks, glowRenderScale)

        for (entity in mc.theWorld.loadedEntityList) {
            val distanceSquared = mc.thePlayer.getDistanceSqToEntity(entity)

            if (distanceSquared <= maxRenderDistanceSq) {
                if (entity !is EntityFallingBlock) continue
                if (onLook && !EntityUtils.isLookingOnEntities(entity, maxAngleDifference.toDouble())) continue
                if (!thruBlocks && !isEntityHeightVisible(entity)) continue

                try {
                    mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                } catch (ex: Exception) {
                    logger.error("An error occurred while rendering all entities for shader esp", ex)
                }
            }
        }

        GlowShader.stopDraw(color, glowRadius, glowFade, glowTargetAlpha)
    }
}
