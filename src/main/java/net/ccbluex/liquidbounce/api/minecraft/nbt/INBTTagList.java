package net.ccbluex.liquidbounce.api.minecraft.nbt;

import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: INBTTagList.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\b\n��\n\u0002\u0010\u000b\n\u0002\b\u0002\bf\u0018��2\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0001H&J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\bH&¨\u0006\f"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagList;", "Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTBase;", "appendTag", "", "createNBTTagString", "getCompoundTagAt", "Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagCompound;", "index", "", "hasNoTags", "", "tagCount", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagList.class */
public interface INBTTagList extends INBTBase {
    boolean hasNoTags();

    int tagCount();

    @NotNull
    INBTTagCompound getCompoundTagAt(int i);

    void appendTag(@NotNull INBTBase iNBTBase);
}
