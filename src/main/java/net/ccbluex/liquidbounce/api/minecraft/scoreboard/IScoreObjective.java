package net.ccbluex.liquidbounce.api.minecraft.scoreboard;

import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: IScoreObjective.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u001a\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018��2\u00020\u0001R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0007X¦\u0004¢\u0006\u0006\u001a\u0004\b\b\u0010\t¨\u0006\n"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/scoreboard/IScoreObjective;", "", "displayName", "", "getDisplayName", "()Ljava/lang/String;", "scoreboard", "Lnet/ccbluex/liquidbounce/api/minecraft/scoreboard/IScoreboard;", "getScoreboard", "()Lnet/ccbluex/liquidbounce/api/minecraft/scoreboard/IScoreboard;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/scoreboard/IScoreObjective.class */
public interface IScoreObjective {
    @NotNull
    String getDisplayName();

    @NotNull
    IScoreboard getScoreboard();
}
