package me.gb2022.quantum3d.vertex;

import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicInteger;

public interface VertexBuilderUploader {
    AtomicInteger UPLOAD_COUNT = new AtomicInteger();

    static void uploadPointer(VertexBuilder builder) {
        VertexFormat format = builder.getFormat();

        DataFormat vertexFormat = format.getVertexFormat();
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        long addr = MemoryUtil.getAddress(builder.generateData());
        int vBytes = format.getVertexBufferSize(1);
        int cBytes = format.getColorBufferSize(1);
        int tBytes = format.getTextureBufferSize(1);
        int nBytes = format.getNormalBufferSize(1);

        int stride = vBytes + cBytes + tBytes + nBytes;

        GL11.glVertexPointer(vertexFormat.getSize(), vertexFormat.getType().getGlId(), stride, addr);

        if (format.hasColorData()) {
            DataFormat fmt = format.getColorFormat();
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
            GL11.glColorPointer(fmt.getSize(), fmt.getType().getGlId(), stride, addr + vBytes);
        }

        if (format.hasTextureData()) {
            DataFormat fmt = format.getTextureFormat();
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GL11.glTexCoordPointer(fmt.getSize(), fmt.getType().getGlId(), stride, addr + vBytes + cBytes);
        }
        if (format.hasNormalData()) {
            DataFormat fmt = format.getNormalFormat();
            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
            GL11.glNormalPointer(fmt.getSize(), fmt.getType().getGlId(), addr + vBytes + cBytes + tBytes);
        }

        GL11.glDrawArrays(builder.getDrawMode().glId(), 0, builder.getVertexCount());

        UPLOAD_COUNT.addAndGet(builder.getVertexCount());

        disableState(format);
    }


    static void enableState(VertexFormat format) {
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        if (format.hasColorData()) {
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }
        if (format.hasTextureData()) {
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }
        if (format.hasNormalData()) {
            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        }
    }

    static void disableState(VertexFormat format) {
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        if (format.hasNormalData()) {
            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        }
        if (format.hasTextureData()) {
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }
        if (format.hasNormalData()) {
            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        }
    }

    static int getUploadedCount() {
        return UPLOAD_COUNT.getAndSet(0);
    }
}
