package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.movement.MoveFix;
import net.minecraft.block.BlockChest;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockChest.class)
public abstract class MixinBlockChest extends MixinBlock {

    /**
     * @author CCBlueX (Adapted for SolidChests by YourName)
     * @reason Force chests to have a full block collision box when SolidChests module is enabled.
     */
    @Inject(method = "setBlockBoundsBasedOnState", at = @At("HEAD"), cancellable = true)
    private void setbbbos(IBlockAccess pSetBlockBoundsBasedOnState1, BlockPos pSetBlockBoundsBasedOnState2, CallbackInfo ci) {
        if (LiquidBounce.INSTANCE != null) {
            if (LiquidBounce.INSTANCE.getModuleManager().getModule(MoveFix.class).getState() && MoveFix.INSTANCE.getMode().contains("Bloxd")) {
                this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                ci.cancel();
            }
        }
    }
}