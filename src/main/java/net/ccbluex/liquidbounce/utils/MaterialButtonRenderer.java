package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.renderer.GlStateManager.resetColor;

public class MaterialButtonRenderer {
    private static final int COLOR_BACKGROUND = new Color(33,33,33,200).getRGB();
    private static final int COLOR_DISABLED = new Color(100,100,100,150).getRGB();
    private static final int ACCENT_RGB = new Color(255,235,59,255).getRGB() & 0x00FFFFFF;
    private static final int ACCENT_ALPHA = 255;
    private static final int SHADOW_ALPHA = 60;
    private static final float RADIUS = 4.0f;

    public static void draw(GuiButton button, Minecraft mc, int mouseX, int mouseY) {
        if (!button.visible) return;



        // Shadow for elevation
        RenderUtils.INSTANCE.drawRoundedRect(
                button.xPosition + 1, button.yPosition + 1,
                button.xPosition + button.width + 1, button.yPosition + button.height + 1,
                new Color(0, 0, 0, SHADOW_ALPHA).getRGB(), RADIUS
        );

        // Background
        int bg = button.enabled ? COLOR_BACKGROUND : COLOR_DISABLED;
        RenderUtils.INSTANCE.drawRoundedRect(
                button.xPosition, button.yPosition,
                button.xPosition + button.width, button.yPosition + button.height,
                bg, RADIUS
        );

        // Text
        AWTFontRenderer.Companion.setAssumeNonVolatile(true);
        FontRenderer fontRenderer = Fonts.font35;
        int textColor = button.enabled ? 0xFFFFFFFF : 0xFFAAAAAA;
        fontRenderer.drawStringWithShadow(
                button.displayString,
                button.xPosition + (button.width - fontRenderer.getStringWidth(button.displayString)) / 2f,
                button.yPosition + (button.height - 5) / 2f,
                textColor
        );
        AWTFontRenderer.Companion.setAssumeNonVolatile(false);
        resetColor();
    }
}
