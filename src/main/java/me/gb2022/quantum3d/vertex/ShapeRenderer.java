package me.gb2022.quantum3d.vertex;

import me.gb2022.quantum3d.vertex.VertexBuilder;

public interface ShapeRenderer {
    static void renderBox(VertexBuilder builder, double x0, double y0, double z0, double x1, double y1, double z1) {
        builder.setColor(0.5, 0.5, 0.5);
        builder.addVertex(x0, y0, z1);
        builder.addVertex(x0, y0, z0);
        builder.addVertex(x1, y0, z0);
        builder.addVertex(x1, y0, z1);
        builder.setColor(0.6, 0.6, 0.6);
        builder.addVertex(x1, y1, z1);
        builder.addVertex(x1, y1, z0);
        builder.addVertex(x0, y1, z0);
        builder.addVertex(x0, y1, z1);
        builder.setColor(0.7, 0.7, 0.7);
        builder.addVertex(x0, y1, z0);
        builder.addVertex(x1, y1, z0);
        builder.addVertex(x1, y0, z0);
        builder.addVertex(x0, y0, z0);
        builder.setColor(0.8, 0.8, 0.8);
        builder.addVertex(x0, y1, z1);
        builder.addVertex(x0, y0, z1);
        builder.addVertex(x1, y0, z1);
        builder.addVertex(x1, y1, z1);
        builder.setColor(0.9, 0.9, 0.9);
        builder.addVertex(x0, y1, z1);
        builder.addVertex(x0, y1, z0);
        builder.addVertex(x0, y0, z0);
        builder.addVertex(x0, y0, z1);
        builder.setColor(1, 1, 1);
        builder.addVertex(x1, y0, z1);
        builder.addVertex(x1, y0, z0);
        builder.addVertex(x1, y1, z0);
        builder.addVertex(x1, y1, z1);
    }

    static void drawRect(VertexBuilder builder, double x0, double x1, double y0, double y1, double z0, double z1) {
        builder.addVertex((float) x0, (float) y1, (float) z1);
        builder.addVertex((float) x1, (float) y1, (float) z0);
        builder.addVertex((float) x1, (float) y0, (float) z0);
        builder.addVertex((float) x0, (float) y0, (float) z1);
    }

    static void drawRectUV(VertexBuilder builder, double x0, double x1, double y0, double y1, double z0, double u0, double u1, double v0, double v1) {
        builder.setTextureCoordinate((float) u1, (float) v0);
        builder.addVertex(x1, y0, z0);
        builder.setTextureCoordinate((float) u0, (float) v0);
        builder.addVertex(x0, y0, z0);
        builder.setTextureCoordinate((float) u0, (float) v1);
        builder.addVertex(x0, y1, z0);
        builder.setTextureCoordinate((float) u1, (float) v1);
        builder.addVertex(x1, y1, z0);
    }
}
