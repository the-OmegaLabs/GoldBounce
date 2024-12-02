/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce.clickGui
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.BlackStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.LiquidBounceStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.NullStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.SlowlyStyle
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.float
import net.ccbluex.liquidbounce.value.int
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard
import java.awt.Color

object ClickGUI : Module("ClickGUI", Category.RENDER, Keyboard.KEY_RSHIFT, canBeEnabled = false) {
    private val style by
    object : ListValue("Style", arrayOf("LiquidBounce", "Null", "Slowly", "Black"), "LiquidBounce") {
        override fun onChanged(oldValue: String, newValue: String) = updateStyle()
    }
    var scale by float("Scale", 0.8f, 0.5f..1.5f)
    val maxElements by int("MaxElements", 15, 1..30)
    val fadeSpeed by float("FadeSpeed", 1f, 0.5f..4f)
    val scrolls by boolean("Scrolls", true)
    val spacedModules by boolean("SpacedModules", false)
    val panelsForcedInBoundaries by boolean("PanelsForcedInBoundaries", false)

    private val colorRainbowValue = boolean("Rainbow", false) { style !in arrayOf("Slowly", "Black") }
    private val colorRed by int("R", 0, 0..255) { colorRainbowValue.isSupported() && !colorRainbowValue.get() }
    private val colorGreen by int("G", 160, 0..255) { colorRainbowValue.isSupported() && !colorRainbowValue.get() }
    private val colorBlue by int("B", 255, 0..255) { colorRainbowValue.isSupported() && !colorRainbowValue.get() }

    val guiColor
        get() = if (colorRainbowValue.get()) ColorUtils.rainbow().rgb
        else Color(colorRed, colorGreen, colorBlue).rgb

    override fun onEnable() {
        updateStyle()
        mc.displayGuiScreen(clickGui)
    }

    private fun updateStyle() {
        clickGui.style = when (style) {
            "LiquidBounce" -> LiquidBounceStyle
            "Null" -> NullStyle
            "Slowly" -> SlowlyStyle
            "Black" -> BlackStyle
            else -> return
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S2EPacketCloseWindow && mc.currentScreen is ClickGui)
            event.cancelEvent()
    }
}
