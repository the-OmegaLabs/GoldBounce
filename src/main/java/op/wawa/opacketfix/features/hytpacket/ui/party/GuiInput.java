package op.wawa.opacketfix.features.hytpacket.ui.party;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import op.wawa.opacketfix.features.hytpacket.hyt.party.Sender;
import op.wawa.opacketfix.features.hytpacket.ui.ClickableButton;
import op.wawa.opacketfix.features.hytpacket.ui.InputField;
import op.wawa.opacketfix.utils.RenderUtils;

import java.awt.*;
import java.io.IOException;

public class GuiInput extends GuiScreen {
    private InputField inputField;
    private ClickableButton confirm;
    private final String fieldId;
    private final VexViewButton confirmButton;

    public GuiInput(String fieldId, VexViewButton confirmButton) {
        this.fieldId = fieldId;
        this.confirmButton = confirmButton;
    }

    @Override
    public void initGui() {
        inputField = new InputField(width / 2 - 50, height / 2 - 30, 100, 20);
        confirm = new ClickableButton(width / 2, height / 2 + 30, 50, 20, confirmButton.getName()) {
            @Override
            public void clicked() {
                Sender.joinParty(inputField.getText(), fieldId, confirmButton.getId());
            }
        };
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        RenderUtils.drawImage(new ResourceLocation("hyt/background.png"), width / 2 - 100, height / 2 - 81, 200, 162);
        Minecraft.getMinecraft().fontRendererObj.drawString("花雨庭组队系统", width / 2, height / 2 - 72, new Color(216, 216, 216).getRGB());
        inputField.drawTextBox();
        confirm.drawScreen();
        GlStateManager.disableBlend();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        inputField.mouseClicked(mouseX, mouseY, mouseButton);
        confirm.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        inputField.keyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        inputField.updateCursorCounter();
        super.updateScreen();
    }
}
