/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles

import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI.scale
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui.clamp
import net.ccbluex.liquidbounce.ui.client.clickgui.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ButtonElement
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement
import net.ccbluex.liquidbounce.ui.client.clickgui.style.Style
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts.font35
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.component2
import net.ccbluex.liquidbounce.utils.extensions.lerpWith
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFilledCircle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.awt.Color
import kotlin.math.abs
import kotlin.math.roundToInt

@SideOnly(Side.CLIENT)
object BlackStyle : Style() {

    // 优化面板背景颜色和边框
    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel) {
        val panelColor = Color(30, 30, 30, 200) // 增加透明度
        val borderColor = Color(40, 40, 40, 200)

        drawRect(panel.x.toFloat(), panel.y - 3f, panel.x + panel.width.toFloat(), panel.y + 17f, panelColor.rgb)
        drawRect(
            panel.x.toFloat(), panel.y + 17f, panel.x + panel.width.toFloat(),
            panel.y + 24f + panel.fade, panelColor.rgb
        )

        if (panel.fade > 0) {
            drawBorderedRect(
                panel.x,
                panel.y + 17,
                panel.x + panel.width,
                panel.y + 19 + panel.fade,
                2, // 减小边框宽度
                borderColor.rgb,
                borderColor.rgb
            )
            drawBorderedRect(
                panel.x,
                panel.y + 17 + panel.fade,
                panel.x + panel.width,
                panel.y + 24 + panel.fade,
                2,
                borderColor.darker().rgb,
                borderColor.darker().rgb
            )
        }

        // 优化面板标题位置和颜色
        val titleColor = if (panel.isHovered(mouseX, mouseY)) Color(255, 255, 255) else Color(200, 200, 200)
        val xPos = panel.x + (panel.width - font35.getStringWidth(panel.name)) / 2
        font35.drawString(panel.name, xPos, panel.y + 4, titleColor.rgb)
    }

    // 优化悬停提示框样式
    override fun drawHoverText(mouseX: Int, mouseY: Int, text: String) {
        val lines = text.lines()
        val width = lines.maxOfOrNull { font35.getStringWidth(it) + 14 } ?: return
        val height = (font35.fontHeight * lines.size) + 3

        val (scaledWidth, scaledHeight) = ScaledResolution(mc)
        val x = mouseX.clamp(0, (scaledWidth / scale - width).roundToInt())
        val y = mouseY.clamp(0, (scaledHeight / scale - height).roundToInt())

        // 使用渐变背景
        drawGradientRect(
            x + 9, y, x + width, y + height,
            Color(50, 50, 50, 220).rgb,
            Color(30, 30, 30, 220).rgb
        )

        // 添加边框
        drawBorderedRect(
            x + 9, y, x + width, y + height, 1,
            Color(80, 80, 80).rgb,
            Color(80, 80, 80).rgb
        )

        lines.forEachIndexed { index, line ->
            font35.drawString(line, x + 12, y + 3 + (font35.fontHeight) * index, Color.WHITE.rgb)
        }
    }

    // 优化按钮元素样式
    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement) {
        val bgColor = if (buttonElement.isHovered(mouseX, mouseY))
            Color(40, 40, 40, 200)
        else
            Color(20, 20, 20, 200)

        drawRect(
            buttonElement.x - 1,
            buttonElement.y - 1,
            buttonElement.x + buttonElement.width + 1,
            buttonElement.y + buttonElement.height + 1,
            bgColor.rgb
        )

        // 添加按钮文字阴影效果
        val textColor = if (buttonElement.isHovered(mouseX, mouseY))
            Color(255, 255, 255)
        else
            Color(200, 200, 200)

        font35.drawStringWithShadow(
            buttonElement.displayName,
            buttonElement.x + 5F,
            buttonElement.y + 5F,
            textColor.rgb
        )
    }

    override fun drawModuleElementAndClick(
        mouseX: Int,
        mouseY: Int,
        moduleElement: ModuleElement,
        mouseButton: Int?
    ): Boolean {
        drawRect(
            moduleElement.x - 1,
            moduleElement.y - 1,
            moduleElement.x + moduleElement.width + 1,
            moduleElement.y + moduleElement.height + 1,
            getHoverColor(Color(40, 40, 40), moduleElement.hoverTime)
        )
        drawRect(
            moduleElement.x - 1,
            moduleElement.y - 1,
            moduleElement.x + moduleElement.width + 1,
            moduleElement.y + moduleElement.height + 1,
            getHoverColor(
                Color(20, 20, 20, moduleElement.slowlyFade),
                moduleElement.hoverTime,
                !moduleElement.module.isActive
            )
        )

        font35.drawString(
            moduleElement.displayName, moduleElement.x + 5, moduleElement.y + 5,
            if (moduleElement.module.state && !moduleElement.module.isActive) Color(255, 255, 255, 128).rgb
            else Color.WHITE.rgb
        )

        // Draw settings
        val moduleValues = moduleElement.module.values.filter { it.shouldRender() }
        if (moduleValues.isNotEmpty()) {
            font35.drawString(
                if (moduleElement.showSettings) "<" else ">",
                moduleElement.x + moduleElement.width - 8,
                moduleElement.y + 5,
                Color.WHITE.rgb
            )

            if (moduleElement.showSettings) {
                var yPos = moduleElement.y + 6

                val minX = moduleElement.x + moduleElement.width + 4
                val maxX = moduleElement.x + moduleElement.width + moduleElement.settingsWidth

                if (moduleElement.settingsWidth > 0 && moduleElement.settingsHeight > 0) drawBorderedRect(
                    minX,
                    yPos,
                    maxX,
                    yPos + moduleElement.settingsHeight,
                    3,
                    Color(20, 20, 20).rgb,
                    Color(40, 40, 40).rgb
                )

                for (value in moduleValues) {
                    assumeNonVolatile = value.get() is Number

                    when (value) {
                        is BoolValue -> {
                            val text = value.name

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + 12) {
                                value.toggle()
                                clickSound()
                                return true
                            }

                            font35.drawString(
                                text, minX + 2, yPos + 2, if (value.get()) Color.WHITE.rgb else Int.MAX_VALUE
                            )

                            yPos += 11
                        }

                        is ListValue -> {
                            val text = value.name

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 16

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + font35.fontHeight) {
                                value.openList = !value.openList
                                clickSound()
                                return true
                            }

                            font35.drawString(text, minX + 2, yPos + 2, Color.WHITE.rgb)
                            font35.drawString(
                                if (value.openList) "-" else "+",
                                (maxX - if (value.openList) 5 else 6),
                                yPos + 2,
                                Color.WHITE.rgb
                            )

                            yPos += font35.fontHeight + 1

                            for (valueOfList in value.values) {
                                moduleElement.settingsWidth = font35.getStringWidth("> $valueOfList") + 12

                                if (value.openList) {
                                    if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + 9) {
                                        value.set(valueOfList)
                                        clickSound()
                                        return true
                                    }

                                    font35.drawString(
                                        "> $valueOfList",
                                        minX + 2,
                                        yPos + 2,
                                        if (value.get() == valueOfList) Color.WHITE.rgb else Int.MAX_VALUE
                                    )

                                    yPos += font35.fontHeight + 1
                                }
                            }
                            if (!value.openList) {
                                yPos += 1
                            }
                        }

                        is FloatValue -> {
                            val text = value.name + "§f: " + round(value.get())

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue =
                                (x + width * (displayValue - value.minimum) / (value.maximum - value.minimum)).roundToInt()

                            if ((mouseButton == 0 || sliderValueHeld == value)
                                && mouseX in minX..maxX
                                && mouseY in yPos + 15..yPos + 21
                            ) {
                                val percentage = (mouseX - x) / width.toFloat()
                                value.set(
                                    round(value.minimum + (value.maximum - value.minimum) * percentage).coerceIn(
                                        value.range
                                    )
                                )

                                // Keep changing this slider until mouse is unpressed.
                                sliderValueHeld = value

                                // Stop rendering and interacting only when this event was triggered by a mouse click.
                                if (mouseButton == 0) return true
                            }

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(x, y, sliderValue, y + 2, color.rgb)
                            drawFilledCircle(sliderValue, y + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 3, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is IntegerValue -> {
                            val text =
                                value.name + "§f: " + if (value is BlockValue) getBlockName(value.get()) + " (" + value.get() + ")" else value.get()

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue =
                                x + width * (displayValue - value.minimum) / (value.maximum - value.minimum)

                            if ((mouseButton == 0 || sliderValueHeld == value) && mouseX in x..x + width && mouseY in y - 2..y + 5) {
                                val percentage = (mouseX - x) / width.toFloat()
                                value.set(
                                    (value.minimum + (value.maximum - value.minimum) * percentage).roundToInt()
                                        .coerceIn(value.range)
                                )

                                // Keep changing this slider until mouse is unpressed.
                                sliderValueHeld = value

                                // Stop rendering and interacting only when this event was triggered by a mouse click.
                                if (mouseButton == 0) return true
                            }

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(x, y, sliderValue, y + 2, color.rgb)
                            drawFilledCircle(sliderValue, y + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 3, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is IntegerRangeValue -> {
                            val slider1 = value.get().first
                            val slider2 = value.get().last

                            val text = "${value.name}§f: $slider1 - $slider2 (Beta)"
                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            if ((mouseButton == 0 || sliderValueHeld == value) && mouseX in x..x + width && mouseY in y - 2..y + 5) {
                                val slider1Pos =
                                    minX + ((slider1 - value.minimum).toFloat() / (value.maximum - value.minimum)) * (maxX - minX)
                                val slider2Pos =
                                    minX + ((slider2 - value.minimum).toFloat() / (value.maximum - value.minimum)) * (maxX - minX)

                                val distToSlider1 = mouseX - slider1Pos
                                val distToSlider2 = mouseX - slider2Pos

                                val percentage = (mouseX - minX - 4F) / (maxX - minX - 8F)

                                if (abs(distToSlider1) <= abs(distToSlider2) && distToSlider2 <= 0) {
                                    value.setFirst(value.lerpWith(percentage).coerceIn(value.minimum, slider2))
                                } else value.setLast(value.lerpWith(percentage).coerceIn(slider1, value.maximum))

                                // Keep changing this slider until mouse is unpressed.
                                sliderValueHeld = value

                                // Stop rendering and interacting only when this event was triggered by a mouse click.
                                if (mouseButton == 0) return true
                            }

                            val displayValue1 = value.get().first
                            val displayValue2 = value.get().last

                            val sliderValue1 =
                                x + width * (displayValue1 - value.minimum) / (value.maximum - value.minimum)
                            val sliderValue2 =
                                x + width * (displayValue2 - value.minimum) / (value.maximum - value.minimum)

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(sliderValue1, y, sliderValue2, y + 2, color.rgb)
                            drawFilledCircle(sliderValue1, y + 1, 3f, color)
                            drawFilledCircle(sliderValue2, y + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is FloatRangeValue -> {
                            val slider1 = value.get().start
                            val slider2 = value.get().endInclusive

                            val text = "${value.name}§f: ${round(slider1)} - ${round(slider2)} (Beta)"
                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4f
                            val y = yPos + 14f
                            val width = moduleElement.settingsWidth - 12f
                            val color = Color(20, 20, 20)

                            if ((mouseButton == 0 || sliderValueHeld == value) && mouseX.toFloat() in x..x + width && mouseY.toFloat() in y - 2..y + 5) {
                                val slider1Pos =
                                    minX + ((slider1 - value.minimum).toFloat() / (value.maximum - value.minimum)) * (maxX - minX)
                                val slider2Pos =
                                    minX + ((slider2 - value.minimum).toFloat() / (value.maximum - value.minimum)) * (maxX - minX)

                                val distToSlider1 = mouseX - slider1Pos
                                val distToSlider2 = mouseX - slider2Pos

                                val percentage = (mouseX - minX - 4F) / (maxX - minX - 8F)

                                if (abs(distToSlider1) <= abs(distToSlider2) && distToSlider2 <= 0) {
                                    value.setFirst(value.lerpWith(percentage).coerceIn(value.minimum, slider2))
                                } else value.setLast(value.lerpWith(percentage).coerceIn(slider1, value.maximum))

                                // Keep changing this slider until mouse is unpressed.
                                sliderValueHeld = value

                                // Stop rendering and interacting only when this event was triggered by a mouse click.
                                if (mouseButton == 0) return true
                            }

                            val displayValue1 = value.get().start
                            val displayValue2 = value.get().endInclusive

                            val sliderValue1 =
                                x + width * (displayValue1 - value.minimum) / (value.maximum - value.minimum)
                            val sliderValue2 =
                                x + width * (displayValue2 - value.minimum) / (value.maximum - value.minimum)

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(sliderValue1, y, sliderValue2, y + 2, color.rgb)
                            drawFilledCircle(sliderValue1.roundToInt(), y.roundToInt() + 1, 3f, color)
                            drawFilledCircle(sliderValue2.roundToInt(), y.roundToInt() + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is FontValue -> {
                            val displayString = value.displayName
                            moduleElement.settingsWidth = font35.getStringWidth(displayString) + 8

                            font35.drawString(displayString, minX + 2, yPos + 2, Color.WHITE.rgb)

                            if (mouseButton != null && mouseX in minX..maxX && mouseY in yPos..yPos + 12) {
                                // Cycle to next font when left-clicked, previous when right-clicked.
                                if (mouseButton == 0) value.next()
                                else value.previous()
                                clickSound()
                                return true
                            }

                            yPos += 11
                        }

                        else -> {
                            val text = value.name + "§f: " + value.get()

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 12
                        }
                    }
                }

                moduleElement.settingsHeight = yPos - moduleElement.y - 6

                if (mouseButton != null && mouseX in minX..maxX && mouseY in moduleElement.y + 6..yPos + 2) return true
            }
        }

        return false
    }

    // 添加新的工具方法
    private fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, startColor: Int, endColor: Int) {
        val startColorObj = Color(startColor, true)
        val endColorObj = Color(endColor, true)

        // 获取颜色的RGB分量
        val startRed = startColorObj.red
        val startGreen = startColorObj.green
        val startBlue = startColorObj.blue
        val startAlpha = startColorObj.alpha

        val endRed = endColorObj.red
        val endGreen = endColorObj.green
        val endBlue = endColorObj.blue
        val endAlpha = endColorObj.alpha

        // 计算颜色差值
        val deltaRed = endRed - startRed
        val deltaGreen = endGreen - startGreen
        val deltaBlue = endBlue - startBlue
        val deltaAlpha = endAlpha - startAlpha

        // 绘制渐变矩形
        for (i in left until right) {
            val ratio = (i - left).toFloat() / (right - left)
            val color = Color(
                (startRed + deltaRed * ratio).toInt(),
                (startGreen + deltaGreen * ratio).toInt(),
                (startBlue + deltaBlue * ratio).toInt(),
                (startAlpha + deltaAlpha * ratio).toInt()
            )
            drawRect(i, top, i + 1, bottom, color.rgb)
        }
    }


    private fun drawStringWithShadow(text: String, x: Int, y: Int, color: Int) {
        val shadowColor = Color(0, 0, 0, 100) // 阴影颜色
        val shadowOffset = 1 // 阴影偏移量

        // 绘制阴影
        font35.drawString(text, x + shadowOffset, y + shadowOffset, shadowColor.rgb)
        // 绘制文字
        font35.drawString(text, x, y, color)
    }

}