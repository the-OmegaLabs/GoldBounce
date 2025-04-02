package net.ccbluex.liquidbounce.api.minecraft.item;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.block.state.IIBlockState;
import net.ccbluex.liquidbounce.api.minecraft.enchantments.IEnchantment;
import net.ccbluex.liquidbounce.api.minecraft.entity.ai.attributes.IAttributeModifier;
import net.ccbluex.liquidbounce.api.minecraft.nbt.INBTBase;
import net.ccbluex.liquidbounce.api.minecraft.nbt.INBTTagCompound;
import net.ccbluex.liquidbounce.api.minecraft.nbt.INBTTagList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/* compiled from: IItemStack.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��n\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u001e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n��\bf\u0018��2\u00020\u0001J\u0018\u0010'\u001a\u00020(2\u0006\u0010)\u001a\u00020*2\u0006\u0010+\u001a\u00020\u000fH&J\u0016\u0010,\u001a\b\u0012\u0004\u0012\u00020.0-2\u0006\u0010/\u001a\u00020\u0003H&J\u0010\u00100\u001a\u0002012\u0006\u00102\u001a\u000203H&J\b\u00104\u001a\u000205H&J\u0010\u00106\u001a\u00020��2\u0006\u0010\u0002\u001a\u00020\u0003H&J\u0018\u00107\u001a\u00020(2\u0006\u0010/\u001a\u00020\u00032\u0006\u00108\u001a\u000209H&R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0014\u0010\u0006\u001a\u0004\u0018\u00010\u0007X¦\u0004¢\u0006\u0006\u001a\u0004\b\b\u0010\tR\u0014\u0010\n\u001a\u0004\u0018\u00010\u000bX¦\u0004¢\u0006\u0006\u001a\u0004\b\f\u0010\rR\u0018\u0010\u000e\u001a\u00020\u000fX¦\u000e¢\u0006\f\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u0012\u0010\u0014\u001a\u00020\u0015X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0016\u0010\u0017R\u0018\u0010\u0018\u001a\u00020\u000fX¦\u000e¢\u0006\f\u001a\u0004\b\u0019\u0010\u0011\"\u0004\b\u001a\u0010\u0013R\u0012\u0010\u001b\u001a\u00020\u000fX¦\u0004¢\u0006\u0006\u001a\u0004\b\u001c\u0010\u0011R\u0012\u0010\u001d\u001a\u00020\u000fX¦\u0004¢\u0006\u0006\u001a\u0004\b\u001e\u0010\u0011R\u001a\u0010\u001f\u001a\u0004\u0018\u00010 X¦\u000e¢\u0006\f\u001a\u0004\b!\u0010\"\"\u0004\b#\u0010$R\u0012\u0010%\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b&\u0010\u0005¨\u0006:"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/item/IItemStack;", "", "displayName", "", "getDisplayName", "()Ljava/lang/String;", "enchantmentTagList", "Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagList;", "getEnchantmentTagList", "()Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagList;", "item", "Lnet/ccbluex/liquidbounce/api/minecraft/item/IItem;", "getItem", "()Lnet/ccbluex/liquidbounce/api/minecraft/item/IItem;", "itemDamage", "", "getItemDamage", "()I", "setItemDamage", "(I)V", "itemDelay", "", "getItemDelay", "()J", "maxDamage", "getMaxDamage", "setMaxDamage", "maxItemUseDuration", "getMaxItemUseDuration", "stackSize", "getStackSize", "tagCompound", "Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagCompound;", "getTagCompound", "()Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagCompound;", "setTagCompound", "(Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagCompound;)V", "unlocalizedName", "getUnlocalizedName", "addEnchantment", "", "enchantment", "Lnet/ccbluex/liquidbounce/api/minecraft/enchantments/IEnchantment;", "level", "getAttributeModifier", "", "Lnet/ccbluex/liquidbounce/api/minecraft/entity/ai/attributes/IAttributeModifier;", "key", "getStrVsBlock", "", "block", "Lnet/ccbluex/liquidbounce/api/minecraft/block/state/IIBlockState;", "isSplash", "", "setStackDisplayName", "setTagInfo", "nbt", "Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTBase;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/item/IItemStack.class */
public interface IItemStack {
    @NotNull
    String getDisplayName();

    @NotNull
    String getUnlocalizedName();

    int getMaxItemUseDuration();

    @Nullable
    INBTTagList getEnchantmentTagList();

    @Nullable
    INBTTagCompound getTagCompound();

    void setTagCompound(@Nullable INBTTagCompound iNBTTagCompound);

    int getStackSize();

    int getItemDamage();

    void setItemDamage(int i);

    int getMaxDamage();

    void setMaxDamage(int i);

    @Nullable
    IItem getItem();

    long getItemDelay();

    float getStrVsBlock(@NotNull IIBlockState iIBlockState);

    void setTagInfo(@NotNull String str, @NotNull INBTBase iNBTBase);

    @NotNull
    IItemStack setStackDisplayName(@NotNull String str);

    void addEnchantment(@NotNull IEnchantment iEnchantment, int i);

    @NotNull
    Collection<IAttributeModifier> getAttributeModifier(@NotNull String str);

    boolean isSplash();
}
