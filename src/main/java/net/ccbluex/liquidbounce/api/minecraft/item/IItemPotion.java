package net.ccbluex.liquidbounce.api.minecraft.item;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.potion.IPotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/* compiled from: IItemPotion.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\u001e\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\bf\u0018��2\u00020\u0001J\u0016\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u0005\u001a\u00020\u0006H&¨\u0006\u0007"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemPotion;", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItem;", "getEffects", "", "Lnet/ccbluex/liquidbounce/api/minecraft/potion/IPotionEffect;", "stack", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemStack;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/item/IItemPotion.class */
public interface IItemPotion extends IItem {

    @NotNull
    Collection<IPotionEffect> getEffects(@NotNull IItemStack iItemStack);

    /* compiled from: IItemPotion.kt */
    @Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 3)
    /* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/item/IItemPotion$DefaultImpls.class */
    final class DefaultImpls {
        @NotNull
        public static IItem getItemByID(IItemPotion $this, int id) {
            return IItem.DefaultImpls.getItemByID($this, id);
        }
    }
}
