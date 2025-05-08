/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import de.florianmichael.viamcp.gui.GuiProtocolSelector;
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.lang.LanguageKt;
import net.ccbluex.liquidbounce.ui.client.GuiClientFixes;
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager;
import net.ccbluex.liquidbounce.ui.client.tools.GuiTools;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = GuiMultiplayer.class, priority = 1001)
public abstract class MixinGuiMultiplayer extends MixinGuiScreen {

    @Shadow
    private static Logger logger;

    private GuiButton bungeeCordSpoofButton;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {

        int increase = 0;
        int yPosition = 8;

        increase += 105;
        yPosition = Math.min(yPosition, 10);

        buttonList.add(new GuiButton(997, 5 + increase, yPosition, 45, 20, "Fixes"));
        buttonList.add(bungeeCordSpoofButton = new GuiButton(998, 55 + increase, yPosition, 98, 20, "BungeeCord Spoof: " + (BungeeCordSpoof.INSTANCE.getEnabled() ? "On" : "Off")));
        buttonList.add(new GuiButton(996, width - 120, yPosition, 62, 20, LanguageKt.translationMenu("altManager")));
        buttonList.add(new GuiButton(999, width - 52, yPosition, 46, 20, "Tools"));
        buttonList.add(new GuiButton(699, 5, yPosition, 90, 20, "Version"));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) throws IOException {
        switch (button.id) {
            case 996:
                mc.displayGuiScreen(new GuiAltManager((GuiScreen) (Object) this));
                break;
            case 997:
                mc.displayGuiScreen(new GuiClientFixes((GuiScreen) (Object) this));
                break;
            case 998:
                BungeeCordSpoof.INSTANCE.setEnabled(!BungeeCordSpoof.INSTANCE.getEnabled());
                bungeeCordSpoofButton.displayString = "BungeeCord Spoof: " + (BungeeCordSpoof.INSTANCE.getEnabled() ? "On" : "Off");
                FileManager.INSTANCE.getValuesConfig().saveConfig();
                break;
            case 999:
                mc.displayGuiScreen(new GuiTools((GuiScreen) (Object) this));
                break;
            case 699:
                mc.displayGuiScreen(new GuiProtocolSelector((GuiScreen) (Object) this));
                break;
        }
    }
}
