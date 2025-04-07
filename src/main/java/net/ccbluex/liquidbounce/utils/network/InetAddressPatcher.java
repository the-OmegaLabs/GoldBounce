package net.ccbluex.liquidbounce.utils.network;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.net.UnknownHostException;

/* loaded from: fast-ip-ping-v1.0.5-mc1.21.1-forge.jar:me/fallenbreath/fastipping/impl/InetAddressPatcher.class */
public class InetAddressPatcher {
    public static InetAddress patch(String hostName, InetAddress addr) throws UnknownHostException {
        if (InetAddresses.isInetAddress(hostName)) {
            InetAddress patched = InetAddress.getByAddress(addr.getHostAddress(), addr.getAddress());
            addr = patched;
        }
        return addr;
    }
}