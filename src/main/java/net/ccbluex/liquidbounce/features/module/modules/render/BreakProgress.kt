package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object BreakProgress : Module("BreakProgress", Category.RENDER, hideModule = false) {
    val colorRainbow by boolean("Rainbow", false)
    val colorRed by int("R", 255, 0..255) { !colorRainbow }
    val colorGreen by int("G", 179, 0..255) { !colorRainbow }
    val colorBlue by int("B", 72, 0..255) { !colorRainbow }

    private var breakProgress: Float = 0f
    private var targetBlockPos: BlockPos? = null

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val color = if (colorRainbow) ColorUtils.rainbow() else Color(colorRed, colorGreen, colorBlue)

        if (breakProgress > 0f && targetBlockPos != null) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_BLEND)
            glDisable(GL_DEPTH_TEST)

            mc.entityRenderer.disableLightmap()

            // Set the position where the progress bar will appear
            mc.renderManager.viewerPosX
            mc.renderManager.viewerPosY
            mc.renderManager.viewerPosZ

            val blockPos = targetBlockPos!!

            // Calculate the coordinates for each face of the block
            for (facing in EnumFacing.values()) {
                val coordinates = getFaceCoordinates(blockPos, facing)

                val x1 = coordinates[0]
                val y1 = coordinates[1]
                val z1 = coordinates[2]
                val x2 = coordinates[3]
                val y2 = coordinates[4]
                val z2 = coordinates[5]

                // Render progress bar on each face
                glBegin(GL_QUADS)
                RenderUtils.glColor(color) // Using RenderUtils.glColor for color setting

                // The width of the progress bar on each face is determined by the progress value
                val barLength = breakProgress * (x2 - x1) // The progress bar length based on the progress value
                when (facing) {
                    EnumFacing.UP, EnumFacing.DOWN -> {
                        // Render top or bottom faces
                        glVertex3d(x1, y1, z1)
                        glVertex3d(x1 + barLength, y1, z1)
                        glVertex3d(x1 + barLength, y2, z2)
                        glVertex3d(x1, y2, z2)
                    }
                    EnumFacing.NORTH, EnumFacing.SOUTH -> {
                        // Render north or south faces
                        glVertex3d(x1, y1, z1)
                        glVertex3d(x1 + barLength, y1, z1)
                        glVertex3d(x1 + barLength, y1, z2)
                        glVertex3d(x1, y1, z2)
                    }
                    EnumFacing.WEST, EnumFacing.EAST -> {
                        // Render west or east faces
                        glVertex3d(x1, y1, z1)
                        glVertex3d(x1, y1, z1 + barLength)
                        glVertex3d(x2, y1, z1 + barLength)
                        glVertex3d(x2, y1, z1)
                    }
                }

                glColor4d(1.0, 1.0, 1.0, 1.0)
                glEnd()
            }

            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)

            glPopMatrix()
        }
    }

    private fun getFaceCoordinates(blockPos: BlockPos, facing: EnumFacing): List<Double> {
        val x = blockPos.x.toDouble()
        val y = blockPos.y.toDouble()
        val z = blockPos.z.toDouble()

        // Define coordinates based on the direction of the face
        return when (facing) {
            EnumFacing.UP -> listOf(x, y + 1.0, z, x + 1.0, y + 1.05, z + 1.0)
            EnumFacing.DOWN -> listOf(x, y - 0.05, z, x + 1.0, y, z + 1.0)
            EnumFacing.NORTH -> listOf(x, y, z - 0.05, x + 1.0, y + 1.0, z)
            EnumFacing.SOUTH -> listOf(x, y, z + 1.0, x + 1.0, y + 1.0, z + 0.95)
            EnumFacing.WEST -> listOf(x - 0.05, y, z, x, y + 1.0, z + 1.0)
            EnumFacing.EAST -> listOf(x + 1.0, y, z, x + 0.95, y + 1.0, z + 1.0)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer != null) {
            mc.thePlayer as EntityPlayerSP

            // Check if player is breaking a block
            val blockPos = mc.objectMouseOver?.blockPos
            if (blockPos != null) {
                // Track the block the player is breaking
                targetBlockPos = blockPos

                // Get progress of block breaking (using PlayerControllerMP.curBlockDamageMP)
                breakProgress = Minecraft.getMinecraft().playerController.curBlockDamageMP
            } else {
                breakProgress = 0f
                targetBlockPos = null
            }
        }
    }

    override fun onEnable() {
        breakProgress = 0f
        targetBlockPos = null
    }

    override fun onDisable() {
        breakProgress = 0f
        targetBlockPos = null
    }
}

