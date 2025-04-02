package net.ccbluex.liquidbounce.api.minecraft;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

/* compiled from: INetworkManager.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��$\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\bf\u0018��2\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u0010\u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\bH&J\u001e\u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\nH&¨\u0006\u000b"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/INetworkManager;", "", "enableEncryption", "", "secretKey", "Ljavax/crypto/SecretKey;", "sendPacket", "packet", "Lnet/ccbluex/liquidbounce/api/minecraft/network/IPacket;", "any", "Lkotlin/Function0;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/INetworkManager.class */
public interface INetworkManager {
    void sendPacket(@NotNull IPacket iPacket);

    void enableEncryption(@NotNull SecretKey secretKey);

    void sendPacket(@NotNull IPacket iPacket, @NotNull Function0<Unit> function0);
}
