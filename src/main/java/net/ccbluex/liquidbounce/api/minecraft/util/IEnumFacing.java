package net.ccbluex.liquidbounce.api.minecraft.util;

import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: IEnumFacing.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\"\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0005\bf\u0018��2\u00020\u0001J\b\u0010\r\u001a\u00020\u000eH&J\b\u0010\u000f\u001a\u00020\u000eH&J\b\u0010\u0010\u001a\u00020\u000eH&J\b\u0010\u0011\u001a\u00020\u000eH&J\b\u0010\u0012\u001a\u00020\u000eH&R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0007X¦\u0004¢\u0006\u0006\u001a\u0004\b\b\u0010\tR\u0012\u0010\n\u001a\u00020��X¦\u0004¢\u0006\u0006\u001a\u0004\b\u000b\u0010\f¨\u0006\u0013"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/util/IEnumFacing;", "", "axisOrdinal", "", "getAxisOrdinal", "()I", "directionVec", "Lnet/ccbluex/liquidbounce/api/minecraft/util/WVec3i;", "getDirectionVec", "()Lnet/ccbluex/liquidbounce/api/minecraft/util/WVec3i;", "opposite", "getOpposite", "()Lnet/ccbluex/liquidbounce/api/minecraft/util/IEnumFacing;", "isEast", "", "isNorth", "isSouth", "isUp", "isWest", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/util/IEnumFacing.class */
public interface IEnumFacing {
    boolean isNorth();

    boolean isSouth();

    boolean isEast();

    boolean isWest();

    boolean isUp();

    @NotNull
    IEnumFacing getOpposite();

    @NotNull
    WVec3i getDirectionVec();

    int getAxisOrdinal();
}
