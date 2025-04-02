package net.ccbluex.liquidbounce.api.minecraft.util;

import kotlin.Metadata;
import kotlin.jvm.JvmOverloads;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import net.ccbluex.liquidbounce.api.enums.EnumFacingType;
import net.ccbluex.liquidbounce.api.minecraft.client.block.IBlock;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity;
import net.ccbluex.liquidbounce.injection.backend.WrapperImpl;
import net.ccbluex.liquidbounce.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: WBlockPos.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��0\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0006\n\u0002\b\u0004\n\u0002\u0018\u0002\n��\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018�� \u00192\u00020\u0001:\u0001\u0019B\u001f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003¢\u0006\u0002\u0010\u0006B\u000f\b\u0016\u0012\u0006\u0010\u0007\u001a\u00020\b¢\u0006\u0002\u0010\tB\u001d\u0012\u0006\u0010\u0002\u001a\u00020\n\u0012\u0006\u0010\u0004\u001a\u00020\n\u0012\u0006\u0010\u0005\u001a\u00020\n¢\u0006\u0002\u0010\u000bJ\u001e\u0010\f\u001a\u00020��2\u0006\u0010\u0002\u001a\u00020\n2\u0006\u0010\u0004\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\nJ\u0006\u0010\r\u001a\u00020��J\u000e\u0010\r\u001a\u00020��2\u0006\u0010\u000e\u001a\u00020\nJ\u0006\u0010\u000f\u001a\u00020��J\u000e\u0010\u000f\u001a\u00020��2\u0006\u0010\u000e\u001a\u00020\nJ\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011J\u0006\u0010\u0012\u001a\u00020��J\u000e\u0010\u0012\u001a\u00020��2\u0006\u0010\u000e\u001a\u00020\nJ\u001a\u0010\u0013\u001a\u00020��2\u0006\u0010\u0014\u001a\u00020\u00152\b\b\u0002\u0010\u000e\u001a\u00020\nH\u0007J\u0006\u0010\u0016\u001a\u00020��J\u000e\u0010\u0016\u001a\u00020��2\u0006\u0010\u000e\u001a\u00020\nJ\u0006\u0010\u0017\u001a\u00020��J\u000e\u0010\u0017\u001a\u00020��2\u0006\u0010\u000e\u001a\u00020\nJ\u0006\u0010\u0018\u001a\u00020��J\u000e\u0010\u0018\u001a\u00020��2\u0006\u0010\u000e\u001a\u00020\n¨\u0006\u001a"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/util/WBlockPos;", "Lnet/ccbluex/liquidbounce/api/minecraft/util/WVec3i;", "x", "", "y", "z", "(DDD)V", "source", "Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/IEntity;", "(Lnet/ccbluex/liquidbounce/api/minecraft/client/entity/IEntity;)V", "", "(III)V", "add", "down", "n", "east", "getBlock", "Lnet/ccbluex/liquidbounce/api/minecraft/client/block/IBlock;", "north", "offset", "side", "Lnet/ccbluex/liquidbounce/api/minecraft/util/IEnumFacing;", "south", "up", "west", "Companion", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/util/WBlockPos.class */
public final class WBlockPos extends WVec3i {
    public static final Companion Companion = new Companion(null);

    @NotNull
    private static final WBlockPos ORIGIN = new WBlockPos(0, 0, 0);

    public WBlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    public WBlockPos(double x, double y, double z) {
        this((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public WBlockPos(@NotNull IEntity source) {
        this(source.getPosX(), source.getPosY(), source.getPosZ());
        Intrinsics.checkParameterIsNotNull(source, "source");
    }

    public static /* synthetic */ WBlockPos offset$default(WBlockPos wBlockPos, IEnumFacing iEnumFacing, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            i = 1;
        }
        return wBlockPos.offset(iEnumFacing, i);
    }

    @JvmOverloads
    @NotNull
    public WBlockPos offset(@NotNull IEnumFacing side) {
        return offset$default(this, side, 0, 2, null);
    }

    @NotNull
    public WBlockPos add(int x, int y, int z) {
        return (x == 0 && y == 0 && z == 0) ? this : new WBlockPos(getX() + x, getY() + y, getZ() + z);
    }

    @JvmOverloads
    @NotNull
    public WBlockPos offset(@NotNull IEnumFacing side, int n) {
        Intrinsics.checkParameterIsNotNull(side, "side");
        return n == 0 ? this : new WBlockPos(getX() + (side.getDirectionVec().getX() * n), getY() + (side.getDirectionVec().getY() * n), getZ() + (side.getDirectionVec().getZ() * n));
    }

    @NotNull
    public WBlockPos up() {
        return up(1);
    }

    @NotNull
    public WBlockPos up(int n) {
        return offset(WrapperImpl.INSTANCE.getClassProvider().getEnumFacing(EnumFacingType.UP), n);
    }

    @NotNull
    public WBlockPos down() {
        return down(1);
    }

    @NotNull
    public WBlockPos down(int n) {
        return offset(WrapperImpl.INSTANCE.getClassProvider().getEnumFacing(EnumFacingType.DOWN), n);
    }

    @NotNull
    public WBlockPos west() {
        return west(1);
    }

    @NotNull
    public WBlockPos west(int n) {
        return offset(WrapperImpl.INSTANCE.getClassProvider().getEnumFacing(EnumFacingType.WEST), n);
    }

    @NotNull
    public WBlockPos east() {
        return east(1);
    }

    @NotNull
    public WBlockPos east(int n) {
        return offset(WrapperImpl.INSTANCE.getClassProvider().getEnumFacing(EnumFacingType.EAST), n);
    }

    @NotNull
    public WBlockPos north() {
        return north(1);
    }

    @NotNull
    public WBlockPos north(int n) {
        return offset(WrapperImpl.INSTANCE.getClassProvider().getEnumFacing(EnumFacingType.NORTH), n);
    }

    @NotNull
    public WBlockPos south() {
        return south(1);
    }

    @NotNull
    public WBlockPos south(int n) {
        return offset(WrapperImpl.INSTANCE.getClassProvider().getEnumFacing(EnumFacingType.SOUTH), n);
    }

    @Nullable
    public IBlock getBlock() {
        return BlockUtils.getBlock(this);
    }

    /* compiled from: WBlockPos.kt */
    @Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u0014\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018��2\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004¢\u0006\b\n��\u001a\u0004\b\u0005\u0010\u0006¨\u0006\u0007"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/util/WBlockPos$Companion;", "", "()V", "ORIGIN", "Lnet/ccbluex/liquidbounce/api/minecraft/util/WBlockPos;", "getORIGIN", "()Lnet/ccbluex/liquidbounce/api/minecraft/util/WBlockPos;", "Pride"})
    /* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/util/WBlockPos$Companion.class */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        @NotNull
        public WBlockPos getORIGIN() {
            return WBlockPos.ORIGIN;
        }
    }
}
