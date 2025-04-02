package net.ccbluex.liquidbounce.api.minecraft.potion;

import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: IPotion.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u001a\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\bf\u0018��2\u00020\u0001R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0007\u0010\u0005R\u0012\u0010\b\u001a\u00020\tX¦\u0004¢\u0006\u0006\u001a\u0004\b\n\u0010\u000b¨\u0006\f"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/potion/IPotion;", "", "id", "", "getId", "()I", "liquidColor", "getLiquidColor", "name", "", "getName", "()Ljava/lang/String;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/potion/IPotion.class */
public interface IPotion {
    int getLiquidColor();

    int getId();

    @NotNull
    String getName();
}
