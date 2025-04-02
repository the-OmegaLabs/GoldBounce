package net.ccbluex.liquidbounce.api.minecraft.item;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.minecraft.IArmorMaterial;
import org.jetbrains.annotations.NotNull;

/* compiled from: IItemArmor.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"�� \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n��\bf\u0018��2\u00020\u0001J\u0010\u0010\f\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\u000eH&R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0007X¦\u0004¢\u0006\u0006\u001a\u0004\b\b\u0010\tR\u0012\u0010\n\u001a\u00020\u0007X¦\u0004¢\u0006\u0006\u001a\u0004\b\u000b\u0010\t¨\u0006\u000f"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemArmor;", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItem;", "armorMaterial", "Lnet/ccbluex/liquidbounce/api/minecraft/minecraft/IArmorMaterial;", "getArmorMaterial", "()Lnet/ccbluex/liquidbounce/api/minecraft/minecraft/IArmorMaterial;", "armorType", "", "getArmorType", "()I", "damageReduceAmount", "getDamageReduceAmount", "getColor", "stack", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemStack;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/item/IItemArmor.class */
public interface IItemArmor extends IItem {

    int getDamageReduceAmount();

    @NotNull
    IArmorMaterial getArmorMaterial();

    int getArmorType();

    int getColor(@NotNull IItemStack iItemStack);

    /* compiled from: IItemArmor.kt */
    @Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 3)
    /* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/item/IItemArmor$DefaultImpls.class */
    final class DefaultImpls {
        @NotNull
        public static IItem getItemByID(IItemArmor $this, int id) {
            return IItem.DefaultImpls.getItemByID($this, id);
        }
    }
}
