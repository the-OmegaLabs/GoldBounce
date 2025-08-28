/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.ccbluex.liquidbounce.bzym.GlobalFeatures.逼;

@Mixin(BlockPane.class)
public abstract class MixinBlockPane extends MixinBlock {
    @Shadow
    @Final
    public abstract boolean canPaneConnectToBlock(Block var1);

    @Shadow
    public abstract boolean canPaneConnectTo(IBlockAccess var1, BlockPos var2, EnumFacing var3);

    @Overwrite
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        float f = 0.4375F;
        float f1 = 0.5625F;
        float f2 = 0.4375F;
        float f3 = 0.5625F;
        boolean flag = this.canPaneConnectToBlock(worldIn.getBlockState(pos.north()).getBlock());
        boolean flag1 = this.canPaneConnectToBlock(worldIn.getBlockState(pos.south()).getBlock());
        boolean flag2 = this.canPaneConnectToBlock(worldIn.getBlockState(pos.west()).getBlock());
        boolean flag3 = this.canPaneConnectToBlock(worldIn.getBlockState(pos.east()).getBlock());
        if (逼()) {
            if ((!flag2 || !flag3) && (flag2 || flag3 || flag || flag1)) {
                if (flag2) {
                    f = 0.0F;
                } else if (flag3) {
                    f1 = 1.0F;
                }
            } else {
                f = 0.0F;
                f1 = 1.0F;
            }

            if ((!flag || !flag1) && (flag2 || flag3 || flag || flag1)) {
                if (flag) {
                    f2 = 0.0F;
                } else if (flag1) {
                    f3 = 1.0F;
                }
            } else {
                f2 = 0.0F;
                f3 = 1.0F;
            }

        } else {
            if ((!flag2 || !flag3) && (flag2 || flag3 || flag || flag1)) {
                if (flag2) {
                    f = 0.0f;
                }
            } else if (flag2) {
                f = 0.0f;
                f1 = 1.0f;
            }
            if ((!flag || !flag1) && (flag2 || flag3 || flag || flag1)) {
                if (flag) {
                    f2 = 0.0f;
                } else if (flag1) {
                    f3 = 1.0f;
                }
            } else if (flag) {
                f2 = 0.0f;
                f3 = 1.0f;
            }
        }
        this.setBlockBounds(f, 0.0F, f2, f1, 1.0F, f3);
    }

    @Inject(method = "addCollisionBoxesToList", at = @At("HEAD"), cancellable = true)
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, CallbackInfo ci) {
        if (逼()) {
            // Cancel the original method execution. We will provide our own logic.
            ci.cancel();

            // Your custom logic from the @Overwrite block
            boolean flag = this.canPaneConnectTo(worldIn, pos, EnumFacing.NORTH);
            boolean flag1 = this.canPaneConnectTo(worldIn, pos, EnumFacing.SOUTH);
            boolean flag2 = this.canPaneConnectTo(worldIn, pos, EnumFacing.WEST);
            boolean flag3 = this.canPaneConnectTo(worldIn, pos, EnumFacing.EAST);

            if ((!flag2 || !flag3) && (flag2 || flag3 || flag || flag1)) {
                if (flag2) {
                    this.setBlockBounds(0.0F, 0.0F, 0.4375F, 0.5F, 1.0F, 0.5625F);
                    // We can't call super here directly in the same way, but we can call the superclass method on `this` instance
                    // since we are injecting into the BlockPane instance.
                    ((Block) (Object) this).addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
                } else if (flag3) {
                    this.setBlockBounds(0.5F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
                    ((Block) (Object) this).addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
                }
            } else {
                this.setBlockBounds(0.0F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
                ((Block) (Object) this).addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
            }

            if ((!flag || !flag1) && (flag2 || flag3 || flag || flag1)) {
                if (flag) {
                    this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 0.5F);
                    ((Block) (Object) this).addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
                } else if (flag1) {
                    this.setBlockBounds(0.4375F, 0.0F, 0.5F, 0.5625F, 1.0F, 1.0F);
                    ((Block) (Object) this).addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
                }
            } else {
                this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 1.0F);
                ((Block) (Object) this).addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
            }
        }
    }
}