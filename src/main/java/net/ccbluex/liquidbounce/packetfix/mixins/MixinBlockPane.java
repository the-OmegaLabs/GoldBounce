/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.packetfix.mixins;


import de.florianmichael.vialoadingbase.ViaLoadingBase;
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

import java.util.List;

@Mixin(BlockPane.class)
public abstract class MixinBlockPane extends MixinBlock{
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
        if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() <= ViaLoadingBase.getInstance().getNativeVersion()) {
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

    @Override
    @Overwrite
    public void addCollisionBoxesToList(World p_addCollisionBoxesToList_1_, BlockPos p_addCollisionBoxesToList_2_, IBlockState p_addCollisionBoxesToList_3_, AxisAlignedBB p_addCollisionBoxesToList_4_, List<AxisAlignedBB> p_addCollisionBoxesToList_5_, Entity p_addCollisionBoxesToList_6_) {
        if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() <= ViaLoadingBase.getInstance().getNativeVersion()) {
            boolean flag = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.NORTH);
            boolean flag1 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.SOUTH);
            boolean flag2 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.WEST);
            boolean flag3 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.EAST);
            if ((!flag2 || !flag3) && (flag2 || flag3 || flag || flag1)) {
                if (flag2) {
                    this.setBlockBounds(0.0F, 0.0F, 0.4375F, 0.5F, 1.0F, 0.5625F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                } else if (flag3) {
                    this.setBlockBounds(0.5F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                }
            } else {
                this.setBlockBounds(0.0F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
                super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
            }

            if ((!flag || !flag1) && (flag2 || flag3 || flag || flag1)) {
                if (flag) {
                    this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 0.5F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                } else if (flag1) {
                    this.setBlockBounds(0.4375F, 0.0F, 0.5F, 0.5625F, 1.0F, 1.0F);
                    super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
                }
            } else {
                this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 1.0F);
                super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
            }
        } else {
            float f = 0.4375f;
            float f1 = 0.5625f;
            float f2 = 0.4375f;
            float f3 = 0.5625f;
            boolean flag = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.NORTH);
            boolean flag1 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.SOUTH);
            boolean flag2 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.WEST);
            boolean flag3 = this.canPaneConnectTo(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, EnumFacing.EAST);
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
            this.setBlockBounds(f, 0.0f, f2, f1, 1.0f, f3);
            super.addCollisionBoxesToList(p_addCollisionBoxesToList_1_, p_addCollisionBoxesToList_2_, p_addCollisionBoxesToList_3_, p_addCollisionBoxesToList_4_, p_addCollisionBoxesToList_5_, p_addCollisionBoxesToList_6_);
        }
    }

}