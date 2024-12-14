package me.gb2022.quantum3d.vertex;

import org.lwjgl.opengl.GL11;

public enum DataType {
    BYTE, UNSIGNED_BYTE,
    SHORT, UNSIGNED_SHORT,
    INTEGER, UNSIGNED_INT,
    LONG, UNSIGNED_LONG,
    FLOAT,
    DOUBLE;

    public int getGlId() {
        if (this == BYTE) return GL11.GL_BYTE;
        else if (this == SHORT) return GL11.GL_SHORT;
        else if (this == FLOAT) return GL11.GL_FLOAT;
        else if (this == DOUBLE) return GL11.GL_DOUBLE;
        else if (this == INTEGER) return GL11.GL_INT;
        else if (this == UNSIGNED_INT) return GL11.GL_UNSIGNED_INT;
        else if (this == UNSIGNED_BYTE) return GL11.GL_UNSIGNED_BYTE;
        else if (this == UNSIGNED_SHORT) return GL11.GL_UNSIGNED_SHORT;
        else throw new RuntimeException("Unexpected DataType: " + this.name());
    }

    public int getBytes() {
        if (this == BYTE || this == UNSIGNED_BYTE) return 1;
        else if (this == SHORT || this == UNSIGNED_SHORT) return 2;
        else if (this == INTEGER || this == UNSIGNED_INT || this == FLOAT) return 4;
        else if (this == LONG || this == UNSIGNED_LONG || this == DOUBLE) return 8;
        else throw new RuntimeException("Unexpected DataType: " + this.name());
    }
}
