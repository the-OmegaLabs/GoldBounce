package net.ccbluex.liquidbounce.api.minecraft.item;

import kotlin.Metadata;
import kotlin.TypeCastException;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

/* compiled from: IItem.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��8\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n��\bf\u0018��2\u00020\u0001J\b\u0010\u0006\u001a\u00020\u0007H&J\b\u0010\b\u001a\u00020\tH&J\b\u0010\n\u001a\u00020\u000bH&J\b\u0010\f\u001a\u00020\rH&J\b\u0010\u000e\u001a\u00020\u000fH&J\u0010\u0010\u0010\u001a\u00020��2\u0006\u0010\u0011\u001a\u00020\u0012H\u0016R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005¨\u0006\u0013"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/item/IItem;", "", "unlocalizedName", "", "getUnlocalizedName", "()Ljava/lang/String;", "asItemArmor", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemArmor;", "asItemBlock", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemBlock;", "asItemBucket", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemBucket;", "asItemPotion", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemPotion;", "asItemSword", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemSword;", "getItemByID", "id", "", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/item/IItem.class */
public interface IItem {
    @NotNull
    String getUnlocalizedName();

    @NotNull
    IItemArmor asItemArmor();

    @NotNull
    IItemPotion asItemPotion();

    @NotNull
    IItemBlock asItemBlock();

    @NotNull
    IItemSword asItemSword();

    @NotNull
    IItemBucket asItemBucket();

    @NotNull
    IItem getItemByID(int i);

    /* compiled from: IItem.kt */
    @Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 3)
    /* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/item/IItem$DefaultImpls.class */
    final class DefaultImpls {
        @NotNull
        public static IItem getItemByID(IItem $this, int id) {
            IItem func_150899_d = Item.func_150899_d(id);
            if (func_150899_d == null) {
                throw new TypeCastException("null cannot be cast to non-null type net.ccbluex.liquidbounce.api.minecraft.item.IItem");
            }
            return func_150899_d;
        }
    }
}
