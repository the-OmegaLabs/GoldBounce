/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.float
import net.ccbluex.liquidbounce.value.int
import org.lwjgl.opengl.GL11.*

object NLCrosshair : Module("NLCrosshair", Category.RENDER) {

    // Settings
    private val thickness by float("Thickness", 1F, 0.1F..5F)
    private val length by int("Length", 5, 1..20)
    private val outline by boolean("Outline", false)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val width = mc.displayWidth / 2F
        val height = mc.displayHeight / 2F
        glPushMatrix()
        glTranslated(width.toDouble(), height.toDouble(), 0.0)  // Convert to Double
        glLineWidth(thickness)

        // Draw crosshair with white color
        glBegin(GL_LINES)
        glColor4d(1.0, 1.0, 1.0, 1.0) // White color
        glVertex2d(-length.toDouble(), 0.0)  // Convert to Double
        glVertex2d(length.toDouble(), 0.0)  // Convert to Double
        glVertex2d(0.0, -length.toDouble())  // Convert to Double
        glVertex2d(0.0, length.toDouble())  // Convert to Double
        glEnd()

        // Draw outline with white color
        if (outline) {
            glLineWidth(thickness + 1F)
            glBegin(GL_LINE_LOOP)
            glColor4d(1.0, 1.0, 1.0, 1.0) // White color
            glVertex2d(-length.toDouble(), 0.0)  // Convert to Double
            glVertex2d(length.toDouble(), 0.0)  // Convert to Double
            glVertex2d(0.0, -length.toDouble())  // Convert to Double
            glVertex2d(0.0, length.toDouble())  // Convert to Double
            glEnd()
        }

        glPopMatrix()
    }

}
