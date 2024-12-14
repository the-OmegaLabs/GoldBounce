package me.gb2022.quantum3d.vertex;

import org.lwjgl.opengl.GL11;

public enum DrawMode {
    POINTS,
    LINES,
    LINE_STRIP,
    TRIANGLES,
    TRIANGLE_FAN,
    QUADS,
    QUAD_STRIP;

    public int glId() {
        if (this == LINES) return GL11.GL_LINES;
        else if (this == QUADS) return GL11.GL_QUADS;
        else if (this == QUAD_STRIP) return GL11.GL_QUAD_STRIP;
        else if (this == POINTS) return GL11.GL_POINTS;
        else if (this == TRIANGLE_FAN) return GL11.GL_TRIANGLE_FAN;
        else if (this == TRIANGLES) return GL11.GL_TRIANGLES;
        else if (this == LINE_STRIP) return GL11.GL_LINE_STRIP;
        else throw new RuntimeException("Unexpected DrawMode: " + this.name());
    }
}
