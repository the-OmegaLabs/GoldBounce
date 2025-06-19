package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.movement.MoveFix;
import net.minecraft.block.BlockBed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBed.class)
public abstract class MixinBlockBed extends MixinBlock {
    @Inject(method = "setBedBounds", at = @At("HEAD"), cancellable = true)
    protected void setbbbds(CallbackInfo ci) {
        if (LiquidBounce.INSTANCE.getModuleManager().getModule(MoveFix.class).getState() && MoveFix.INSTANCE.getMode().contains("Bloxd")) {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            ci.cancel();
        }
    }
}
