/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.Element.Companion.MAX_GRADIENT_COLORS
import net.ccbluex.liquidbounce.value.floatValue
import net.ccbluex.liquidbounce.value.intValue
import java.awt.Color

class ColorSettingsFloat(owner: Any, name: String, val index: Int? = null, generalApply: () -> Boolean = { true }) {
    private val r by floatValue(
        "$name-R${index ?: ""}",
        if ((index ?: 0) % 3 == 1) 255f else 0f,
        0f..255f
    ) { generalApply() }
    private val g by floatValue(
        "$name-G${index ?: ""}",
        if ((index ?: 0) % 3 == 2) 255f else 0f,
        0f..255f
    ) { generalApply() }
    private val b by floatValue(
        "$name-B${index ?: ""}",
        if ((index ?: 0) % 3 == 0) 255f else 0f,
        0f..255f
    ) { generalApply() }

    fun color() = Color(r / 255f, g / 255f, b / 255f)

    init {
        when (owner) {
            is Element -> owner.addConfigurable(this)
            is Module -> owner.addConfigurable(this)
            // Should any other class use this, add here
        }
    }

    companion object {
        fun create(
            owner: Any, name: String, colors: Int = MAX_GRADIENT_COLORS, generalApply: (Int) -> Boolean = { true },
        ): List<ColorSettingsFloat> {
            return (1..colors).map { ColorSettingsFloat(owner, name, it) { generalApply(it) } }
        }
    }
}

class ColorSettingsInteger(
    owner: Any, name: String? = null, val index: Int? = null, withAlpha: Boolean = true,
    zeroAlphaCheck: Boolean = false,
    alphaApply: Boolean? = null, applyMax: Boolean = false, generalApply: () -> Boolean = { true },
) {
    private val string = if (name == null) "" else "$name-"
    private val max = if (applyMax) 255 else 0

    private var red = intValue(
        "${string}R${index ?: ""}",
        max,
        0..255
    ) { generalApply() && (!zeroAlphaCheck || a > 0) }
    private var green = intValue(
        "${string}G${index ?: ""}",
        max,
        0..255
    ) { generalApply() && (!zeroAlphaCheck || a > 0) }
    private var blue = intValue(
        "${string}B${index ?: ""}",
        max,
        0..255
    ) { generalApply() && (!zeroAlphaCheck || a > 0) }
    private var alpha = intValue(
        "${string}Alpha${index ?: ""}",
        255,
        0..255
    ) { alphaApply ?: generalApply() && withAlpha }

    private var r by red
    private var g by green
    private var b by blue
    private var a by alpha

    fun color(a: Int = this.a) = Color(r, g, b, a)

    fun color() = Color(r, g, b, a)

    fun with(r: Int? = null, g: Int? = null, b: Int? = null, a: Int? = null): ColorSettingsInteger {
        r?.let { red.set(it) }
        g?.let { green.set(it) }
        b?.let { blue.set(it) }
        a?.let { alpha.set(it) }

        return this
    }

    fun with(color: Color) = with(color.red, color.green, color.blue, color.alpha)

    init {
        when (owner) {
            is Element -> owner.addConfigurable(this)
            is Module -> owner.addConfigurable(this)
            // Should any other class use this, add here
        }
    }

    companion object {
        fun create(
            owner: Any, name: String, colors: Int, withAlpha: Boolean = true, zeroAlphaCheck: Boolean = true,
            applyMax: Boolean = false, generalApply: (Int) -> Boolean = { true },
        ): List<ColorSettingsInteger> {
            return (1..colors).map {
                ColorSettingsInteger(
                    owner,
                    name,
                    it,
                    withAlpha,
                    zeroAlphaCheck,
                    applyMax = applyMax
                ) { generalApply(it) }
            }
        }
    }
}

fun List<ColorSettingsFloat>.toColorArray(max: Int) = (0 until max).map {
    val colors = this[it].color()

    floatArrayOf(
        colors.red.toFloat() / 255f,
        colors.green.toFloat() / 255f,
        colors.blue.toFloat() / 255f,
        1f
    )
}