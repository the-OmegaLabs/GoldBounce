package net.ccbluex.liquidbounce.utils.skid.xinxin;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BlockUtil {
    private static final Minecraft mc;
    public static final List<Block> invalidBlocks;
    private static final List<Integer> nonValidItems;

    public static boolean isValid(Item item) {
        return item instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock)item).getBlock());
    }

    public static Block getBlock(BlockPos blockPos) {
        return BlockUtil.mc.theWorld.getBlockState(blockPos).getBlock();
    }

    public static boolean isValidBock(BlockPos blockPos) {
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
        return !(block instanceof BlockLiquid) && !(block instanceof BlockAir) && !(block instanceof BlockChest) && !(block instanceof BlockFurnace);
    }

    public static boolean isAirBlock(BlockPos blockPos) {
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
        return block instanceof BlockAir;
    }

    public static boolean isValidStack(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof ItemSlab || item instanceof ItemLeaves || item instanceof ItemSnow || item instanceof ItemBanner || item instanceof ItemFlintAndSteel) {
            return false;
        }
        for (int item2 : nonValidItems) {
            if (!item.equals(Item.getItemById(item2))) continue;
            return false;
        }
        return true;
    }

    public static Vec3 floorVec3(Vec3 vec3) {
        return new Vec3(Math.floor(vec3.xCoord), Math.floor(vec3.yCoord), Math.floor(vec3.zCoord));
    }

    public static Material getMaterial(BlockPos blockPos) {
        return BlockUtil.getBlock(blockPos).getMaterial();
    }

    public static boolean isReplaceable(BlockPos blockPos) {
        return BlockUtil.getMaterial(blockPos).isReplaceable();
    }

    public static IBlockState getState(BlockPos pos) {
        return BlockUtil.mc.theWorld.getBlockState(pos);
    }

    public static boolean canBeClicked(BlockPos pos) {
        return BlockUtil.getBlock(pos).canCollideCheck(BlockUtil.getState(pos), false);
    }

    public static String getBlockName(int id) {
        return Block.getBlockById(id).getLocalizedName();
    }

    public static boolean isFullBlock(BlockPos blockPos) {
        AxisAlignedBB axisAlignedBB = BlockUtil.getBlock(blockPos).getCollisionBoundingBox(BlockUtil.mc.theWorld, blockPos, BlockUtil.getState(blockPos));
        return axisAlignedBB != null && axisAlignedBB.maxX - axisAlignedBB.minX == 1.0 && axisAlignedBB.maxY - axisAlignedBB.minY == 1.0 && axisAlignedBB.maxZ - axisAlignedBB.minZ == 1.0;
    }

    public static double getCenterDistance(BlockPos blockPos) {
        return BlockUtil.mc.thePlayer.getDistance((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
    }

    public static Map<BlockPos, Block> searchBlocks(int radius) {
        HashMap<BlockPos, Block> blocks = new HashMap<BlockPos, Block>();
        for (int x2 = radius; x2 > -radius; --x2) {
            for (int y2 = radius; y2 > -radius; --y2) {
                for (int z = radius; z > -radius; --z) {
                    BlockPos blockPos = new BlockPos(BlockUtil.mc.thePlayer.lastTickPosX + (double)x2, BlockUtil.mc.thePlayer.lastTickPosY + (double)y2, BlockUtil.mc.thePlayer.lastTickPosZ + (double)z);
                    Block block = BlockUtil.getBlock(blockPos);
                    blocks.put(blockPos, block);
                }
            }
        }
        return blocks;
    }

    public static boolean collideBlock(AxisAlignedBB axisAlignedBB, ICollide collide) {
        for (int x2 = MathHelper.floor_double(BlockUtil.mc.thePlayer.getEntityBoundingBox().minX); x2 < MathHelper.floor_double(BlockUtil.mc.thePlayer.getEntityBoundingBox().maxX) + 1; ++x2) {
            for (int z = MathHelper.floor_double(BlockUtil.mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(BlockUtil.mc.thePlayer.getEntityBoundingBox().maxZ) + 1; ++z) {
                Block block = BlockUtil.getBlock(new BlockPos(x2, axisAlignedBB.minY, z));
                if (block == null || collide.collideBlock(block)) continue;
                return false;
            }
        }
        return true;
    }

    public static boolean collideBlockIntersects(AxisAlignedBB axisAlignedBB, ICollide collide) {
        for (int x2 = MathHelper.floor_double(BlockUtil.mc.thePlayer.getEntityBoundingBox().minX); x2 < MathHelper.floor_double(BlockUtil.mc.thePlayer.getEntityBoundingBox().maxX) + 1; ++x2) {
            for (int z = MathHelper.floor_double(BlockUtil.mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(BlockUtil.mc.thePlayer.getEntityBoundingBox().maxZ) + 1; ++z) {
                AxisAlignedBB boundingBox;
                BlockPos blockPos = new BlockPos(x2, axisAlignedBB.minY, z);
                Block block = BlockUtil.getBlock(blockPos);
                if (block == null || !collide.collideBlock(block) || (boundingBox = block.getCollisionBoundingBox(BlockUtil.mc.theWorld, blockPos, BlockUtil.getState(blockPos))) == null || !BlockUtil.mc.thePlayer.getEntityBoundingBox().intersectsWith(boundingBox)) continue;
                return true;
            }
        }
        return false;
    }

    static {
        invalidBlocks = Arrays.asList(Blocks.enchanting_table, Blocks.furnace, Blocks.carpet, Blocks.crafting_table, Blocks.trapped_chest, Blocks.chest, Blocks.dispenser, Blocks.air, Blocks.water, Blocks.lava, Blocks.flowing_water, Blocks.flowing_lava, Blocks.sand, Blocks.snow_layer, Blocks.torch, Blocks.anvil, Blocks.jukebox, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.noteblock, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.wooden_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_slab, Blocks.wooden_slab, Blocks.stone_slab2, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.yellow_flower, Blocks.red_flower, Blocks.anvil, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.cactus, Blocks.ladder, Blocks.web);
        nonValidItems = Arrays.asList(30, 58, 116, 158, 23, 6, 54, 146, 130, 26, 50, 76, 46, 37, 38);
        mc = Minecraft.getMinecraft();
    }

    public interface ICollide {
        boolean collideBlock(Block var1);
    }
}

