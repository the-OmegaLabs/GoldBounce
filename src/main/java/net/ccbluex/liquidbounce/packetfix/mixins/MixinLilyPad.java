/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.packetfix.mixins;


import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockLilyPad.class)
public abstract class MixinLilyPad extends MixinBlock{
    @Overwrite
    public AxisAlignedBB getCollisionBoundingBox(World p_getCollisionBoundingBox_1_, BlockPos p_getCollisionBoundingBox_2_, IBlockState p_getCollisionBoundingBox_3_) {
        if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() <= ViaLoadingBase.getInstance().getNativeVersion()) {
            return new AxisAlignedBB((double)p_getCollisionBoundingBox_2_.getX() + this.minX, (double)p_getCollisionBoundingBox_2_.getY() + this.minY, (double)p_getCollisionBoundingBox_2_.getZ() + this.minZ, (double)p_getCollisionBoundingBox_2_.getX() + this.maxX, (double)p_getCollisionBoundingBox_2_.getY() + this.maxY, (double)p_getCollisionBoundingBox_2_.getZ() + this.maxZ);
        }
        return new AxisAlignedBB((double)p_getCollisionBoundingBox_2_.getX() + 0.0625, p_getCollisionBoundingBox_2_.getY(), (double)p_getCollisionBoundingBox_2_.getZ() + 0.0625, (double)p_getCollisionBoundingBox_2_.getX() + 0.9375, (double)p_getCollisionBoundingBox_2_.getY() + 0.09375, (double)p_getCollisionBoundingBox_2_.getZ() + 0.9375);
    }
}