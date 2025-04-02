package net.ccbluex.liquidbounce.api.minecraft.client.renderer.vertex;

import kotlin.Metadata;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/* compiled from: IVertexBuffer.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\"\n\u0002\u0018\u0002\n\u0002\u0010��\n��\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\bf\u0018��2\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\u0010\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\u0003H&J\u0010\u0010\b\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\u0003H&¨\u0006\f"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/renderer/vertex/IVertexBuffer;", "", "bindBuffer", "", "bufferData", "buffer", "Ljava/nio/ByteBuffer;", "deleteGlBuffers", "drawArrays", "mode", "", "unbindBuffer", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/renderer/vertex/IVertexBuffer.class */
public interface IVertexBuffer {
    void deleteGlBuffers();

    void bindBuffer();

    void drawArrays(int i);

    void unbindBuffer();

    void bufferData(@NotNull ByteBuffer byteBuffer);
}
