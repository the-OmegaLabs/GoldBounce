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
import net.ccbluex.liquidbounce.utils.render.RenderUtils
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
    companion object {
        private const val MAX_LINE_WIDTH = 200
        private const val LINE_HEIGHT = 12
        private const val PADDING = 8
        private const val CORNER_RADIUS = 4
    }
    var x = 0F
    var textLength = 0
    var lines = listOf<String>()
    var height = 0F
    var width = 0F

    private var stay = 0F
    private var fadeStep = 0F
    var fadeState = FadeState.IN

    /**
     * Fade state for animation
     */
    enum class FadeState { IN, STAY, OUT, END }

    init {
        processMessage()
    }

    /**
     * Draw notification
     */
    private fun processMessage() {
            val words = message.split(" ")
            val currentLine = StringBuilder()
            lines = mutableListOf()

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val testWidth = Fonts.font35.getStringWidth(testLine)

                if (testWidth > MAX_LINE_WIDTH && currentLine.isNotEmpty()) {
                    (lines as MutableList).add(currentLine.toString())
                    currentLine.clear().append(word)
                } else {
                    currentLine.append(if (currentLine.isEmpty()) word else " $word")
                }
            }
            if (currentLine.isNotEmpty()) {
                (lines as MutableList).add(currentLine.toString())
            }

            // 计算尺寸
            width = (lines.maxOf { Fonts.font35.getStringWidth(it) } + PADDING * 2).toFloat()
            height = (lines.size * LINE_HEIGHT + PADDING).toFloat()
        }
fun drawNotification(offsetY: Float) {
            val alpha = when (fadeState) {
                FadeState.IN -> fadeStep / width
                FadeState.OUT -> 1 - fadeStep / width
                else -> 1F
            }.coerceIn(0F, 1F)

            // 背景
            RenderUtils.drawRoundedRect(
                -x, offsetY - height,
                -x + width, offsetY,
                CORNER_RADIUS,
                Color(40, 40, 40, (200 * alpha).toInt()).rgb.toFloat()
            )

            // 进度条
            val progressWidth = if (fadeState == FadeState.STAY)
                width * (stay / delay) else width
            RenderUtils.drawRoundedRect(
                -x, offsetY - 2,
                -x + progressWidth, offsetY,
                1,
                Color(255, 204, 0, (220 * alpha).toInt()).rgb.toFloat()
            )

            // 文字
            lines.forEachIndexed { index, line ->
                Fonts.font35.drawString(
                    line,
                    -x + PADDING,
                    offsetY - height + PADDING + index * LINE_HEIGHT,
                    Color(255, 255, 255, (255 * alpha).toInt()).rgb,
                    false
                )
            }

            // 动画逻辑
            val delta = deltaTime
            when (fadeState) {
                FadeState.IN -> {
                    x = AnimationUtils.easeOut(fadeStep, width) * width
                    fadeStep += delta / 4F
                    if (x >= width) {
                        fadeState = FadeState.STAY
                        stay = delay
                    }
                }
                FadeState.STAY -> {
                    stay -= delta
                    if (stay <= 0) fadeState = FadeState.OUT
                }
                FadeState.OUT -> {
                    x = AnimationUtils.easeOut(fadeStep, width) * width
                    fadeStep -= delta / 4F
                    if (x <= 0) fadeState = FadeState.END
                }
                else -> {}
            }
        }
    }
