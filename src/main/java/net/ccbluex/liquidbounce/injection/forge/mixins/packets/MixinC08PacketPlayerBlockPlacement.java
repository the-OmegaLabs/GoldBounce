package net.ccbluex.liquidbounce.injection.forge.mixins.packets;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

import static net.ccbluex.liquidbounce.bzym.GlobalFeatures.逼;

@Mixin(C08PacketPlayerBlockPlacement.class)
public abstract class MixinC08PacketPlayerBlockPlacement {
    @Shadow private BlockPos position;

    @Shadow private int placedBlockDirection;

    @Shadow public ItemStack stack;

    @Shadow public float facingX;

    @Shadow public float facingY;

    @Shadow public float facingZ;

}