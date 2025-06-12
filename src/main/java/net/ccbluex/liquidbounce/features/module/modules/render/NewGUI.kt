/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.MaterialClickGUI
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.NewUi
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*

object NewGUI : Module("ClickGUI", Category.RENDER, Keyboard.KEY_RSHIFT, canBeEnabled = false) {
    override fun onEnable() {
        if(clickGUIMode.get() == "Material") mc.displayGuiScreen(MaterialClickGUI.getInstance())
        else mc.displayGuiScreen(NewUi.getInstance())
    }

    val fastRenderValue: BoolValue = BoolValue("FastRender", false)
    private val clickGUIMode = ListValue("ClickGUIMode", arrayOf("New", "Material"), "New")
    private val colorModeValue =
        ListValue("Color", arrayOf("Custom", "Fade"), "Custom")
    private val colorRedValue = IntegerValue("Red", 0, 0..255)
    private val colorGreenValue = IntegerValue("Green", 140, 0..255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0..255)

    val accentColor: Color?
        get() {
            var c: Color? = Color(255, 255, 255, 255)
            when (colorModeValue.get().lowercase(Locale.getDefault())) {
                "custom" -> c = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
                "fade" -> c = fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
            }
            return c
        }
}