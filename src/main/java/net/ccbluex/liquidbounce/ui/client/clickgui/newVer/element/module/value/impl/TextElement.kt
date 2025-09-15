package net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module.value.impl

import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.ColorManager
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module.value.ValueElement
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MouseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.TextValue
import org.lwjgl.input.Keyboard
import java.awt.Color

class TextElement(val savedValue: TextValue) : ValueElement<String>(savedValue) {

    private var text: String = savedValue.get()
    private var focused = false
    private var cursorVisible = true
    private var lastBlink = System.currentTimeMillis()
    private var cursorPos = text.length

    override fun drawElement(
        mouseX: Int,
        mouseY: Int,
        x: Float,
        y: Float,
        width: Float,
        bgColor: Color,
        accentColor: Color
    ): Float {
        // Label
        Fonts.font40.drawString(
            value.name,
            x + 10F,
            y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F,
            -1
        )

        val boxX = (x + width - 120F)
        val boxY = y + 2F
        val boxW = 110F
        val boxH = 16F

        // 背景
        RenderUtils.drawRoundedRect(
            boxX,
            boxY,
            boxX + boxW,
            boxY + boxH,
            ColorManager.textBox.rgb,
            3F,
        )

        // 聚焦高亮
        if (focused) {
            RenderUtils.drawRoundedRect(
                boxX,
                boxY + boxH - 1,
                boxX + boxW,
                boxY + boxH,
                accentColor.rgb,
                2F,
            )
        }

        // 文字
        val displayText = if (text.isEmpty() && !focused) "Enter text..." else text
        val textColor = if (text.isEmpty() && !focused) Color.GRAY.rgb else -1

        Fonts.font35.drawString(
            displayText,
            boxX + 5,
            boxY + (boxH - Fonts.font35.FONT_HEIGHT) / 2F,
            textColor
        )

        // 光标闪烁
        if (focused) {
            val now = System.currentTimeMillis()
            if (now - lastBlink > 500) {
                cursorVisible = !cursorVisible
                lastBlink = now
            }
            if (cursorVisible) {
                val cursorX = boxX + 5 + Fonts.font35.getStringWidth(text.substring(0, cursorPos))
                RenderUtils.drawRoundedRect(
                    cursorX.toFloat(),
                    boxY + 3,
                    cursorX + 1,
                    boxY + boxH - 3,
                    -1,
                    0.1F
                )
            }
        }

        return valueHeight
    }

    override fun onClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        val boxX = (x + width - 120F)
        val boxY = y + 2F
        val boxW = 110F
        val boxH = 16F

        focused = MouseUtils.mouseWithinBounds(
            mouseX,
            mouseY,
            boxX,
            boxY,
            boxX + boxW,
            boxY + boxH
        )

        if (!focused) {
            savedValue.set(text)
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {}

    override fun onKeyPress(typedChar: Char, keyCode: Int): Boolean {
        if (!focused) return false

        when (keyCode) {
            Keyboard.KEY_BACK -> {
                if (cursorPos > 0) {
                    text = text.removeRange(cursorPos - 1, cursorPos)
                    cursorPos--
                }
            }
            Keyboard.KEY_DELETE -> {
                if (cursorPos < text.length) {
                    text = text.removeRange(cursorPos, cursorPos + 1)
                }
            }
            Keyboard.KEY_LEFT -> if (cursorPos > 0) cursorPos--
            Keyboard.KEY_RIGHT -> if (cursorPos < text.length) cursorPos++
            Keyboard.KEY_HOME -> cursorPos = 0
            Keyboard.KEY_END -> cursorPos = text.length
            Keyboard.KEY_RETURN -> {
                savedValue.set(text)
                focused = false
            }
            else -> {
                if (typedChar.isLetterOrDigit() || typedChar.isWhitespace() || typedChar in "._-") {
                    text = text.substring(0, cursorPos) + typedChar + text.substring(cursorPos)
                    cursorPos++
                }
            }
        }
        return true
    }

    fun isTyping(): Boolean = focused
}
