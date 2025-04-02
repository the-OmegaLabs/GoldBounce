package net.ccbluex.liquidbounce.api.minecraft.client.render;

import kotlin.Metadata;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

/* compiled from: WIImageBuffer.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u0018\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n��\bf\u0018��2\u00020\u0001J\u0014\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003H&J\b\u0010\u0005\u001a\u00020\u0006H&¨\u0006\u0007"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/render/WIImageBuffer;", "", "parseUserSkin", "Ljava/awt/image/BufferedImage;", "image", "skinAvailable", "", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/render/WIImageBuffer.class */
public interface WIImageBuffer {
    @Nullable
    BufferedImage parseUserSkin(@Nullable BufferedImage bufferedImage);

    void skinAvailable();
}
