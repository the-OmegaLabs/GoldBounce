package net.ccbluex.liquidbounce.api.minecraft.entity.player;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack;
import net.ccbluex.liquidbounce.api.util.IWrappedArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: IInventoryPlayer.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u001e\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\r\bf\u0018��2\u00020\u0001J\u0012\u0010\u0011\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0012\u001a\u00020\bH&J\n\u0010\u0013\u001a\u0004\u0018\u00010\u0004H&J\u0012\u0010\u0014\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0012\u001a\u00020\bH&R\u001a\u0010\u0002\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0018\u0010\u0007\u001a\u00020\bX¦\u000e¢\u0006\f\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u001a\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u000e\u0010\u0006R\u001a\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0010\u0010\u0006¨\u0006\u0015"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/entity/player/IInventoryPlayer;", "", "armorInventory", "Lnet/ccbluex/liquidbounce/api/util/IWrappedArray;", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemStack;", "getArmorInventory", "()Lnet/ccbluex/liquidbounce/api/util/IWrappedArray;", "currentItem", "", "getCurrentItem", "()I", "setCurrentItem", "(I)V", "mainInventory", "getMainInventory", "offHandInventory", "getOffHandInventory", "armorItemInSlot", "slot", "getCurrentItemInHand", "getStackInSlot", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/entity/player/IInventoryPlayer.class */
public interface IInventoryPlayer {
    @NotNull
    IWrappedArray<IItemStack> getMainInventory();

    @NotNull
    IWrappedArray<IItemStack> getArmorInventory();

    int getCurrentItem();

    void setCurrentItem(int i);

    @NotNull
    IWrappedArray<IItemStack> getOffHandInventory();

    @Nullable
    IItemStack getStackInSlot(int i);

    @Nullable
    IItemStack armorItemInSlot(int i);

    @Nullable
    IItemStack getCurrentItemInHand();
}
