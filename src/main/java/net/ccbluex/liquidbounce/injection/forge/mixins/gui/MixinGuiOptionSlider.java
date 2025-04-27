package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={GuiOptionSlider.class})
public class MixinGuiOptionSlider
extends GuiButton {
    @Shadow
    public boolean dragging;
    @Shadow
    public float sliderValue;
    @Shadow
    private GameSettings.Options options;

    public MixinGuiOptionSlider(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    @Overwrite
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = ((float)(mouseX - this.xPosition) - 4.0f) / ((float)this.width - 8.0f);
                this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0f, 1.0f);
                float f = this.options.denormalizeValue(this.sliderValue);
                mc.gameSettings.setOptionFloatValue(this.options, f);
                this.displayString = mc.gameSettings.getKeyBinding(this.options);
            }
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * ((float)this.width - 8.0f)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect((float)(this.xPosition + (int)(this.sliderValue * ((float)this.width - 8.0f))) + 4.0f, this.yPosition, 196, 66, 4, 20);
        }
    }
}
