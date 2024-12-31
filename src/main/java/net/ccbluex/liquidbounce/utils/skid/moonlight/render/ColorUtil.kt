package net.ccbluex.liquidbounce.utils.skid.moonlight.render

import net.ccbluex.liquidbounce.utils.skid.fdpx.MathUtils.interpolateFloat
import net.ccbluex.liquidbounce.utils.skid.fdpx.MathUtils.interpolateInt
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

object ColorUtil {
    @JvmOverloads
    fun tripleColor(rgbValue: Int, alpha: Float = 1f): Color {
        var alpha = alpha
        alpha = min(1.0, max(0.0, alpha.toDouble())).toFloat()
        return Color(rgbValue, rgbValue, rgbValue, (255 * alpha).toInt())
    }

    fun getAnalogousColor(color: Color): Array<Color?> {
        val colors = arrayOfNulls<Color>(2)
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)

        val degree = 30 / 360f

        val newHueAdded = hsb[0] + degree
        colors[0] = Color(Color.HSBtoRGB(newHueAdded, hsb[1], hsb[2]))

        val newHueSubtracted = hsb[0] - degree

        colors[1] = Color(Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]))

        return colors
    }


    val randomColor: Color
        get() = Color(
            Color.HSBtoRGB(
                Math.random().toFloat(),
                (.5 + Math.random() / 2).toFloat(),
                (.5 + Math.random() / 2f).toFloat()
            )
        )

    //RGB TO HSL AND HSL TO RGB FOUND HERE: https://gist.github.com/mjackson/5311256
    fun hslToRGB(hsl: FloatArray): Color {
        var red: Float
        var green: Float
        var blue: Float

        if (hsl[1] == 0f) {
            blue = 1f
            green = blue
            red = green
        } else {
            val q = if (hsl[2] < .5) hsl[2] * (1 + hsl[1]) else hsl[2] + hsl[1] - hsl[2] * hsl[1]
            val p = 2 * hsl[2] - q

            red = hueToRGB(p, q, hsl[0] + 1 / 3f)
            green = hueToRGB(p, q, hsl[0])
            blue = hueToRGB(p, q, hsl[0] - 1 / 3f)
        }

        red *= 255f
        green *= 255f
        blue *= 255f

        return Color(red.toInt(), green.toInt(), blue.toInt())
    }


    fun hueToRGB(p: Float, q: Float, t: Float): Float {
        var newT = t
        if (newT < 0) newT += 1f
        if (newT > 1) newT -= 1f
        if (newT < 1 / 6f) return p + (q - p) * 6 * newT
        if (newT < .5f) return q
        if (newT < 2 / 3f) return p + (q - p) * (2 / 3f - newT) * 6
        return p
    }

    fun rgbToHSL(rgb: Color): FloatArray {
        val red = rgb.red / 255f
        val green = rgb.green / 255f
        val blue = rgb.blue / 255f

        val max = max(max(red.toDouble(), green.toDouble()), blue.toDouble()).toFloat()
        val min = min(min(red.toDouble(), green.toDouble()), blue.toDouble()).toFloat()
        val c = (max + min) / 2f
        val hsl = floatArrayOf(c, c, c)

        if (max == min) {
            hsl[1] = 0f
            hsl[0] = hsl[1]
        } else {
            val d = max - min
            hsl[1] = if (hsl[2] > .5) d / (2 - max - min) else d / (max + min)

            if (max == red) {
                hsl[0] = (green - blue) / d + (if (green < blue) 6 else 0)
            } else if (max == blue) {
                hsl[0] = (blue - red) / d + 2
            } else if (max == green) {
                hsl[0] = (red - green) / d + 4
            }
            hsl[0] /= 6f
        }
        return hsl
    }


    fun imitateTransparency(backgroundColor: Color, accentColor: Color, percentage: Float): Color {
        return Color(interpolateColor(backgroundColor, accentColor, (255 * percentage) / 255))
    }

    fun applyOpacity(color: Int, opacity: Float): Int {
        val old = Color(color)
        return applyOpacity(old, opacity).rgb
    }

    //Opacity value ranges from 0-1
    fun applyOpacity(color: Color, opacity: Float): Color {
        var opacity = opacity
        opacity = min(1.0, max(0.0, opacity.toDouble())).toFloat()
        return Color(color.red, color.green, color.blue, (color.alpha * opacity).toInt())
    }

    fun darker(color: Color, FACTOR: Float): Color {
        return Color(
            max((color.red * FACTOR).toInt().toDouble(), 0.0).toInt(),
            max((color.green * FACTOR).toInt().toDouble(), 0.0).toInt(),
            max((color.blue * FACTOR).toInt().toDouble(), 0.0).toInt(),
            color.alpha
        )
    }

    fun brighter(color: Color, FACTOR: Float): Color {
        var r = color.red
        var g = color.green
        var b = color.blue
        val alpha = color.alpha

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        val i = (1.0 / (1.0 - FACTOR)).toInt()
        if (r == 0 && g == 0 && b == 0) {
            return Color(i, i, i, alpha)
        }
        if (r > 0 && r < i) r = i
        if (g > 0 && g < i) g = i
        if (b > 0 && b < i) b = i

        return Color(
            min((r / FACTOR).toInt().toDouble(), 255.0).toInt(),
            min((g / FACTOR).toInt().toDouble(), 255.0).toInt(),
            min((b / FACTOR).toInt().toDouble(), 255.0).toInt(),
            alpha
        )
    }

    /**
     * This method gets the average color of an image
     * performance of this goes as O((width * height) / step)
     */
    fun averageColor(bi: BufferedImage, width: Int, height: Int, pixelStep: Int): Color {
        val color = IntArray(3)
        var x = 0
        while (x < width) {
            var y = 0
            while (y < height) {
                val pixel = Color(bi.getRGB(x, y))
                color[0] += pixel.red
                color[1] += pixel.green
                color[2] += pixel.blue
                y += pixelStep
            }
            x += pixelStep
        }
        val num = (width * height) / (pixelStep * pixelStep)
        return Color(color[0] / num, color[1] / num, color[2] / num)
    }

    fun rainbow(speed: Int, index: Int, saturation: Float, brightness: Float, opacity: Float): Color {
        val angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        val hue = angle / 360f
        val color = Color(Color.HSBtoRGB(hue, saturation, brightness))
        return Color(
            color.red,
            color.green,
            color.blue,
            max(0.0, min(255.0, (opacity * 255).toInt().toDouble())).toInt()
        )
    }

    fun interpolateColorsBackAndForth(speed: Int, index: Int, start: Color, end: Color, trueColor: Boolean): Color {
        var angle = (((System.currentTimeMillis()) / speed + index) % 360).toInt()
        angle = (if (angle >= 180) 360 - angle else angle) * 2
        return if (trueColor) interpolateColorHue(start, end, angle / 360f) else interpolateColorC(
            start,
            end,
            angle / 360f
        )
    }

    //The next few methods are for interpolating colors
    fun interpolateColor(color1: Color, color2: Color, amount: Float): Int {
        var amount = amount
        amount = min(1.0, max(0.0, amount.toDouble())).toFloat()
        return interpolateColorC(color1, color2, amount).rgb
    }

    fun interpolateColor(color1: Int, color2: Int, amount: Float): Int {
        var amount = amount
        amount = min(1.0, max(0.0, amount.toDouble())).toFloat()
        val cColor1 = Color(color1)
        val cColor2 = Color(color2)
        return interpolateColorC(cColor1, cColor2, amount).rgb
    }

    fun interpolateColorC(color1: Color, color2: Color, amount: Float): Color {
        var amount = amount
        amount = min(1.0, max(0.0, amount.toDouble())).toFloat()
        return Color(
            interpolateInt(color1.red, color2.red, amount.toDouble()),
            interpolateInt(color1.green, color2.green, amount.toDouble()),
            interpolateInt(color1.blue, color2.blue, amount.toDouble()),
            interpolateInt(color1.alpha, color2.alpha, amount.toDouble())
        )
    }

    fun interpolateColorHue(color1: Color, color2: Color, amount: Float): Color {
        var amount = amount
        amount = min(1.0, max(0.0, amount.toDouble())).toFloat()

        val color1HSB = Color.RGBtoHSB(color1.red, color1.green, color1.blue, null)
        val color2HSB = Color.RGBtoHSB(color2.red, color2.green, color2.blue, null)

        val resultColor = Color.getHSBColor(
            interpolateFloat(color1HSB[0], color2HSB[0], amount.toDouble()),
            interpolateFloat(color1HSB[1], color2HSB[1], amount.toDouble()), interpolateFloat(color1HSB[2], color2HSB[2], amount.toDouble())
        )

        return applyOpacity(resultColor, interpolateInt(color1.alpha, color2.alpha, amount.toDouble()) / 255f)
    }


    //Fade a color in and out with a specified alpha value ranging from 0-1
    fun fade(speed: Int, index: Int, color: Color, alpha: Float): Color {
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        var angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        angle = (if (angle > 180) 360 - angle else angle) + 180

        val colorHSB = Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360f))

        return Color(
            colorHSB.red,
            colorHSB.green,
            colorHSB.blue,
            max(0.0, min(255.0, (alpha * 255).toInt().toDouble())).toInt()
        )
    }


    private fun getAnimationEquation(index: Int, speed: Int): Float {
        val angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        return ((if (angle > 180) 360 - angle else angle) + 180) / 360f
    }

    fun createColorArray(color: Int): IntArray {
        return intArrayOf(
            bitChangeColor(color, 16),
            bitChangeColor(color, 8),
            bitChangeColor(color, 0),
            bitChangeColor(color, 24)
        )
    }

    fun getOppositeColor(color: Int): Int {
        var R = bitChangeColor(color, 0)
        var G = bitChangeColor(color, 8)
        var B = bitChangeColor(color, 16)
        val A = bitChangeColor(color, 24)
        R = 255 - R
        G = 255 - G
        B = 255 - B
        return R + (G shl 8) + (B shl 16) + (A shl 24)
    }

    fun getOppositeColor(color: Color): Color {
        return Color(getOppositeColor(color.rgb))
    }


    private fun bitChangeColor(color: Int, bitChange: Int): Int {
        return (color shr bitChange) and 255
    }
}