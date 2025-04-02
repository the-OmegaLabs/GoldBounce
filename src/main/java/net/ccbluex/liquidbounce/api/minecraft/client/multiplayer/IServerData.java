package net.ccbluex.liquidbounce.api.minecraft.client.multiplayer;

import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: IServerData.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\"\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u000b\n\u0002\u0010\b\n\u0002\b\u0003\bf\u0018��2\u00020\u0001R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0007X¦\u0004¢\u0006\u0006\u001a\u0004\b\b\u0010\tR\u0012\u0010\n\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u000b\u0010\u0005R\u0012\u0010\f\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\r\u0010\u0005R\u0012\u0010\u000e\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u000f\u0010\u0005R\u0012\u0010\u0010\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0011\u0010\u0005R\u0012\u0010\u0012\u001a\u00020\u0013X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0014\u0010\u0015¨\u0006\u0016"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/multiplayer/IServerData;", "", "gameVersion", "", "getGameVersion", "()Ljava/lang/String;", "pingToServer", "", "getPingToServer", "()J", "populationInfo", "getPopulationInfo", "serverIP", "getServerIP", "serverMOTD", "getServerMOTD", "serverName", "getServerName", "version", "", "getVersion", "()I", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/multiplayer/IServerData.class */
public interface IServerData {
    long getPingToServer();

    int getVersion();

    @NotNull
    String getGameVersion();

    @NotNull
    String getServerMOTD();

    @NotNull
    String getPopulationInfo();

    @NotNull
    String getServerName();

    @NotNull
    String getServerIP();
}
