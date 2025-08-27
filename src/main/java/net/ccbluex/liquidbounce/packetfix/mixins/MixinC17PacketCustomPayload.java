package net.ccbluex.liquidbounce.packetfix.mixins;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(C17PacketCustomPayload.class)
public abstract class MixinC17PacketCustomPayload extends C17PacketCustomPayload implements Packet<INetHandlerPlayServer> {
    @Shadow
    private String channel;
    @Shadow
    private PacketBuffer data;

    // C17 Fixed, ah?
    @Overwrite
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(this.channel);
        buf.writeBytes((ByteBuf)this.data);
    }
    @Overwrite
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processVanilla250Packet(this);

        if (this.data != null)
        {
            this.data.release();
        }
    }
}