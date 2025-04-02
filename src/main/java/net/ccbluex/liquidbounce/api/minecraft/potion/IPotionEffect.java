package net.ccbluex.liquidbounce.api.minecraft.potion;

import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

/* compiled from: IPotionEffect.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010\u000e\n��\bf\u0018��2\u00020\u0001J\b\u0010\n\u001a\u00020\u000bH&R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0007\u0010\u0005R\u0012\u0010\b\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\t\u0010\u0005¨\u0006\f"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/potion/IPotionEffect;", "", "amplifier", "", "getAmplifier", "()I", "duration", "getDuration", "potionID", "getPotionID", "getDurationString", "", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/potion/IPotionEffect.class */
public interface IPotionEffect {
    @NotNull
    String getDurationString();

    int getAmplifier();

    int getDuration();

    int getPotionID();
}
