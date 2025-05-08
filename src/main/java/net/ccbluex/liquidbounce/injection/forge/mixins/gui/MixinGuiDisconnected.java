/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.special.AutoReconnect;
import net.ccbluex.liquidbounce.features.special.ClientFixes;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.utils.ServerUtils;
import net.ccbluex.liquidbounce.utils.misc.RandomUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.text.DecimalFormat;

@Mixin(GuiDisconnected.class)
public abstract class MixinGuiDisconnected extends MixinGuiScreen {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0");

    @Shadow
    private int field_175353_i;

    private GuiButton reconnectButton;
    private GuiSlider autoReconnectDelaySlider;
    private GuiButton forgeBypassButton;
    private int reconnectTimer;


    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        reconnectTimer = 0;
        buttonList.add(reconnectButton = new GuiButton(1, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 22, 98, 20, "Reconnect"));

        drawReconnectDelaySlider();

        buttonList.add(new GuiButton(4, width / 2 + 2, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 44, 98, 20, "Random username"));
        buttonList.add(forgeBypassButton = new GuiButton(5, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT + 66, "Bypass AntiForge: " + (ClientFixes.INSTANCE.getFmlFixesEnabled() ? "On" : "Off")));

        updateSliderText();
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) throws IOException {
        switch (button.id) {
            case 1:
                ServerUtils.INSTANCE.connectToLastServer();
                break;
            case 4:
                RandomUtils.INSTANCE.randomAccount();
                ServerUtils.INSTANCE.connectToLastServer();
                break;
            case 5:
                ClientFixes.INSTANCE.setFmlFixesEnabled(!ClientFixes.INSTANCE.getFmlFixesEnabled());
                forgeBypassButton.displayString = "Bypass AntiForge: " + (ClientFixes.INSTANCE.getFmlFixesEnabled() ? "On" : "Off");
                FileManager.INSTANCE.getValuesConfig().saveConfig();
                break;
        }
    }

    @Override
    public void updateScreen() {
        if (AutoReconnect.INSTANCE.isEnabled()) {
            reconnectTimer++;
            if (reconnectTimer > AutoReconnect.INSTANCE.getDelay() / 50)
                ServerUtils.INSTANCE.connectToLastServer();
        }
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreen(CallbackInfo callbackInfo) {
        if (AutoReconnect.INSTANCE.isEnabled()) {
            updateReconnectButton();
        }
    }

    private void drawReconnectDelaySlider() {
        buttonList.add(autoReconnectDelaySlider =
                new GuiSlider(2, width / 2 + 2, height / 2 + field_175353_i / 2
                        + fontRendererObj.FONT_HEIGHT + 22, 98, 20, "AutoReconnect: ",
                        "ms", AutoReconnect.MIN, AutoReconnect.MAX, AutoReconnect.INSTANCE.getDelay(), false, true,
                        guiSlider -> {
                            AutoReconnect.INSTANCE.setDelay(guiSlider.getValueInt());

                            reconnectTimer = 0;
                            updateReconnectButton();
                            updateSliderText();
                        }));
    }

    private void updateSliderText() {
        if (autoReconnectDelaySlider == null)
            return;

        if (!AutoReconnect.INSTANCE.isEnabled()) {
            autoReconnectDelaySlider.displayString = "AutoReconnect: Off";
        } else {
            autoReconnectDelaySlider.displayString = "AutoReconnect: " + DECIMAL_FORMAT.format(AutoReconnect.INSTANCE.getDelay() / 1000.0) + "s";
        }
    }

    private void updateReconnectButton() {
        if (reconnectButton != null)
            reconnectButton.displayString = "Reconnect" + (AutoReconnect.INSTANCE.isEnabled() ? " (" + (AutoReconnect.INSTANCE.getDelay() / 1000 - reconnectTimer / 20) + ")" : "");
    }
}