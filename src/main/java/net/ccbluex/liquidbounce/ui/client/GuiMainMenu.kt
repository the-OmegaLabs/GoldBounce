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

        buttonList.run {
            add(GuiButton(100, width / 2 - 100, defaultHeight + 24, 98, 20, "账户管理器"))
            add(GuiButton(103, width / 2 + 2, defaultHeight + 24, 98, 20, "模组菜单"))
            add(GuiButton(101, width / 2 - 100, defaultHeight + 24 * 2, 98, 20, "服务器状态"))
            add(GuiButton(102, width / 2 + 2, defaultHeight + 24 * 2, 98, 20, "外挂设置"))

            add(GuiButton(1, width / 2 - 100, defaultHeight, 98, 20, "单人游戏"))
            add(GuiButton(2, width / 2 + 2, defaultHeight, 98, 20, "多人游戏"))

            // Minecraft Realms
            //		this.buttonList.add(new GuiButton(14, this.width / 2 - 100, j + 24 * 2, I18n.format("menu.online", new Object[0])));

            add(GuiButton(108, width / 2 - 100, defaultHeight + 24 * 3, "贡献者"))
            add(GuiButton(0, width / 2 - 100, defaultHeight + 24 * 4, 98, 20, "设置"))
            add(GuiButton(4, width / 2 + 2, defaultHeight + 24 * 4, 98, 20, "退出"))
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

        Fonts.fontBold180.drawCenteredString("金振口服液", width / 2F, height / 8F, 16433213, true)
        Fonts.font35.drawCenteredString("b07", width / 2F + 148, height / 8F + Fonts.font35.fontHeight, 0xffffff, true)

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