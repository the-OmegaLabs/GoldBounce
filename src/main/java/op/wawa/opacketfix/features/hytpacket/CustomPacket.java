package op.wawa.opacketfix.features.hytpacket;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public interface CustomPacket {
    Minecraft mc = Minecraft.getMinecraft();

    String getChannel();

    void process(ByteBuf byteBuf);
}
