/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce.IN_DEV
import net.ccbluex.liquidbounce.api.ClientUpdate
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

class GuiUpdate : GuiScreen() {

    override fun initGui() {
        val j = height / 4 + 48

        buttonList.run {
            add(GuiButton(1, width / 2 + 2, j + 24 * 2, 98, 20, "Ignore"))
            add(GuiButton(2, width / 2 - 100, j + 24 * 2, 98, 20, "Go to download page"))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        assumeNonVolatile = true

        drawBackground(0)

        super.drawScreen(mouseX, mouseY, partialTicks)

        // Title
        glScalef(2F, 2F, 2F)
        assumeNonVolatile = false
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> mc.displayGuiScreen(GuiMainMenu())
            2 -> MiscUtils.showURL("https://liquidbounce.net/download")
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode)
            return

        super.keyTyped(typedChar, keyCode)
    }
}
