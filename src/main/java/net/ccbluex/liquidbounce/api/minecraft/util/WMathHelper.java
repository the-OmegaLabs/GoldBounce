package net.ccbluex.liquidbounce.api.minecraft.util;

import jdk.nashorn.internal.runtime.PropertyDescriptor;
import kotlin.Metadata;
import kotlin.jvm.JvmStatic;

/* compiled from: WMathHelper.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\"\n\u0002\u0018\u0002\n\u0002\u0010��\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0004\n\u0002\u0010\u0007\n��\n\u0002\u0010\b\n\u0002\b\u0004\bÆ\u0002\u0018��2\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J!\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\u0004H\u0087\bJ!\u0010\b\u001a\u00020\t2\u0006\u0010\u0005\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\t2\u0006\u0010\u0007\u001a\u00020\tH\u0087\bJ\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0004H\u0007J\u0010\u0010\r\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\tH\u0007¨\u0006\u000f"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/util/WMathHelper;", "", "()V", "clamp_double", "", "num", "min", "max", "clamp_float", "", "floor_double", "", PropertyDescriptor.VALUE, "wrapAngleTo180_float", "angle", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/util/WMathHelper.class */
public final class WMathHelper {
    public static final WMathHelper INSTANCE = new WMathHelper();

    private WMathHelper() {
    }

    @JvmStatic
    public static float wrapAngleTo180_float(float angle) {
        float value = angle % 360.0f;
        if (value >= 180.0f) {
            value -= 360.0f;
        }
        if (value < -180.0f) {
            value += 360.0f;
        }
        return value;
    }

    @JvmStatic
    public static float clamp_float(float num, float min, float max) {
        return num < min ? min : num > max ? max : num;
    }

    @JvmStatic
    public static double clamp_double(double num, double min, double max) {
        return num < min ? min : num > max ? max : num;
    }

    @JvmStatic
    public static int floor_double(double value) {
        int i = (int) value;
        return value < ((double) i) ? i - 1 : i;
    }
}
