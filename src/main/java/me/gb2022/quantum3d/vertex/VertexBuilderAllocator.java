package me.gb2022.quantum3d.vertex;

import java.util.concurrent.atomic.AtomicInteger;

public final class VertexBuilderAllocator {
    private final AtomicInteger counter = new AtomicInteger();

    public VertexBuilderAllocator() {
    }

    public void free(VertexBuilder builder) {
        if (builder.getMemoryAllocator() != this) {
            throw new IllegalArgumentException("not from here");
        }
        builder.free();
    }

    public VertexBuilder create(VertexFormat format, DrawMode mode, int capacity) {
        this.counter.incrementAndGet();
        return new VertexBuilder(format, capacity, mode, this);
    }

    public VertexBuilder allocate(VertexFormat format, DrawMode mode, int capacity) {
        this.counter.incrementAndGet();
        VertexBuilder b = new VertexBuilder(format, capacity, mode, this);
        b.allocate();
        return b;
    }

    public AtomicInteger getCounter() {
        return counter;
    }

    public void clearInstance(VertexBuilder vertexBuilder) {
        this.counter.decrementAndGet();
    }
}
