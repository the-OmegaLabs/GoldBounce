package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.ccbluex.liquidbounce.bzym.GlobalFeatures;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static net.ccbluex.liquidbounce.bzym.GlobalFeatures.逼;

@Mixin(BlockFarmland.class)
public abstract class MixinBlockFarmland extends MixinBlock{
}