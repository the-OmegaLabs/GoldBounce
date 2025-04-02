package net.ccbluex.liquidbounce.api.minecraft.item;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.client.block.IBlock;
import org.jetbrains.annotations.NotNull;

/* compiled from: IItemBucket.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018��2\u00020\u0001R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0002\u0010\u0004¨\u0006\u0005"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemBucket;", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItem;", "isFull", "Lnet/ccbluex/liquidbounce/api/minecraft/client/block/IBlock;", "()Lnet/ccbluex/liquidbounce/api/minecraft/client/block/IBlock;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/item/IItemBucket.class */
public interface IItemBucket extends IItem {

    @NotNull
    IBlock isFull();

    /* compiled from: IItemBucket.kt */
    @Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 3)
    /* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/item/IItemBucket$DefaultImpls.class */
    final class DefaultImpls {
        @NotNull
        public static IItem getItemByID(IItemBucket $this, int id) {
            return IItem.DefaultImpls.getItemByID($this, id);
        }
    }
}
