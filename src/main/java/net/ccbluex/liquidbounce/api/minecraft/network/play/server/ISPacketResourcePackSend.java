package net.ccbluex.liquidbounce.api.minecraft.network.play.server;

import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: ISPacketResourcePackSend.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u0012\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\u000e\n\u0002\b\u0005\bf\u0018��2\u00020\u0001R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0007\u0010\u0005¨\u0006\b"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/network/play/server/ISPacketResourcePackSend;", "", "hash", "", "getHash", "()Ljava/lang/String;", "url", "getUrl", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/network/play/server/ISPacketResourcePackSend.class */
public interface ISPacketResourcePackSend {
    @NotNull
    String getUrl();

    @NotNull
    String getHash();
}
