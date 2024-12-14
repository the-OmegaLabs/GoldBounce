package me.gb2022.quantum3d.vertex;

import java.nio.ByteBuffer;

@SuppressWarnings("ClassCanBeRecord")
public class VertexFormat {
    public static final VertexFormat V3F_C4F = new VertexFormat(DataFormat.FLOAT_3, null, DataFormat.FLOAT_4, null);
    public static final VertexFormat V3F_C3F = new VertexFormat(DataFormat.FLOAT_3, null, DataFormat.FLOAT_3, null);
    public static final VertexFormat V3F_C4F_T2F = new VertexFormat(DataFormat.FLOAT_3, DataFormat.FLOAT_2, DataFormat.FLOAT_4, null);
    public static final VertexFormat V3F_C3F_T2F = new VertexFormat(DataFormat.FLOAT_3, DataFormat.FLOAT_2, DataFormat.FLOAT_3, null);
    public static final VertexFormat V3F_T2F = new VertexFormat(DataFormat.FLOAT_3, DataFormat.FLOAT_2, null, null);

    private final DataFormat vertexFormat;
    private final DataFormat textureFormat;
    private final DataFormat colorFormat;
    private final DataFormat normalFormat;

    public VertexFormat(DataFormat vertexFormat, DataFormat textureFormat, DataFormat colorFormat, DataFormat normalFormat) {
        this.vertexFormat = vertexFormat;
        this.textureFormat = textureFormat;
        this.colorFormat = colorFormat;
        this.normalFormat = normalFormat;
    }

    public static void putData(DataFormat format, ByteBuffer dataBuffer, double... data) {
        if (format == null) {
            return;
        }
        if (data.length != format.getSize()) {
            throw new IllegalArgumentException(String.format("non-match data size:%d -> %d", data.length, format.getSize()));
        }


        for (double d : data) {
            if (format.getType() == DataType.BYTE || format.getType() == DataType.UNSIGNED_BYTE) {
                dataBuffer.put((byte) d);
            } else if (format.getType() == DataType.SHORT || format.getType() == DataType.UNSIGNED_SHORT) {
                dataBuffer.putShort((short) d);
            } else if (format.getType() == DataType.INTEGER || format.getType() == DataType.UNSIGNED_INT) {
                dataBuffer.putInt((int) d);
            } else if (format.getType() == DataType.LONG || format.getType() == DataType.UNSIGNED_LONG) {
                dataBuffer.putLong((long) d);
            } else if (format.getType() == DataType.FLOAT) {
                dataBuffer.putFloat((float) d);
            } else if (format.getType() == DataType.DOUBLE) {
                dataBuffer.putDouble(d);
            }
        }
    }

    // Format getters
    public DataFormat getColorFormat() {
        return colorFormat;
    }

    public DataFormat getNormalFormat() {
        return normalFormat;
    }

    public DataFormat getTextureFormat() {
        return textureFormat;
    }

    public DataFormat getVertexFormat() {
        return vertexFormat;
    }

    // Data existence checks
    public boolean hasColorData() {
        return this.colorFormat != null;
    }

    public boolean hasTextureData() {
        return this.textureFormat != null;
    }

    public boolean hasNormalData() {
        return this.normalFormat != null;
    }

    // Buffer size calculations
    public int getVertexBufferSize(int vertexCount) {
        return this.getVertexFormat().getBufferCapacity(vertexCount);
    }

    public int getColorBufferSize(int vertexCount) {
        if (!this.hasColorData()) {
            return 0;
        }
        return this.getColorFormat().getBufferCapacity(vertexCount);
    }

    public int getTextureBufferSize(int vertexCount) {
        if (!this.hasTextureData()) {
            return 0;
        }
        return this.getTextureFormat().getBufferCapacity(vertexCount);
    }

    public int getNormalBufferSize(int vertexCount) {
        if (!this.hasNormalData()) {
            return 0;
        }
        return this.getNormalFormat().getBufferCapacity(vertexCount);
    }

    public int getRawBufferSize(int vertexCount) {
        return this.getVertexBufferSize(vertexCount)
                + this.getColorBufferSize(vertexCount)
                + this.getTextureBufferSize(vertexCount)
                + this.getNormalBufferSize(vertexCount);
    }

    @Override
    public String toString() {
        return "VertexFormat{" +
                "vertexFormat=" + vertexFormat +
                ", textureFormat=" + textureFormat +
                ", colorFormat=" + colorFormat +
                ", normalFormat=" + normalFormat +
                '}';
    }

    public int getTotalBytes() {
        int size = this.vertexFormat.getSize() * this.vertexFormat.getType().getBytes();

        if (this.hasColorData()) {
            size += this.getColorFormat().getSize() * this.colorFormat.getType().getBytes();
        }
        if (this.hasTextureData()) {
            size += this.getTextureFormat().getSize() * this.textureFormat.getType().getBytes();
        }
        if (this.hasNormalData()) {
            size += this.getNormalFormat().getSize() * this.normalFormat.getType().getBytes();
        }

        return size;
    }
}
