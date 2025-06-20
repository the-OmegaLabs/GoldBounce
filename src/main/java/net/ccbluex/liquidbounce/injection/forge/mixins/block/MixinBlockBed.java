package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.movement.MoveFix;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBed.class)
public abstract class MixinBlockBed extends MixinBlock {

    /**
     * @author CCBlueX (Adapted for SolidChests by YourName)
     * @reason Force chests to have a full block collision box when SolidChests module is enabled.
     */
    @Inject(method = "setBedBounds", at = @At("HEAD"), cancellable = true)
    private void setbbbos(CallbackInfo ci) {
        try {
            if (LiquidBounce.INSTANCE != null && LiquidBounce.INSTANCE.getModuleManager() != null) {
                MoveFix moveFix = (MoveFix) LiquidBounce.INSTANCE.getModuleManager().getModule(MoveFix.class);
                if (moveFix != null && moveFix.getState() && MoveFix.INSTANCE.getMode().contains("Bloxd")) {
                    this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                    ci.cancel();
                }
            }
        }
        catch (Exception ignored) {
            // Ignore exceptions during early initialization
        }
    }
}