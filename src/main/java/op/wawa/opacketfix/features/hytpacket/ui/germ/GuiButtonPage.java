package op.wawa.opacketfix.features.hytpacket.ui.germ;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import op.wawa.opacketfix.utils.RenderUtils;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * @projectName: MIN
 * @author: vlouboos
 * @date: 2023-07-21 15:21:19
 */
public class GuiButtonPage extends GuiScreen {
    private final String uuid;
    private final LinkedHashSet<GermModButton> buttons = new LinkedHashSet<>();

    public GuiButtonPage(String uuid, ArrayList<GermModButton> germModButtons) {
        this.uuid = uuid;
        if (germModButtons.size() == 0) {
            mc.displayGuiScreen(null);
            return;
        }
        buttons.addAll(germModButtons);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        RenderUtils.drawImage(new ResourceLocation("liquidbounce+/hyt/background.png"), width / 2 - 100, height / 2 - 81, 200, 162);
        Minecraft.getMinecraft().fontRendererObj.drawString("花雨庭菜单", width / 2, height / 2 - 72, new Color(216, 216, 216).getRGB());
        int y = height / 2 - 20;
        for (GermModButton button : buttons) {
            button.drawButton(uuid, width / 2, y, mouseX, mouseY);
            y += 40;
        }
        GlStateManager.disableBlend();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (GermModButton button : buttons) {
            button.mouseClicked(uuid);
        }
    }
}
