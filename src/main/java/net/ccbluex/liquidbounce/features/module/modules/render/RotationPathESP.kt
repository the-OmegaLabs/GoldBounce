package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.Vec3d
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.float
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11

object RotationPathESP : Module("RotationPathESP", Category.RENDER) {

    private val showTarget by boolean("ShowTarget", true)
    private val showLine by boolean("ShowLine", true)
    private val alpha by float("Alpha", 1f, 0f..1f)
    private val lineWidth by float("LineWidth", 2f, 0.5f..5f)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val rotation = RotationUtils.targetRotation ?: return
        val player = mc.thePlayer ?: return

        val eyePos = Vec3d(player.posX, player.posY + player.eyeHeight, player.posZ)
        fun Vec3.toVec3d() = Vec3d(this.xCoord, this.yCoord, this.zCoord)
        val targetVec = eyePos.add(RotationUtils.getVectorForRotation(rotation).toVec3d().scale(50.0))

        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        if (showLine) {
            GL11.glLineWidth(lineWidth)
            GL11.glBegin(GL11.GL_LINES)
            GL11.glColor4f(1f, 0f, 0f, alpha)
            GL11.glVertex3d(
                eyePos.x - mc.renderManager.renderPosX,
                eyePos.y - mc.renderManager.renderPosY,
                eyePos.z - mc.renderManager.renderPosZ
            )
            GL11.glVertex3d(
                targetVec.x - mc.renderManager.renderPosX,
                targetVec.y - mc.renderManager.renderPosY,
                targetVec.z - mc.renderManager.renderPosZ
            )
            GL11.glEnd()
        }

        if (showTarget) {
            val box = AxisAlignedBB(
                targetVec.x - 0.1, targetVec.y - 0.1, targetVec.z - 0.1,
                targetVec.x + 0.1, targetVec.y + 0.1, targetVec.z + 0.1
            ).offset(
                -mc.renderManager.renderPosX,
                -mc.renderManager.renderPosY,
                -mc.renderManager.renderPosZ
            )

            RenderUtils.drawFilledBox(box)
        }

        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()
        GlStateManager.popMatrix()
    }
}
