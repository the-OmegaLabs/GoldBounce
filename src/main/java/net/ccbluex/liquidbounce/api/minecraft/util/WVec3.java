package net.ccbluex.liquidbounce.api.minecraft.util;

import kotlin.Metadata;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: WVec3.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��2\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0004\u0018��2\u00020\u0001B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004B\u001d\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\u0006\u0010\b\u001a\u00020\u0006¢\u0006\u0002\u0010\tJ\u0011\u0010\u000e\u001a\u00020��2\u0006\u0010\u000f\u001a\u00020��H\u0086\bJ!\u0010\u0010\u001a\u00020��2\u0006\u0010\u0011\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u0006H\u0086\bJ\u000e\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020��J\u0013\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0001H\u0096\u0002J\b\u0010\u0018\u001a\u00020\u0019H\u0016J\u000e\u0010\u001a\u001a\u00020��2\u0006\u0010\u001b\u001a\u00020\u001cJ\u000e\u0010\u001d\u001a\u00020��2\u0006\u0010\u001e\u001a\u00020\u001cJ\u0011\u0010\u001f\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020��H\u0086\bR\u0011\u0010\u0005\u001a\u00020\u0006¢\u0006\b\n��\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0007\u001a\u00020\u0006¢\u0006\b\n��\u001a\u0004\b\f\u0010\u000bR\u0011\u0010\b\u001a\u00020\u0006¢\u0006\b\n��\u001a\u0004\b\r\u0010\u000b¨\u0006 "}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/util/WVec3;", "", "blockPos", "Lnet/ccbluex/liquidbounce/api/minecraft/util/WVec3i;", "(Lnet/ccbluex/liquidbounce/api/minecraft/util/WVec3i;)V", "xCoord", "", "yCoord", "zCoord", "(DDD)V", "getXCoord", "()D", "getYCoord", "getZCoord", "add", "vec", "addVector", "x", "y", "z", "distanceTo", "equals", "", "other", "hashCode", "", "rotatePitch", "pitch", "", "rotateYaw", "yaw", "squareDistanceTo", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/util/WVec3.class */
public final class WVec3 {
    private final double xCoord;
    private final double yCoord;
    private final double zCoord;

    public WVec3(double xCoord, double yCoord, double zCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = zCoord;
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public WVec3(@NotNull WVec3i blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Intrinsics.checkParameterIsNotNull(blockPos, "blockPos");
    }

    public double getXCoord() {
        return this.xCoord;
    }

    public double getYCoord() {
        return this.yCoord;
    }

    public double getZCoord() {
        return this.zCoord;
    }

    @NotNull
    public WVec3 addVector(double x, double y, double z) {
        return new WVec3(getXCoord() + x, getYCoord() + y, getZCoord() + z);
    }

    public double distanceTo(@NotNull WVec3 vec) {
        Intrinsics.checkParameterIsNotNull(vec, "vec");
        double d0 = vec.xCoord - this.xCoord;
        double d1 = vec.yCoord - this.yCoord;
        double d2 = vec.zCoord - this.zCoord;
        return Math.sqrt((d0 * d0) + (d1 * d1) + (d2 * d2));
    }

    public double squareDistanceTo(@NotNull WVec3 vec) {
        Intrinsics.checkParameterIsNotNull(vec, "vec");
        double d0 = vec.getXCoord() - getXCoord();
        double d1 = vec.getYCoord() - getYCoord();
        double d2 = vec.getZCoord() - getZCoord();
        return (d0 * d0) + (d1 * d1) + (d2 * d2);
    }

    @NotNull
    public WVec3 add(@NotNull WVec3 vec) {
        Intrinsics.checkParameterIsNotNull(vec, "vec");
        double x$iv = vec.getXCoord();
        double y$iv = vec.getYCoord();
        double z$iv = vec.getZCoord();
        return new WVec3(getXCoord() + x$iv, getYCoord() + y$iv, getZCoord() + z$iv);
    }

    @NotNull
    public WVec3 rotatePitch(float pitch) {
        float f = (float) Math.cos(pitch);
        float f1 = (float) Math.sin(pitch);
        double d0 = this.xCoord;
        double d1 = (this.yCoord * f) + (this.zCoord * f1);
        double d2 = (this.zCoord * f) - (this.yCoord * f1);
        return new WVec3(d0, d1, d2);
    }

    @NotNull
    public WVec3 rotateYaw(float yaw) {
        float f = (float) Math.cos(yaw);
        float f1 = (float) Math.sin(yaw);
        double d0 = (this.xCoord * f) + (this.zCoord * f1);
        double d1 = this.yCoord;
        double d2 = (this.zCoord * f) - (this.xCoord * f1);
        return new WVec3(d0, d1, d2);
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!Intrinsics.areEqual(getClass(), other != null ? other.getClass() : null)) {
            return false;
        }
        if (other == null) {
            throw new TypeCastException("null cannot be cast to non-null type net.ccbluex.liquidbounce.api.minecraft.util.WVec3");
        }
        return this.xCoord == ((WVec3) other).xCoord && this.yCoord == ((WVec3) other).yCoord && this.zCoord == ((WVec3) other).zCoord;
    }

    public int hashCode() {
        int result = Double.hashCode(this.xCoord);
        return (31 * ((31 * result) + Double.hashCode(this.yCoord))) + Double.hashCode(this.zCoord);
    }
}
