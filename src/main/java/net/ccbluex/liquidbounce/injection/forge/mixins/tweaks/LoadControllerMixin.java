package net.ccbluex.liquidbounce.injection.forge.mixins.tweaks;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.LoaderState;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LoadController.class, remap = false)
public class LoadControllerMixin {
    /**
     * <a href="https://codeberg.org/MicrocontrollersDev/QuickQuit">View origional source</a>
     * @author MicrocontrollersDev / bzym2
     * @reason Allows closing Minecraft during the Forge resource loading screen when starting the game.
     */
    @Inject(method = "transition(Lnet/minecraftforge/fml/common/LoaderState;Z)V", at = @At("HEAD"))
    private void quickQuit$earlyExit(LoaderState desiredState, boolean forceState, CallbackInfo ci) {
        if (Display.isCreated() && Display.isCloseRequested()) {
            FMLLog.info("The game window is being closed by the player, exiting.");
            FMLCommonHandler.instance().exitJava(0, false);
        }
    }
}
