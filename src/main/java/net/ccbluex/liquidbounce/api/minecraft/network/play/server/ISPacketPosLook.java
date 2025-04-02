package net.ccbluex.liquidbounce.api.minecraft.network.play.server;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;

/* compiled from: ISPacketPosLook.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0007\n\u0002\b\b\bf\u0018��2\u00020\u0001R\u0018\u0010\u0002\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\u0004\u0010\u0005\"\u0004\b\u0006\u0010\u0007R\u0018\u0010\b\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\t\u0010\u0005\"\u0004\b\n\u0010\u0007¨\u0006\u000b"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/network/play/server/ISPacketPosLook;", "Lnet/ccbluex/liquidbounce/api/minecraft/network/IPacket;", "pitch", "", "getPitch", "()F", "setPitch", "(F)V", "yaw", "getYaw", "setYaw", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/network/play/server/ISPacketPosLook.class */
public interface ISPacketPosLook extends IPacket {
    float getYaw();

    void setYaw(float f);

    float getPitch();

    void setPitch(float f);
}
