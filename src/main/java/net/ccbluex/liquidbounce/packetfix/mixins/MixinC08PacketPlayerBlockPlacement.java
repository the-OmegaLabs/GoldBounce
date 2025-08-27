package net.ccbluex.liquidbounce.packetfix.mixins;


import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.ViaMCP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(C08PacketPlayerBlockPlacement.class)
public abstract class MixinC08PacketPlayerBlockPlacement {
    @Shadow private BlockPos position;

    @Shadow private int placedBlockDirection;

    @Shadow public ItemStack stack;

    @Shadow public float facingX;

    @Shadow public float facingY;

    @Shadow public float facingZ;

    // C08 Fixed, ah?
    @Overwrite()
    public void readPacketData(PacketBuffer p_readPacketData_1_)throws IOException{
        this.position = p_readPacketData_1_.readBlockPos();
        this.placedBlockDirection = p_readPacketData_1_.readUnsignedByte();
        this.stack = p_readPacketData_1_.readItemStackFromBuffer();
        float f = ViaLoadingBase.getInstance().getTargetVersion().getVersion() <= 47 ? 16.0F : 1.0F;
        this.facingX = (float)p_readPacketData_1_.readUnsignedByte() / f;
        this.facingY = (float)p_readPacketData_1_.readUnsignedByte() / f;
        this.facingZ = (float)p_readPacketData_1_.readUnsignedByte() / f;
    }
    @Overwrite()
    public void writePacketData(PacketBuffer p_writePacketData_1_) throws IOException{
        p_writePacketData_1_.writeBlockPos(this.position);
        p_writePacketData_1_.writeByte(this.placedBlockDirection);
        p_writePacketData_1_.writeItemStackToBuffer(this.stack);
        float f = ViaLoadingBase.getInstance().getTargetVersion().getVersion() <= 47 ? 16.0F : 1.0F;
        p_writePacketData_1_.writeByte((int)(this.facingX * f));
        p_writePacketData_1_.writeByte((int)(this.facingY * f));
        p_writePacketData_1_.writeByte((int)(this.facingZ * f));
    }
}