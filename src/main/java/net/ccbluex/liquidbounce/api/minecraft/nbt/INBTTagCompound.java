package net.ccbluex.liquidbounce.api.minecraft.nbt;

import jdk.nashorn.internal.runtime.PropertyDescriptor;
import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: INBTTagCompound.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\n\n��\n\u0002\u0010\u000e\n��\n\u0002\u0010\u000b\n��\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\bf\u0018��2\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0004\u001a\u00020\u0005H&J\u0018\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\fH&J\u0018\u0010\r\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\u0005H&J\u0018\u0010\u000e\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\u0001H&¨\u0006\u0010"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagCompound;", "Lnet/ccbluex/liquidbounce/api/minecraft/nbt/INBTBase;", "getShort", "", "name", "", "hasKey", "", "setInteger", "", "key", PropertyDescriptor.VALUE, "", "setString", "setTag", "tag", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/nbt/INBTTagCompound.class */
public interface INBTTagCompound extends INBTBase {
    boolean hasKey(@NotNull String str);

    short getShort(@NotNull String str);

    void setString(@NotNull String str, @NotNull String str2);

    void setTag(@NotNull String str, @NotNull INBTBase iNBTBase);

    void setInteger(@NotNull String str, int i);
}
