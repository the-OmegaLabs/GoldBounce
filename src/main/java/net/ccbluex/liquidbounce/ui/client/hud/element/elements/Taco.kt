/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */

package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.command.commands.TacoCommand.tacoToggle
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.value.float
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation

/**
 * TACO TACO TACO!!
 */
@ElementInfo(name = "Cockroach", priority = 1)
class Taco(x: Double = 2.0, y: Double = 441.0) : Element(x = x, y = y) {

    private val frameSpeed by float("frameSpeed", 50f, 0f..200f)
    private val animationSpeed by float("animationSpeed", 0.15f, 0.01f..1.0f)

    private var lastFrameTime = System.currentTimeMillis()
    private var image = 0
    private var running = 0f

    private val tacoTextures = arrayOf(
        ResourceLocation("liquidbounce/taco/1.png"),
        ResourceLocation("liquidbounce/taco/2.png"),
        ResourceLocation("liquidbounce/taco/3.png"),
        ResourceLocation("liquidbounce/taco/4.png"),
        ResourceLocation("liquidbounce/taco/5.png"),
        ResourceLocation("liquidbounce/taco/6.png"),
        ResourceLocation("liquidbounce/taco/7.png"),
        ResourceLocation("liquidbounce/taco/8.png"),
        ResourceLocation("liquidbounce/taco/9.png"),
        ResourceLocation("liquidbounce/taco/10.png"),
        ResourceLocation("liquidbounce/taco/11.png"),
        ResourceLocation("liquidbounce/taco/12.png"),
        ResourceLocation("liquidbounce/taco/13.png"),
        ResourceLocation("liquidbounce/taco/14.png"),
        ResourceLocation("liquidbounce/taco/15.png"),
        ResourceLocation("liquidbounce/taco/16.png"),
        ResourceLocation("liquidbounce/taco/17.png"),
        ResourceLocation("liquidbounce/taco/18.png"),
        ResourceLocation("liquidbounce/taco/19.png"),
        ResourceLocation("liquidbounce/taco/20.png"),
        ResourceLocation("liquidbounce/taco/21.png"),
        ResourceLocation("liquidbounce/taco/22.png"),
        ResourceLocation("liquidbounce/taco/23.png"),
        ResourceLocation("liquidbounce/taco/24.png"),
        ResourceLocation("liquidbounce/taco/25.png"),
        ResourceLocation("liquidbounce/taco/26.png"),
        ResourceLocation("liquidbounce/taco/27.png"),
        ResourceLocation("liquidbounce/taco/28.png"),
        ResourceLocation("liquidbounce/taco/29.png"),
        ResourceLocation("liquidbounce/taco/30.png"),
        ResourceLocation("liquidbounce/taco/31.png"),
        ResourceLocation("liquidbounce/taco/32.png"),
        ResourceLocation("liquidbounce/taco/33.png"),
        ResourceLocation("liquidbounce/taco/34.png"),
        ResourceLocation("liquidbounce/taco/35.png"),
        ResourceLocation("liquidbounce/taco/36.png"),
        ResourceLocation("liquidbounce/taco/37.png"),
        ResourceLocation("liquidbounce/taco/38.png"),
        ResourceLocation("liquidbounce/taco/39.png"),
        ResourceLocation("liquidbounce/taco/40.png"),
        ResourceLocation("liquidbounce/taco/41.png"),
        ResourceLocation("liquidbounce/taco/42.png"),
        ResourceLocation("liquidbounce/taco/43.png"),
        ResourceLocation("liquidbounce/taco/44.png"),
        ResourceLocation("liquidbounce/taco/45.png"),
        ResourceLocation("liquidbounce/taco/46.png")
    )

    override fun drawElement(): Border {
        val player = mc.thePlayer ?: return Border(0F, 0F, 0F, 0F)

        if (tacoToggle || player.ticksExisted < 20)
            return Border(0F, 0F, 0F, 0F)

        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastFrameTime

        if (elapsedTime >= frameSpeed) {
            updateAnimation()
            lastFrameTime = currentTime
        }

        val scaledScreen = ScaledResolution(mc)

        running += animationSpeed * deltaTime
        RenderUtils.drawImage(tacoTextures[image], running.toInt(), 0, 64, 32)

        if (running > scaledScreen.scaledWidth) {
            running = -scaledScreen.scaledWidth / 4F
        }

        return Border(0F, 0F, 64F, 32F)
    }

    private fun updateAnimation() {
        image = (image + 1) % tacoTextures.size
    }

}