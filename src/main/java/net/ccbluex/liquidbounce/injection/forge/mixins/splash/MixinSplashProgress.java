   package net.ccbluex.liquidbounce.injection.forge.mixins.splash;

   import net.minecraft.client.Minecraft;
   import net.minecraft.util.ResourceLocation;
   import net.minecraft.client.gui.Gui;
   import net.minecraft.client.gui.GuiMainMenu;
   import org.spongepowered.asm.mixin.Mixin;
   import org.spongepowered.asm.mixin.injection.At;
   import org.spongepowered.asm.mixin.injection.Inject;
   import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

   import static net.minecraftforge.fml.client.config.GuiUtils.drawTexturedModalRect;

   @Mixin(GuiMainMenu.class)
   public class MixinSplashProgress {

       @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
       private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
           // Replace with your image path
           ResourceLocation backgroundTexture = new ResourceLocation("liquidbounce/splash/splash.png");
           Minecraft mc = Minecraft.getMinecraft();
           mc.getTextureManager().bindTexture(backgroundTexture);

           // Use mc.displayWidth and mc.displayHeight for screen dimensions
           int width = mc.displayWidth;
           int height = mc.displayHeight;

           // Assuming the texture is the same size as the screen
           int textureHeight = 622;

           drawTexturedModalRect(0, 0, 0, 0, width, height, textureHeight);

           ci.cancel(); // Cancel the original method execution
       }
   }
   