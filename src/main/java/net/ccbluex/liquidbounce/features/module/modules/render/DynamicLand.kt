package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.gui.ScaledResolution

object DynamicLand : Module("DynamicLand",Category.RENDER){
    override fun onEnable() {
        getFPS()
    }
    private fun getFPS(): Int {
        return (1.0 / mc.timer.renderPartialTicks).toInt()
    }
    @EventTarget
    fun onRender2D() {
        val sc = ScaledResolution(mc)
        val willDraw = "GoldBounce | ${mc.thePlayer?.name ?: "Unknown"} | ${getFPS()}FPS"
        Fonts.font40.drawString(willDraw, sc.scaledWidth / 2 - Fonts.font40.getStringWidth(willDraw) / 2, sc.scaledHeight / 2 - 10, -1)
    }
}