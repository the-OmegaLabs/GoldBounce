package net.ccbluex.liquidbounce.packetfix.features.hytpacket;



import net.ccbluex.liquidbounce.packetfix.features.hytpacket.packets.GermModPacket;
import net.ccbluex.liquidbounce.packetfix.features.hytpacket.packets.VexViewPacket;

import java.util.ArrayList;

public class PacketManager {
    public final ArrayList<CustomPacket> packets = new ArrayList<>();

    public String getName() {
        return "Packet Manager";
    }

    public void init() {
        packets.add(new GermModPacket());
        packets.add(new VexViewPacket());
    }
}
