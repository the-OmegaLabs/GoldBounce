package net.ccbluex.liquidbounce.injection.forge.mixins.packets;

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
public class MixinC17PacketCustomPayload {

    @Shadow
    private String channel;

    @Shadow
    private PacketBuffer data;

    /**
     * @author YourName
     * @reason C17 Fixed, ah?
     */
    @Overwrite
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(this.channel);
        buf.writeBytes(this.data);
    }

    /**
     * @author YourName
     * @reason C17 Fixed, ah?
     */
    @Overwrite
    public void processPacket(INetHandlerPlayServer handler) {
        // 由于这个 Mixin 混入到了 C17PacketCustomPayload 实例中，
        // "this" 就代表了那个实例，所以可以直接传递。
        handler.processVanilla250Packet((C17PacketCustomPayload) (Object) this);

        if (this.data != null) {
            this.data.release();
        }
    }
}