/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.EntityUtils.getHealth
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.regex.Pattern
import kotlin.math.abs

object ColorUtils {
    /** Array of the special characters that are allowed in any text drawing of Minecraft.  */
    val allowedCharactersArray = charArrayOf('/', '\n', '\r', '\t', '\u0000', '', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')

    fun isAllowedCharacter(character: Char) =
        character.code != 167 && character.code >= 32 && character.code != 127

    private val COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")

    val hexColors = IntArray(16)
    fun interpolateColorsCyclic(colors: Array<Color>, progress: Float): Color {
        if (colors.isEmpty()) return Color.WHITE
        if (colors.size == 1) return colors[0]

        val n = colors.size
        val total = progress * n
        val index0 = (total.toInt() % n).coerceAtLeast(0)
        val index1 = (index0 + 1) % n  // 循环到第一个颜色
        val fraction = total - total.toInt()

        return interpolateColor(colors[index0], colors[index1], fraction)
    }
    fun interpolateColor(color1: Color, color2: Color, fraction: Float): Color {
        val invertedFraction = 1f - fraction
        return Color(
            (color1.red * invertedFraction + color2.red * fraction).toInt(),
            (color1.green * invertedFraction + color2.green * fraction).toInt(),
            (color1.blue * invertedFraction + color2.blue * fraction).toInt(),
            (color1.alpha * invertedFraction + color2.alpha * fraction).toInt()
        )
    }
    init {
        repeat(16) { i ->
            val baseColor = (i shr 3 and 1) * 85

            val red = (i shr 2 and 1) * 170 + baseColor + if (i == 6) 85 else 0
            val green = (i shr 1 and 1) * 170 + baseColor
            val blue = (i and 1) * 170 + baseColor

            hexColors[i] = red and 255 shl 16 or (green and 255 shl 8) or (blue and 255)
        }
    }
    @JvmStatic
    fun setColour(colour: Int) {
        val a = (colour shr 24 and 0xFF) / 255.0f
        val r = (colour shr 16 and 0xFF) / 255.0f
        val g = (colour shr 8 and 0xFF) / 255.0f
        val b = (colour and 0xFF) / 255.0f
        GL11.glColor4f(r, g, b, a)
    }
    fun interpolateColors(colors: Array<Color>, progress: Float): Color {
        if (colors.isEmpty()) return Color.WHITE
        val scaledProgress = (progress * (colors.size - 1)).coerceIn(0f, colors.size - 1 - 1e-6f)
        val index = scaledProgress.toInt()
        val localProgress = scaledProgress - index

        val start = colors[index]
        val end = colors[(index + 1) % colors.size]

        val r = start.red + (end.red - start.red) * localProgress
        val g = start.green + (end.green - start.green) * localProgress
        val b = start.blue + (end.blue - start.blue) * localProgress
        return Color(r/255f, g/255f, b/255f)
    }
    fun stripColor(input: String): String = COLOR_PATTERN.matcher(input).replaceAll("")
    fun Color.withAlpha(a: Int) = Color(red, green, blue, a)
    fun translateAlternateColorCodes(textToTranslate: String): String {
        val chars = textToTranslate.toCharArray()

        for (i in 0 until chars.lastIndex) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(chars[i + 1], true)) {
                chars[i] = '§'
                chars[i + 1] = Character.toLowerCase(chars[i + 1])
            }
        }

        return String(chars)
    }

    fun randomMagicText(text: String): String {
        val stringBuilder = StringBuilder()
        val allowedCharacters = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"

        for (c in text.toCharArray()) {
            if (isAllowedCharacter(c)) {
                val index = nextInt(endExclusive = allowedCharacters.length)
                stringBuilder.append(allowedCharacters.toCharArray()[index])
            }
        }

        return stringBuilder.toString()
    }
    fun colorCode(code: String, alpha: Int = 255): Color {
        when (code.lowercase()) {
            "0" -> {
                return Color(0, 0, 0, alpha)
            }
            "1" -> {
                return Color(0, 0, 170, alpha)
            }
            "2" -> {
                return Color(0, 170, 0, alpha)
            }
            "3" -> {
                return Color(0, 170, 170, alpha)
            }
            "4" -> {
                return Color(170, 0, 0, alpha)
            }
            "5" -> {
                return Color(170, 0, 170, alpha)
            }
            "6" -> {
                return Color(255, 170, 0, alpha)
            }
            "7" -> {
                return Color(170, 170, 170, alpha)
            }
            "8" -> {
                return Color(85, 85, 85, alpha)
            }
            "9" -> {
                return Color(85, 85, 255, alpha)
            }
            "a" -> {
                return Color(85, 255, 85, alpha)
            }
            "b" -> {
                return Color(85, 255, 255, alpha)
            }
            "c" -> {
                return Color(255, 85, 85, alpha)
            }
            "d" -> {
                return Color(255, 85, 255, alpha)
            }
            "e" -> {
                return Color(255, 255, 85, alpha)
            }
            else -> {
                return Color(255, 255, 255, alpha)
            }
        }
    }
    fun rainbow(offset: Long = 400000L, alpha: Float = 1f): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 10000000000F % 1, 1F, 1F))
        return Color(currentColor.red / 255F * 1F, currentColor.green / 255f * 1F, currentColor.blue / 255F * 1F, alpha)
    }

    fun interpolateHSB(startColor: Color, endColor: Color, process: Float): Color {
        val startHSB = Color.RGBtoHSB(startColor.red, startColor.green, startColor.blue, null)
        val endHSB = Color.RGBtoHSB(endColor.red, endColor.green, endColor.blue, null)

        val brightness = (startHSB[2] + endHSB[2]) / 2
        val saturation = (startHSB[1] + endHSB[1]) / 2

        val hueMax = if (startHSB[0] > endHSB[0]) startHSB[0] else endHSB[0]
        val hueMin = if (startHSB[0] > endHSB[0]) endHSB[0] else startHSB[0]

        val hue = (hueMax - hueMin) * process + hueMin
        return Color.getHSBColor(hue, saturation, brightness)
    }

    fun interpolateHealthColor(
        entity: EntityLivingBase,
        r: Int,
        g: Int,
        b: Int,
        a: Int,
        healthFromScoreboard: Boolean,
        absorption: Boolean
    ): Color {
        val entityHealth = getHealth(entity, healthFromScoreboard, absorption)
        val healthRatio = (entityHealth / entity.maxHealth).coerceIn(0F, 1F)
        val red = (r * (1 - healthRatio)).toInt()
        val green = (g * healthRatio).toInt()

        return Color(red, green, b, a)
    }
    fun fade(colorSettings: ColorSettingsInteger, speed: Int, count: Int): Color {
        val color = colorSettings.color()

        return fade(color, speed, count)
    }

    fun fade(color: Color, index: Int, count: Int): Color {
        val hsb = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsb)
        var brightness =
            abs(((System.currentTimeMillis() % 2000L).toFloat() / 1000.0f + index.toFloat() / count.toFloat() * 2.0f) % 2.0f - 1.0f)
        brightness = 0.5f + 0.5f * brightness
        hsb[2] = brightness % 2.0f
        return Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]))
    }
}
