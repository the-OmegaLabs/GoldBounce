/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce.hud
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Element.Companion.MAX_GRADIENT_COLORS
import net.ccbluex.liquidbounce.utils.render.ColorSettingsFloat
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.floatValue
import net.ccbluex.liquidbounce.value.intValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.util.ResourceLocation

object HUD : Module("HUD", Category.RENDER, defaultInArray = false, gameDetecting = false, hideModule = true) {
    val customHotbar by _boolean("CustomHotbar", true)

    val smoothHotbarSlot by _boolean("SmoothHotbarSlot", true) { customHotbar }

    val roundedHotbarRadius by floatValue("RoundedHotbar-Radius", 3F, 0F..5F) { customHotbar }

    val hotbarMode by choices("Hotbar-Color", arrayOf("Custom", "Rainbow", "Gradient"), "Custom") { customHotbar }
    val hbHighlightColors = ColorSettingsInteger(this, "Hotbar-Highlight-Colors", applyMax = true)
    { customHotbar }.with(a = 0)
    val hbBackgroundColors = ColorSettingsInteger(this, "Hotbar-Background-Colors")
    { customHotbar && hotbarMode == "Custom" }.with(a = 190)
    val gradientHotbarSpeed by floatValue("Hotbar-Gradient-Speed", 1f, 0.5f..10f)
    { customHotbar && hotbarMode == "Gradient" }
    val maxHotbarGradientColors by intValue("Max-Hotbar-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { customHotbar && hotbarMode == "Gradient" }
    val bgGradColors = ColorSettingsFloat.create(this, "Hotbar-Gradient")
    { customHotbar && hotbarMode == "Gradient" && it <= maxHotbarGradientColors }
    val hbHighlightBorder by floatValue("HotbarBorder-Highlight-Width", 2F, 0.5F..5F) { customHotbar }
    val hbHighlightBorderColors = ColorSettingsInteger(this, "HotbarBorder-Highlight-Colors", zeroAlphaCheck = true)
    { customHotbar }.with(a = 255, g = 111, b = 255)
    val hbBackgroundBorder by floatValue("HotbarBorder-Background-Width", 0.5F, 0.5F..5F) { customHotbar }
    val hbBackgroundBorderColors = ColorSettingsInteger(this, "HotbarBorder-Background-Colors", zeroAlphaCheck = true)
    { customHotbar }.with(a = 0)

    val rainbowX by floatValue("Rainbow-X", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Rainbow" }
    val rainbowY by floatValue("Rainbow-Y", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Rainbow" }
    val gradientX by floatValue("Gradient-X", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Gradient" }
    val gradientY by floatValue("Gradient-Y", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Gradient" }

    val inventoryParticle by _boolean("InventoryParticle", false)
    private val blur by _boolean("Blur", false)
    val fontChat by _boolean("FontChat", false)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner)
            return

        hud.render(false)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) = hud.update()

    @EventTarget
    fun onKey(event: KeyEvent) = hud.handleKey('a', event.key)

    @EventTarget(ignoreCondition = true)
    fun onScreen(event: ScreenEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return
        if (state && blur && !mc.entityRenderer.isShaderActive && event.guiScreen != null &&
            !(event.guiScreen is GuiChat || event.guiScreen is GuiHudDesigner)
        ) mc.entityRenderer.loadShader(
            ResourceLocation("liquidbounce/blur.json")
        ) else if (mc.entityRenderer.shaderGroup != null &&
            "liquidbounce/blur.json" in mc.entityRenderer.shaderGroup.shaderGroupName
        ) mc.entityRenderer.stopUseShader()
    }

    init {
        state = true
    }
}