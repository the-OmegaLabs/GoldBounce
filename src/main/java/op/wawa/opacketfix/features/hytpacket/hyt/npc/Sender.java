package op.wawa.opacketfix.features.hytpacket.hyt.npc;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

/**
 * @projectName: MIN
 * @author: vlouboos
 * @date: 2023-07-17 21:16:15
 */
public class Sender {
    public static void sendPacket(int packetId, String context) {
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C17PacketCustomPayload("germmod-netease", (new PacketBuffer(Unpooled.buffer().writeInt(packetId))).writeString(context)));
    }
}
