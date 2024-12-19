package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.minecraft.client.renderer.GlStateManager.resetColor
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "NewNotifications", single = true)
class NewNotifications(x: Double = 0.0, y: Double = 30.0, scale: Float = 1F,
                    side: Side = Side(Side.Horizontal.LEFT, Side.Vertical.UP)) : Element(x, y, scale, side) {

    private val initialY = 30.0 // Initial Y coordinate

    /** Draw element */
    override fun drawElement(): Border? {
        val notifications = HUD.notifications2.toList() // Create a copy of the list to avoid concurrent modification issues

        if (notifications.isEmpty()) {
            // Reset Y coordinate if there are no notifications
            this.y = initialY
            return null
        }

        for ((index, notification) in notifications.withIndex()) {
            notification.drawNotification(index * 40F) // Increased vertical offset for larger notifications
        }

        if (mc.currentScreen is GuiHudDesigner) {
            // Example notification, if not in the list, add it.
            val exampleNotification = NewNotification("Example New Notification", Color(255, 255, 0))
            if (exampleNotification !in HUD.notifications2) {
                HUD.addNewNotification(exampleNotification)
            }
        }

        return Border(-100F, -40F * notifications.size, 0F, 0F) // Adjusted for new height
    }

}

// Default delay set to 60F
class NewNotification(private val message: String, private val color: Color, private val delay: Float = 60F) {
    var x = 0F
    var textLength = 0

    private var stay = 0F
    private var fadeStep = 0F
    var fadeState = FadeState.IN

    /** Fade state for animation */
    enum class FadeState { IN, STAY, OUT, END }

    init {
        textLength = Fonts.font35.getStringWidth(message)
    }

    /** Draw notification */
    fun drawNotification(offsetY: Float) {
        resetColor()
        glColor4f(1f, 1f, 1f, 1f)

        // Draw notification background with customizable color
        drawRect(x, offsetY, x + textLength + 10F, offsetY + 30F, color.rgb) // Increased height for better visibility

        // Draw message
        Fonts.font35.drawString(message, x + 5F, offsetY + 20F, Int.MAX_VALUE)

        // Draw progress bar
        val progress = (delay - stay) / delay
        drawRect(x, offsetY + 30F, x + textLength + 10F, offsetY + 32F, Color(0, 0, 0, 100).rgb) // Background
        drawRect(x, offsetY + 30F, x + (textLength + 10F) * progress, offsetY + 32F, Color(0, 255, 0).rgb) // Progress bar

        // Animation
        val delta = deltaTime
        val width = textLength + 8F

        when (fadeState) {
            FadeState.IN -> {
                if (x < width) {
                    x = AnimationUtils.easeOut(fadeStep, width) * width
                    fadeStep += delta / 4F
                }
                if (x >= width) {
                    fadeState = FadeState.STAY
                    x = width
                    fadeStep = width
                }

                stay = delay
            }

            FadeState.STAY -> if (stay > 0)
                stay -= delta
            else
                fadeState = FadeState.OUT

            FadeState.OUT -> if (x > 0) {
                x = AnimationUtils.easeOut(fadeStep, width) * width
                fadeStep -= delta / 4F
            } else
                fadeState = FadeState.END

            FadeState.END -> HUD.removeNewNotification(this)
        }
    }
}
