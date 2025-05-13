/*
 * GoldBounce Hacked Client – Material Design Button Mixins
 * Updated to implement Material Design elevation, ripple, and accent coloring
 * Accent color: Google Yellow (#FFEB3B)
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.MaterialButtonRenderer;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;
import static net.minecraft.client.renderer.GlStateManager.resetColor;

@Mixin(GuiButton.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiButton extends Gui {
    @Shadow public boolean visible;
    @Shadow public int xPosition, yPosition, width, height;
    @Shadow protected boolean hovered;
    @Shadow public boolean enabled;
    @Shadow protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);
    @Shadow public String displayString;
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        MaterialButtonRenderer.draw((GuiButton)(Object)this, mc, mouseX, mouseY);
    }
}
