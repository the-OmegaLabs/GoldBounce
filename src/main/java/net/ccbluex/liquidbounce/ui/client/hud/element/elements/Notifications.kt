/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
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
@ElementInfo(name = "Notifications", single = true)
class Notifications(x: Double = 0.0, y: Double = 30.0, scale: Float = 1F,
                    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    private val initialY = 30.0 // Initial Y coordinate
    private val exampleNotification = Notification("Example Notification")

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        val notifications = HUD.notifications.toList() // Create a copy of the list to avoid concurrent modification issues

        if (notifications.isEmpty()) {
            // Reset Y coordinate if there are no notifications
            this.y = initialY
            return null
        }

        for ((index, notification) in notifications.withIndex()) {
            notification.drawNotification(index * 25F) // Offset each notification vertically
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (exampleNotification !in HUD.notifications)
                addNotification(exampleNotification)

            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.x = exampleNotification.textLength + 8F

            return Border(-95F, -20F * notifications.size, 0F, 0F)
        }

        return Border(-95F, -20F * notifications.size, 0F, 0F)
    }

}

// Default delay set to 60F
class Notification(private val message: String, private val delay: Float = 60F) {
    var x = 0F
    var textLength = 0

    private var stay = 0F
    private var fadeStep = 0F
    var fadeState = FadeState.IN

    /**
     * Fade state for animation
     */
    enum class FadeState { IN, STAY, OUT, END }

    init {
        textLength = Fonts.font35.getStringWidth(message)
    }

    /**
     * Draw notification
     */
    fun drawNotification(offsetY: Float) {
        resetColor()
        glColor4f(1f, 1f, 1f, 1f)

        drawRect(-x + 8 + textLength, offsetY, -x, offsetY - 20F, Color.BLACK.rgb)
        drawRect(-x, offsetY, -x - 5, offsetY - 20F, Color(255, 255, 0).rgb)
        Fonts.font35.drawString(message, -x + 4, offsetY - 14F, Int.MAX_VALUE)
        // Animation
        val delta = deltaTimet(fadeStep, width) * width
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

            FadeState.END -> HUD.removeNotification(this)
        }
    }
}
