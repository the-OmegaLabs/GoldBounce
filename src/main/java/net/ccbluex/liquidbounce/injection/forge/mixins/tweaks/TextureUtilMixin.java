package net.ccbluex.liquidbounce.injection.forge.mixins.tweaks;

import net.ccbluex.liquidbounce.utils.modernsplash.CustomSplash;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraftforge.fml.client.SplashProgress;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Mixin(TextureUtil.class)
public class TextureUtilMixin {

    @Shadow
    static void bindTexture(int p_94277_0_) {}

    @Shadow
    public static void deleteTexture(int textureId) {}

    @Inject(method = "allocateTextureImpl", at = @At("HEAD"), cancellable = true)
    private static void allocateTextureImpl(int p_allocateTextureImpl_0_, int p_allocateTextureImpl_1_, int p_allocateTextureImpl_2_, int p_allocateTextureImpl_3_, CallbackInfo ci) {
        synchronized(CustomSplash.class) {
            deleteTexture(p_allocateTextureImpl_0_);
            bindTexture(p_allocateTextureImpl_0_);
        }

        if (p_allocateTextureImpl_1_ >= 0) {
            GL11.glTexParameteri(3553, 33085, p_allocateTextureImpl_1_);
            GL11.glTexParameterf(3553, 33082, 0.0F);
            GL11.glTexParameterf(3553, 33083, (float)p_allocateTextureImpl_1_);
            GL11.glTexParameterf(3553, 34049, 0.0F);
        }

        for(int i = 0; i <= p_allocateTextureImpl_1_; ++i) {
            GL11.glTexImage2D(3553, i, 6408, p_allocateTextureImpl_2_ >> i, p_allocateTextureImpl_3_ >> i, 0, 32993, 33639, (IntBuffer)null);
        }
        ci.cancel();
    }
}
