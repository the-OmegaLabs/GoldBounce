package net.ccbluex.liquidbounce.features.module.modules.render

// ... existing imports ...
import net.ccbluex.liquidbounce.LiquidBounce.eventManager
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU
import org.lwjgl.util.glu.Sphere

object DickESP : Module("DickESP", Category.RENDER, gameDetecting = false, hideModule = false) {
    private var spin = 0f
    private var cumSize = 0f
    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        for (o in mc.theWorld.loadedEntityList) {
            if (o is EntityPlayer) {
                val player = o
                val mc = Minecraft.getMinecraft()
                val RenderManager = mc.renderManager
                if (player == mc.thePlayer) {
                    continue
                }
                val n = player.lastTickPosX + (player.posX - player.lastTickPosX) * mc.timer.elapsedTicks
                mc.getRenderManager()
                val x = n - RenderManager.renderPosX
                val n2 = player.lastTickPosY + (player.posY - player.lastTickPosY) * mc.timer.elapsedTicks
                mc.getRenderManager()
                val y = n2 - RenderManager.renderPosY
                val n3 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * mc.timer.elapsedTicks
                mc.getRenderManager()
                val z = n3 - RenderManager.renderPosZ
                GL11.glPushMatrix()
                RenderHelper.disableStandardItemLighting()
                this.esp(player, x, y, z)
                RenderHelper.enableStandardItemLighting()
                GL11.glPopMatrix()
            }
        }


    }

    private fun esp(player: EntityPlayer, x: Double, y: Double, z: Double) {
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glTranslated(x, y, z)
        GL11.glRotatef(-player.rotationYaw, 0f, player.height, 0f)
        GL11.glTranslated(-x, -y, -z)
        GL11.glTranslated(x, y + player.height / 2.0f - 0.22499999403953552, z)
        GL11.glColor4f(1.38f, 0.55f, 2.38f, 1.0f)
        GL11.glRotatef((if (player.isSneaking) 35 else 0) + spin, 1.0f + spin, 0f, cumSize)
        Cylinder().apply {
            drawStyle = GLU.GLU_SMOOTH
            draw(0.1f, 0.11f, 0.4f, 25, 20)
        }
        GL11.glColor4f(1.38f, 0.85f, 1.38f, 1.0f)
        GL11.glTranslated(-0.09, 0.0, -0.125)
        Sphere().apply {
            drawStyle = GLU.GLU_SMOOTH
            draw(0.14f, 10, 20)
        }
        
        GL11.glTranslated(0.16, 0.0, 0.0)
        Sphere().apply {
            drawStyle = GLU.GLU_SMOOTH
            draw(0.14f, 10, 20)
        }
        GL11.glColor4f(1.35f, 0.0f, 0.0f, 1.0f)
        GL11.glTranslated(-0.07, 0.0, 0.59)
        Sphere().apply {
            drawStyle = GLU.GLU_SMOOTH
            draw(0.13f, 15, 20)
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LIGHTING)
    }
}