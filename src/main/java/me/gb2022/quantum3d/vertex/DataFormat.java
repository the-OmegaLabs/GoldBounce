package me.gb2022.quantum3d.vertex;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public final class DataFormat {
    public static final DataFormat EMPTY = null;

    public static final DataFormat FLOAT_2 = new DataFormat(2, DataType.FLOAT);
    public static final DataFormat FLOAT_3 = new DataFormat(3, DataType.FLOAT);
    public static final DataFormat FLOAT_4 = new DataFormat(4, DataType.FLOAT);

    private final int size;
    private final DataType type;

    public DataFormat(int size, DataType type) {
        this.size = size;
        this.type = type;
    }

    public static int getDataLength(int vertexCount, DataFormat... fmtList) {
        int len = 0;
        for (DataFormat fmt : fmtList) {
            if (fmt == null) {
                continue;
            }
            len += fmt.getBufferCapacity(vertexCount);
        }
        return len;
    }

    public DataType getType() {
        return this.type;
    }

    public int getSize() {
        return this.size;
    }

    public int getBufferCapacity(int vertexCount) {
        return vertexCount * this.type.getBytes() * this.getSize();
    }

    @Deprecated
    public ByteBuffer createBuffer(int vertexCount) {
        return BufferUtils.createByteBuffer(this.getBufferCapacity(vertexCount));
    }

    @Deprecated
    public void putToBuffer(ByteBuffer buffer, double... data) {
        for (int i = 0; i < this.getSize(); i++) {
            double d = data[i];

            if (this.type == DataType.BYTE || this.type == DataType.UNSIGNED_BYTE) {
                buffer.put((byte) d);
                continue;
            }

            if (this.type == DataType.SHORT || this.type == DataType.UNSIGNED_SHORT) {
                buffer.putShort((short) d);
                continue;
            }

            if (this.type == DataType.INTEGER || this.type == DataType.UNSIGNED_INT) {
                buffer.putInt((int) d);
                continue;
            }

            if (this.type == DataType.LONG || this.type == DataType.UNSIGNED_LONG) {
                buffer.putLong((byte) d);
                continue;
            }
            if (this.type == DataType.FLOAT) {
                buffer.putFloat((float) d);
                continue;
            }
            if (this.type == DataType.DOUBLE) {
                buffer.putDouble(d);
            }
        }
    }

    @Override
    public String toString() {
        return "DataFormat{" +
                "size=" + size +
                ", type=" + type +
                '}';
    }
}
