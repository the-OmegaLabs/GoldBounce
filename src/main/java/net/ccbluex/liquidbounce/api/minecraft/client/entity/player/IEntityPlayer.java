package net.ccbluex.liquidbounce.api.minecraft.client.entity.player;

import com.mojang.authlib.GameProfile;
import kotlin.Metadata;
import kotlin.jvm.JvmName;
import net.ccbluex.liquidbounce.api.MinecraftVersion;
import net.ccbluex.liquidbounce.api.SupportsMinecraftVersions;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase;
import net.ccbluex.liquidbounce.api.minecraft.entity.player.IInventoryPlayer;
import net.ccbluex.liquidbounce.api.minecraft.entity.player.IPlayerCapabilities;
import net.ccbluex.liquidbounce.api.minecraft.inventory.IContainer;
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack;
import net.ccbluex.liquidbounce.api.minecraft.stats.IStatBase;
import net.ccbluex.liquidbounce.api.minecraft.util.IFoodStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: IEntityPlayer.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��x\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\u0006\n\u0002\b\u000f\n\u0002\u0010\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n��\bf\u0018��2\u00020\u0001J\u0010\u0010I\u001a\u00020J2\u0006\u0010K\u001a\u00020\u0011H&J\u0018\u0010L\u001a\u00020J2\u0006\u0010M\u001a\u00020\u00032\u0006\u0010N\u001a\u00020\u0003H&J\u0010\u0010O\u001a\u00020\u00032\u0006\u0010P\u001a\u00020\u0003H'J\b\u0010Q\u001a\u00020JH&J\u0010\u0010R\u001a\u00020J2\u0006\u0010K\u001a\u00020\u0011H&J\u0010\u0010S\u001a\u00020J2\u0006\u0010K\u001a\u00020\u0001H&J\b\u0010T\u001a\u00020JH&J\b\u0010U\u001a\u00020JH&J\u0010\u0010V\u001a\u00020J2\u0006\u0010W\u001a\u00020XH&R\u0018\u0010\u0002\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\u0004\u0010\u0005\"\u0004\b\u0006\u0010\u0007R\u0012\u0010\b\u001a\u00020\tX¦\u0004¢\u0006\u0006\u001a\u0004\b\n\u0010\u000bR\u0012\u0010\f\u001a\u00020\rX¦\u0004¢\u0006\u0006\u001a\u0004\b\u000e\u0010\u000fR\u0014\u0010\u0010\u001a\u0004\u0018\u00010\u0011X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013R\u0012\u0010\u0014\u001a\u00020\u0015X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0016\u0010\u0017R\u0012\u0010\u0018\u001a\u00020\u0019X¦\u0004¢\u0006\u0006\u001a\u0004\b\u001a\u0010\u001bR\u0014\u0010\u001c\u001a\u0004\u0018\u00010\u001dX¦\u0004¢\u0006\u0006\u001a\u0004\b\u001e\u0010\u001fR\u0012\u0010 \u001a\u00020!X¦\u0004¢\u0006\u0006\u001a\u0004\b\"\u0010#R\u0012\u0010$\u001a\u00020%X¦\u0004¢\u0006\u0006\u001a\u0004\b&\u0010'R\u0014\u0010(\u001a\u00020)8gX¦\u0004¢\u0006\u0006\u001a\u0004\b(\u0010*R\u0012\u0010+\u001a\u00020)X¦\u0004¢\u0006\u0006\u001a\u0004\b+\u0010*R\u0012\u0010,\u001a\u00020)X¦\u0004¢\u0006\u0006\u001a\u0004\b,\u0010*R\u0014\u0010-\u001a\u0004\u0018\u00010\u001dX¦\u0004¢\u0006\u0006\u001a\u0004\b.\u0010\u001fR\u0018\u0010/\u001a\u000200X¦\u000e¢\u0006\f\u001a\u0004\b1\u00102\"\u0004\b3\u00104R\u0012\u00105\u001a\u000200X¦\u0004¢\u0006\u0006\u001a\u0004\b6\u00102R\u0014\u00107\u001a\u0004\u0018\u00010%X¦\u0004¢\u0006\u0006\u001a\u0004\b8\u0010'R\u0012\u00109\u001a\u00020:X¦\u0004¢\u0006\u0006\u001a\u0004\b;\u0010<R\u0018\u0010=\u001a\u000200X¦\u000e¢\u0006\f\u001a\u0004\b>\u00102\"\u0004\b?\u00104R\u0018\u0010@\u001a\u00020)X¦\u000e¢\u0006\f\u001a\u0004\bA\u0010*\"\u0004\bB\u0010CR\u0014\u0010D\u001a\u00020)8gX¦\u0004¢\u0006\u0006\u001a\u0004\bE\u0010*R\u0018\u0010F\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\bG\u0010\u0005\"\u0004\bH\u0010\u0007¨\u0006Y"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/player/IEntityPlayer;", "Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/IEntityLivingBase;", "cameraYaw", "", "getCameraYaw", "()F", "setCameraYaw", "(F)V", "capabilities", "Lnet/ccbluex/liquidbounce/api/minecraft/entity/player/IPlayerCapabilities;", "getCapabilities", "()Lnet/ccbluex/liquidbounce/api/minecraft/entity/player/IPlayerCapabilities;", "displayNameString", "", "getDisplayNameString", "()Ljava/lang/String;", "fishEntity", "Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/IEntity;", "getFishEntity", "()Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/IEntity;", "foodStats", "Lnet/ccbluex/liquidbounce/api/minecraft/util/IFoodStats;", "getFoodStats", "()Lnet/ccbluex/liquidbounce/api/minecraft/util/IFoodStats;", "gameProfile", "Lcom/mojang/authlib/GameProfile;", "getGameProfile", "()Lcom/mojang/authlib/GameProfile;", "heldItem", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemStack;", "getHeldItem", "()Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemStack;", "inventory", "Lnet/ccbluex/liquidbounce/api/minecraft/entity/player/IInventoryPlayer;", "getInventory", "()Lnet/ccbluex/liquidbounce/api/minecraft/entity/player/IInventoryPlayer;", "inventoryContainer", "Lnet/ccbluex/liquidbounce/api/minecraft/inventory/IContainer;", "getInventoryContainer", "()Lnet/ccbluex/liquidbounce/api/minecraft/inventory/IContainer;", "isBlocking", "", "()Z", "isPlayerSleeping", "isUsingItem", "itemInUse", "getItemInUse", "itemInUseCount", "", "getItemInUseCount", "()I", "setItemInUseCount", "(I)V", "itemInUseDuration", "getItemInUseDuration", "openContainer", "getOpenContainer", "prevChasingPosY", "", "getPrevChasingPosY", "()D", "sleepTimer", "getSleepTimer", "setSleepTimer", "sleeping", "getSleeping", "setSleeping", "(Z)V", "spectator", "isSpectator", "speedInAir", "getSpeedInAir", "setSpeedInAir", "attackTargetEntityWithCurrentItem", "", "entity", "fall", "distance", "damageMultiplier", "getCooledAttackStrength", "fl", "jump", "onCriticalHit", "onEnchantmentCritical", "resetCooldown", "stopUsingItem", "triggerAchievement", "stat", "Lnet/ccbluex/liquidbounce/api/minecraft/stats/IStatBase;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/entity/player/IEntityPlayer.class */
public interface IEntityPlayer extends IEntityLivingBase {
    @NotNull
    GameProfile getGameProfile();

    @Nullable
    IEntity getFishEntity();

    @NotNull
    IFoodStats getFoodStats();

    double getPrevChasingPosY();

    int getSleepTimer();

    void setSleepTimer(int i);

    boolean getSleeping();

    void setSleeping(boolean z);

    boolean isPlayerSleeping();

    float getSpeedInAir();

    void setSpeedInAir(float f);

    float getCameraYaw();

    void setCameraYaw(float f);

    @JvmName(name = "isBlocking")
    boolean isBlocking();

    int getItemInUseCount();

    void setItemInUseCount(int i);

    @Nullable
    IItemStack getItemInUse();

    @NotNull
    IPlayerCapabilities getCapabilities();

    @Nullable
    IItemStack getHeldItem();

    boolean isUsingItem();

    @NotNull
    IContainer getInventoryContainer();

    @NotNull
    IInventoryPlayer getInventory();

    @Nullable
    IContainer getOpenContainer();

    int getItemInUseDuration();

    @NotNull
    String getDisplayNameString();

    @JvmName(name = "isSpectator")
    boolean isSpectator();

    void stopUsingItem();

    void onCriticalHit(@NotNull IEntity iEntity);

    void onEnchantmentCritical(@NotNull IEntityLivingBase iEntityLivingBase);

    void attackTargetEntityWithCurrentItem(@NotNull IEntity iEntity);

    void fall(float f, float f2);

    void triggerAchievement(@NotNull IStatBase iStatBase);

    void jump();

    @SupportsMinecraftVersions({MinecraftVersion.MC_1_12})
    float getCooledAttackStrength(float f);

    void resetCooldown();
}
