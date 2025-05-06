/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.minecraft.client.gui.*

class GuiMainMenu : GuiScreen() {

    override fun initGui() {
        val defaultHeight = height / 4 + 48
        val buttonWidth = 98
        val buttonHeight = 20
        val buttonSpacing = 24

        buttonList.run {
            add(GuiButton(1, width / 2 - 100, defaultHeight, buttonWidth, buttonHeight, "单人游戏"))
            add(GuiButton(2, width / 2 + 2, defaultHeight, buttonWidth, buttonHeight, "多人游戏"))

            add(GuiButton(100, width / 2 - 100, defaultHeight + buttonSpacing, buttonWidth, buttonHeight, "账户管理器"))
            add(GuiButton(103, width / 2 + 2, defaultHeight + buttonSpacing, buttonWidth, buttonHeight, "模组菜单"))

            add(GuiButton(101, width / 2 - 100, defaultHeight + buttonSpacing * 2, buttonWidth, buttonHeight, "服务器状态"))
            add(GuiButton(102, width / 2 + 2, defaultHeight + buttonSpacing * 2, buttonWidth, buttonHeight, "外挂设置"))

            add(GuiButton(108, width / 2 - 100, defaultHeight + buttonSpacing * 3, buttonWidth * 2 + 4, buttonHeight, "贡献者"))

            add(GuiButton(0, width / 2 - 100, defaultHeight + buttonSpacing * 4, buttonWidth, buttonHeight, "设置"))
            add(GuiButton(4, width / 2 + 2, defaultHeight + buttonSpacing * 4, buttonWidth, buttonHeight, "退出"))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        drawRoundedBorderRect(width / 2f - 115, height / 4f + 35, width / 2f + 115, height / 4f + 175,
            2f,
            Integer.MIN_VALUE,
            Integer.MIN_VALUE,
            3F
        )

        Fonts.fontBoldNoto180.drawCenteredString("金振口服液", width / 2F, height / 8F, 16433213, true)
        Fonts.fontNoto35.drawCenteredString("b09", width / 2F + 148, height / 8F + Fonts.font35.fontHeight, 0xffffff, true)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            101 -> mc.displayGuiScreen(GuiServerStatus(this))
            102 -> mc.displayGuiScreen(GuiClientConfiguration(this))
            103 -> mc.displayGuiScreen(GuiModsMenu(this))
            108 -> mc.displayGuiScreen(GuiContributors(this))
        }
    }
}