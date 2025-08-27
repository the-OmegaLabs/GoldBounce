/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.packetfix.mixins;


import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockFarmland.class)
public abstract class MixinBlockFarmland extends MixinBlock{
    @Overwrite
    public AxisAlignedBB getCollisionBoundingBox(World p_getCollisionBoundingBox_1_, BlockPos p_getCollisionBoundingBox_2_, IBlockState p_getCollisionBoundingBox_3_) {
        double f = ViaLoadingBase.getInstance().getTargetVersion().getVersion() <= 47 ? 1.0 : 0.9375;
        return new AxisAlignedBB(p_getCollisionBoundingBox_2_.getX(), p_getCollisionBoundingBox_2_.getY(), p_getCollisionBoundingBox_2_.getZ(), p_getCollisionBoundingBox_2_.getX() + 1, (double)p_getCollisionBoundingBox_2_.getY() + f, p_getCollisionBoundingBox_2_.getZ() + 1);


    }
}