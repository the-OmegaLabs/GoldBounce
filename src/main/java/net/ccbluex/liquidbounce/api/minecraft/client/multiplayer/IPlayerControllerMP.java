package net.ccbluex.liquidbounce.api.minecraft.client.multiplayer;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityPlayerSP;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.player.IEntityPlayer;
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack;
import net.ccbluex.liquidbounce.api.minecraft.util.IEnumFacing;
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos;
import net.ccbluex.liquidbounce.api.minecraft.util.WVec3;
import net.ccbluex.liquidbounce.api.minecraft.world.IWorld;
import net.ccbluex.liquidbounce.api.minecraft.world.IWorldSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: IPlayerControllerMP.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��f\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u0007\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\bf\u0018��2\u00020\u0001J\u0018\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cH&J\b\u0010\u001d\u001a\u00020\u0015H&J\u0018\u0010\u001e\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cH&J:\u0010\u001f\u001a\u00020\u00152\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#2\b\u0010$\u001a\u0004\u0018\u00010%2\u0006\u0010&\u001a\u00020\u001a2\u0006\u0010'\u001a\u00020\u001c2\u0006\u0010(\u001a\u00020)H&J\u0010\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020!H&J \u0010-\u001a\u00020\u00152\u0006\u0010 \u001a\u00020.2\u0006\u0010\"\u001a\u00020/2\u0006\u0010$\u001a\u00020%H&J\b\u00100\u001a\u00020+H&J0\u00101\u001a\u00020+2\u0006\u00102\u001a\u00020\u00032\u0006\u00103\u001a\u00020\u00032\u0006\u00104\u001a\u00020\u00032\u0006\u00105\u001a\u00020\u00032\u0006\u00106\u001a\u00020!H&R\u0018\u0010\u0002\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\u0004\u0010\u0005\"\u0004\b\u0006\u0010\u0007R\u0012\u0010\b\u001a\u00020\tX¦\u0004¢\u0006\u0006\u001a\u0004\b\n\u0010\u000bR\u0018\u0010\f\u001a\u00020\tX¦\u000e¢\u0006\f\u001a\u0004\b\r\u0010\u000b\"\u0004\b\u000e\u0010\u000fR\u0012\u0010\u0010\u001a\u00020\u0011X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013R\u0012\u0010\u0014\u001a\u00020\u0015X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0014\u0010\u0016R\u0012\u0010\u0017\u001a\u00020\u0015X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0017\u0010\u0016¨\u00067"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/multiplayer/IPlayerControllerMP;", "", "blockHitDelay", "", "getBlockHitDelay", "()I", "setBlockHitDelay", "(I)V", "blockReachDistance", "", "getBlockReachDistance", "()F", "curBlockDamageMP", "getCurBlockDamageMP", "setCurBlockDamageMP", "(F)V", "currentGameType", "Lnet/ccbluex/liquidbounce/api/minecraft/world/IWorldSettings$WGameType;", "getCurrentGameType", "()Lnet/ccbluex/liquidbounce/api/minecraft/world/IWorldSettings$WGameType;", "isInCreativeMode", "", "()Z", "isNotCreative", "clickBlock", "blockPos", "Lnet/ccbluex/liquidbounce/api/minecraft/util/WBlockPos;", "enumFacing", "Lnet/ccbluex/liquidbounce/api/minecraft/util/IEnumFacing;", "extendedReach", "onPlayerDestroyBlock", "onPlayerRightClick", "playerSP", "Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/IEntityPlayerSP;", "theWorld", "Lnet/ccbluex/liquidbounce/api/minecraft/client/multiplayer/IWorldClient;", "itemStack", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemStack;", "position", "sideOpposite", "hitVec", "Lnet/ccbluex/liquidbounce/api/minecraft/util/WVec3;", "onStoppedUsingItem", "", "thePlayer", "sendUseItem", "Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/player/IEntityPlayer;", "Lnet/ccbluex/liquidbounce/api/minecraft/world/IWorld;", "updateController", "windowClick", "windowId", "slot", "mouseButton", "mode", "player", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/multiplayer/IPlayerControllerMP.class */
public interface IPlayerControllerMP {
    boolean isNotCreative();

    float getBlockReachDistance();

    @NotNull
    IWorldSettings.WGameType getCurrentGameType();

    boolean isInCreativeMode();

    float getCurBlockDamageMP();

    void setCurBlockDamageMP(float f);

    int getBlockHitDelay();

    void setBlockHitDelay(int i);

    void windowClick(int i, int i2, int i3, int i4, @NotNull IEntityPlayerSP iEntityPlayerSP);

    void updateController();

    boolean sendUseItem(@NotNull IEntityPlayer iEntityPlayer, @NotNull IWorld iWorld, @NotNull IItemStack iItemStack);

    boolean onPlayerRightClick(@NotNull IEntityPlayerSP iEntityPlayerSP, @NotNull IWorldClient iWorldClient, @Nullable IItemStack iItemStack, @NotNull WBlockPos wBlockPos, @NotNull IEnumFacing iEnumFacing, @NotNull WVec3 wVec3);

    void onStoppedUsingItem(@NotNull IEntityPlayerSP iEntityPlayerSP);

    boolean clickBlock(@NotNull WBlockPos wBlockPos, @NotNull IEnumFacing iEnumFacing);

    boolean onPlayerDestroyBlock(@NotNull WBlockPos wBlockPos, @NotNull IEnumFacing iEnumFacing);

    boolean extendedReach();
}
