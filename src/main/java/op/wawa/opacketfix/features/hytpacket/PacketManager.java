package op.wawa.opacketfix.features.hytpacket;



import op.wawa.opacketfix.features.hytpacket.packets.GermModPacket;
import op.wawa.opacketfix.features.hytpacket.packets.VexViewPacket;

import java.util.ArrayList;
import java.util.List;

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
