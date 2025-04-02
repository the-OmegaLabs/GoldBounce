package net.ccbluex.liquidbounce.api.minecraft.client.entity;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.client.network.IINetHandlerPlayClient;
import net.ccbluex.liquidbounce.api.minecraft.util.IIChatComponent;
import net.ccbluex.liquidbounce.api.minecraft.util.IMovementInput;
import org.jetbrains.annotations.NotNull;

/* compiled from: IEntityPlayerSP.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n��\bf\u0018��2\u00020\u0001J\u0010\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 H&J\b\u0010!\u001a\u00020\u001eH&J\b\u0010\"\u001a\u00020\u001eH&J\u0010\u0010#\u001a\u00020\u001e2\u0006\u0010$\u001a\u00020%H&R\u0018\u0010\u0002\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\u0004\u0010\u0005\"\u0004\b\u0006\u0010\u0007R\u0018\u0010\b\u001a\u00020\tX¦\u000e¢\u0006\f\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u0012\u0010\u000e\u001a\u00020\u000fX¦\u0004¢\u0006\u0006\u001a\u0004\b\u000e\u0010\u0010R\u0012\u0010\u0011\u001a\u00020\u0012X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0013\u0010\u0014R\u0012\u0010\u0015\u001a\u00020\u0016X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0017\u0010\u0018R\u0018\u0010\u0019\u001a\u00020\u000fX¦\u000e¢\u0006\f\u001a\u0004\b\u001a\u0010\u0010\"\u0004\b\u001b\u0010\u001c¨\u0006&"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/IEntityPlayerSP;", "Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/IAbstractClientPlayer;", "horseJumpPower", "", "getHorseJumpPower", "()F", "setHorseJumpPower", "(F)V", "horseJumpPowerCounter", "", "getHorseJumpPowerCounter", "()I", "setHorseJumpPowerCounter", "(I)V", "isHandActive", "", "()Z", "movementInput", "Lnet/ccbluex/liquidbounce/api/minecraft/util/IMovementInput;", "getMovementInput", "()Lnet/ccbluex/liquidbounce/api/minecraft/util/IMovementInput;", "sendQueue", "Lnet/ccbluex/liquidbounce/api/minecraft/client/network/IINetHandlerPlayClient;", "getSendQueue", "()Lnet/ccbluex/liquidbounce/api/minecraft/client/network/IINetHandlerPlayClient;", "serverSprintState", "getServerSprintState", "setServerSprintState", "(Z)V", "addChatMessage", "", "component", "Lnet/ccbluex/liquidbounce/api/minecraft/util/IIChatComponent;", "closeScreen", "respawnPlayer", "sendChatMessage", "msg", "", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/entity/IEntityPlayerSP.class */
public interface IEntityPlayerSP extends IAbstractClientPlayer {
    int getHorseJumpPowerCounter();

    void setHorseJumpPowerCounter(int i);

    float getHorseJumpPower();

    void setHorseJumpPower(float f);

    @NotNull
    IINetHandlerPlayClient getSendQueue();

    @NotNull
    IMovementInput getMovementInput();

    boolean isHandActive();

    boolean getServerSprintState();

    void setServerSprintState(boolean z);

    void sendChatMessage(@NotNull String str);

    void respawnPlayer();

    void addChatMessage(@NotNull IIChatComponent iIChatComponent);

    void closeScreen();
}
