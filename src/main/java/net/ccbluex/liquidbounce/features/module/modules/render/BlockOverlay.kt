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
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFilledBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawSelectionBoundingBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.floatValue
import net.ccbluex.liquidbounce.value.intValue
import net.minecraft.block.Block
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object BlockOverlay : Module("BlockOverlay", Category.RENDER, gameDetecting = false, hideModule = false) {
    // Migrated Settings
    private val fillValue by _boolean("Fill", true)
    private val outlineValue by _boolean("Outline", true)
    private val animationValue by _boolean("Animation", true)
    private val depth3D by _boolean("Depth3D", false)
    private val outlineWidth by floatValue("OutlineWidth", 2F, 1F..10F)

    // Original Settings
    val info by _boolean("Info", false)

    // Color Settings
    private val colorRainbow by _boolean("Rainbow", false)
    private val colorRed by intValue("R", 68, 0..255) { !colorRainbow }
    private val colorGreen by intValue("G", 117, 0..255) { !colorRainbow }
    private val colorBlue by intValue("B", 255, 0..255) { !colorRainbow }
    private val fillAlpha by floatValue("FillAlpha", 0.3F, 0F..1F)
    private val outlineAlpha by floatValue("OutlineAlpha", 1F, 0F..1F)

    // Animation Fields
    private var currentBB: AxisAlignedBB? = null
    private var slideBB: AxisAlignedBB? = null
    private val smoothAnimations = Array(6) { SmoothAnimation(0.0F) }

    // Minimal helper class for smooth animation, as seen in the provided code
    private class SmoothAnimation(var value: Float) {
        fun setAnimation(target: Float, speed: Double) {
            value += ((target - value) / speed).toFloat()
        }
    }

    val currentBlock: BlockPos?
        get() {
            val blockPos = mc.objectMouseOver?.blockPos ?: return null

            if (canBeClicked(blockPos) && mc.theWorld.worldBorder.contains(blockPos))
                return blockPos

            return null
        }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val blockPos = currentBlock ?: run {
            // Reset animation state when not looking at a block
            currentBB = null
            slideBB = null
            return
        }

        val block = getBlock(blockPos) ?: return
        val thePlayer = mc.thePlayer ?: return

        block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)
        val selectedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)

        val expandedBB = if (animationValue) {
            if (selectedBB != currentBB) {
                if (currentBB != null) { // Only set slideBB if there was a previous block
                    slideBB = currentBB
                }
                currentBB = selectedBB
            }

            val current = currentBB
            val slide = slideBB

            // Animate only if we have a start (slide) and end (current) point
            if (slide != null && current != null) {
                smoothAnimations[0].setAnimation(current.minX.toFloat(), 24.0)
                smoothAnimations[1].setAnimation(current.minY.toFloat(), 24.0)
                smoothAnimations[2].setAnimation(current.minZ.toFloat(), 24.0)
                smoothAnimations[3].setAnimation(current.maxX.toFloat(), 24.0)
                smoothAnimations[4].setAnimation(current.maxY.toFloat(), 24.0)
                smoothAnimations[5].setAnimation(current.maxZ.toFloat(), 24.0)

                AxisAlignedBB(
                    smoothAnimations[0].value.toDouble(), smoothAnimations[1].value.toDouble(), smoothAnimations[2].value.toDouble(),
                    smoothAnimations[3].value.toDouble(), smoothAnimations[4].value.toDouble(), smoothAnimations[5].value.toDouble()
                ).expand(0.01, 0.01, 0.01)
            } else {
                selectedBB.expand(0.002, 0.002, 0.002) // Fallback if animation can't run yet
            }
        } else {
            selectedBB.expand(0.002, 0.002, 0.002)
        }

        val (x, y, z) = thePlayer.interpolatedPosition(thePlayer.lastTickPos)
        val finalBB = expandedBB.offset(-x, -y, -z)

        val baseColor = if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glDisable(GL_TEXTURE_2D)
        if (depth3D) glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        // Draw Fill
        if (fillValue) {
            val fillColor = Color(baseColor.red, baseColor.green, baseColor.blue, (fillAlpha * 255).toInt())
            glColor(fillColor)
            drawFilledBox(finalBB)
        }

        // Draw Outline
        if (outlineValue) {
            val outlineColor = Color(baseColor.red, baseColor.green, baseColor.blue, (outlineAlpha * 255).toInt())
            glLineWidth(outlineWidth)
            glColor(outlineColor)
            drawSelectionBoundingBox(finalBB)
        }

        if (depth3D) glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDepthMask(true)
        glLineWidth(1F) // Reset line width to default
        resetColor()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (info) {
            val blockPos = currentBlock ?: return
            val block = getBlock(blockPos) ?: return

            val info = "${block.localizedName} §7ID: ${Block.getIdFromBlock(block)}"
            val (width, height) = ScaledResolution(mc)

            drawBorderedRect(
                width / 2 - 2F,
                height / 2 + 5F,
                width / 2 + Fonts.font40.getStringWidth(info) + 2F,
                height / 2 + 16F,
                3F, Color.BLACK.rgb, Color.BLACK.rgb
            )

            resetColor()
            Fonts.font40.drawString(info, width / 2f, height / 2f + 7f, Color.WHITE.rgb, false)
        }
    }
}