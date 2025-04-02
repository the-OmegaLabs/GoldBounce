package net.ccbluex.liquidbounce.api.minecraft.network.handshake.client;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.network.IEnumConnectionState;
import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;
import org.jetbrains.annotations.NotNull;

/* compiled from: ICPacketHandshake.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018��2\u00020\u0001R\u0018\u0010\u0002\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\u0004\u0010\u0005\"\u0004\b\u0006\u0010\u0007R\u0012\u0010\b\u001a\u00020\tX¦\u0004¢\u0006\u0006\u001a\u0004\b\n\u0010\u000bR\u0012\u0010\f\u001a\u00020\rX¦\u0004¢\u0006\u0006\u001a\u0004\b\u000e\u0010\u000f¨\u0006\u0010"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/network/handshake/client/ICPacketHandshake;", "Lnet/ccbluex/liquidbounce/api/minecraft/network/IPacket;", "ip", "", "getIp", "()Ljava/lang/String;", "setIp", "(Ljava/lang/String;)V", "port", "", "getPort", "()I", "requestedState", "Lnet/ccbluex/liquidbounce/api/minecraft/network/IEnumConnectionState;", "getRequestedState", "()Lnet/ccbluex/liquidbounce/api/minecraft/network/IEnumConnectionState;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/network/handshake/client/ICPacketHandshake.class */
public interface ICPacketHandshake extends IPacket {
    int getPort();

    @NotNull
    String getIp();

    void setIp(@NotNull String str);

    @NotNull
    IEnumConnectionState getRequestedState();
}
