package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.Fonts.getFont
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.value.float
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.renderer.GlStateManager.resetColor
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.sqrt

/**
 * CustomHUD player speed element
 *
 * Displays the player's speed in Blocks per second (BPS).
 */
@ElementInfo(name = "SpeedBPS")
class SpeedBPS(
    x: Double = 75.0, y: Double = 110.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    private val speedMultiplier by float("Speed Multiplier", 1F, 0.1F..10F) // Adjusts the speed display scale
    private val colorRed by int("R", 255, 0..255)
    private val colorGreen by int("G", 0, 0..255)
    private val colorBlue by int("B", 0, 0..255)

    private var lastTick = -1
    private var lastPosX = 0.0
    private var lastPosZ = 0.0

    override fun drawElement(): Border {
        AWTFontRenderer.Companion.assumeNonVolatile = true

        val player = mc.thePlayer
        var speed = 0.0

        if (lastTick != player.ticksExisted) {
            lastTick = player.ticksExisted

            // Calculate the movement delta (change in position)
            val deltaX = player.posX - lastPosX
            val deltaZ = player.posZ - lastPosZ

            // Calculate speed (distance / time) - in blocks per second
            speed = sqrt(deltaX * deltaX + deltaZ * deltaZ)

            // Update the last positions for next tick calculation
            lastPosX = player.posX
            lastPosZ = player.posZ
        }

        // Convert speed to Blocks per second (BPS)
        val speedBPS = speed * speedMultiplier

        // Format the speed text
        val speedText = String.format("Speed: %.2f BPS", speedBPS)
        val fontRenderer = Fonts.font40
        val textWidth = fontRenderer.getStringWidth(speedText)
        val textHeight = fontRenderer.height

        // Set up rendering
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(2F)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        // Draw the speed text on the screen
        glColor(Color(colorRed, colorGreen, colorBlue, 255))
        fontRenderer.drawString(speedText, x.toInt(), y.toInt(), Color(255, 255, 255, 255).rgb)

        // Reset OpenGL settings
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)

        assumeNonVolatile = false

        resetColor()

        // Return the size of the element as a border (used for layout)
        return Border(0F, 0F, textWidth.toFloat(), textHeight.toFloat())
    }
}
