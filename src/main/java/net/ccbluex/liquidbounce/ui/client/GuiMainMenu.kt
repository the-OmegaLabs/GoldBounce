package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.NewUi
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.playMP3
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.minecraft.client.gui.*
import net.minecraft.util.ResourceLocation
import java.awt.Color

class GuiMainMenu : GuiScreen() {

    private val buttonsList = mutableListOf<ValorantButton>()

    override fun initGui() {
        super.initGui()
        buttonsList.clear()

        val btnWidth = 260
        val btnHeight = 45
        val startX = 30
        var startY = this.height / 5

        // 爽磕语法糖 kotlin我爱你
        listOf("SinglePlayer", "MultiPlayer", "Settings", "AltManager", "Hack Settings", "Exit").forEach { text ->
            buttonsList.add(ValorantButton(text, startX, startY, btnWidth, btnHeight))
            startY += 45
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawImage(ResourceLocation("liquidbounce/background.png"), 0, 0, width, height)

        // 右侧大立绘
        val heroineW = (width / 2.2f).toInt()
        val heroineH = height
        drawImage(
            ResourceLocation("liquidbounce/heroine.png"),
            width - heroineW, height - heroineH,
            heroineW, heroineH
        )

        // 在这里直接foreach
        buttonsList.forEach { it.draw(mouseX, mouseY) }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        buttonsList.forEach { btn ->
            if (btn.isMouseOver(mouseX, mouseY)) {
                playMP3("/assets/minecraft/liquidbounce/sounds/select.mp3")
                btn.onClick()
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val mouseX = org.lwjgl.input.Mouse.getEventX() * this.width / mc.displayWidth
        val mouseY = this.height - org.lwjgl.input.Mouse.getEventY() * this.height / mc.displayHeight - 1

        buttonsList.forEach { btn ->
            val hovering = btn.isMouseOver(mouseX, mouseY)
            if (hovering && !btn.hoveredLastTick) {
                playMP3("/assets/minecraft/liquidbounce/sounds/hover.mp3")
                btn.hoveredLastTick = true
            } else if (!hovering && btn.hoveredLastTick) {
                btn.hoveredLastTick = false
            }
        }
    }

    inner class ValorantButton(
        val text: String,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    ) {
        var hoveredLastTick = false
        val clickSound = ResourceLocation("liquidbounce/sounds/click.ogg")

        fun draw(mouseX: Int, mouseY: Int) {
            val hovered = isMouseOver(mouseX, mouseY)
            val color = if (hovered) Color(255, 0, 0).rgb else Color(255, 255, 255).rgb
            Fonts.font72.drawCenteredString( text, x + width / 2F, y + (height - 8) / 2F, color)
        }

        fun isMouseOver(mouseX: Int, mouseY: Int) =
            mouseX in x..(x + width) && mouseY in y..(y + height)

        fun onClick() {
            when (text) {
                "SinglePlayer" -> mc.displayGuiScreen(GuiSelectWorld(this@GuiMainMenu))
                "MultiPlayer" -> mc.displayGuiScreen(GuiMultiplayer(this@GuiMainMenu))
                "Settings" -> mc.displayGuiScreen(GuiOptions(this@GuiMainMenu, mc.gameSettings))
                "AltManager" -> mc.displayGuiScreen(GuiAltManager(this@GuiMainMenu))
                "Hack Settings" -> mc.displayGuiScreen(GuiClientConfiguration(this@GuiMainMenu))
                // ClickGUI主菜单无法调用 暂时弃用
                "ClickGUI" -> mc.displayGuiScreen(NewUi.getInstance())
                "Exit" -> mc.shutdown()
            }
        }
    }
}
