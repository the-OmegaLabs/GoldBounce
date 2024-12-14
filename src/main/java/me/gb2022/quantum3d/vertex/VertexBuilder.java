package me.gb2022.quantum3d.vertex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;


public final class VertexBuilder {
    public static final Logger LOGGER = LogManager.getLogger("VertexBuilder");

    private final AtomicInteger vertexCount = new AtomicInteger();

    private final VertexBuilderAllocator allocator;
    private final VertexFormat format;
    private final DrawMode drawMode;
    private final int capacity;
    private ByteBuffer data;

    private double[] colorCache;
    private double[] textureCache;
    private double[] normalCache;

    public VertexBuilder(VertexFormat format, int capacity, DrawMode drawMode, VertexBuilderAllocator memoryAllocator) {
        this.format = format;
        this.drawMode = drawMode;
        this.capacity = capacity;
        this.allocator = memoryAllocator;
    }

    public void allocate() {
        int cap = this.getCapacity();

        this.data = BufferUtils.createByteBuffer(this.format.getRawBufferSize(cap));

        if (this.format.hasColorData()) {
            this.colorCache = new double[this.format.getColorFormat().getSize()];
        }
        if (this.format.hasTextureData()) {
            this.textureCache = new double[this.format.getTextureFormat().getSize()];
        }
        if (this.format.hasNormalData()) {
            this.normalCache = new double[this.format.getNormalFormat().getSize()];
        }

        if (this.colorCache == null) {
            return;
        }
        Arrays.fill(this.colorCache, 1.0f);
    }

    public void free() {
//        ((ByteBuffer) this.data).cleaner().clean();

        this.allocator.clearInstance(this);
    }

    public void addVertex(double... data) {
        if (this.vertexCount.get() >= this.capacity) {
            throw new RuntimeException("Builder overflowed: reached capacity " + this.capacity);
        }
        this.vertexCount.incrementAndGet();
        VertexFormat.putData(this.format.getVertexFormat(), this.data, data);
        VertexFormat.putData(this.format.getColorFormat(), this.data, this.colorCache);
        VertexFormat.putData(this.format.getTextureFormat(), this.data, this.textureCache);
        VertexFormat.putData(this.format.getNormalFormat(), this.data, this.normalCache);
    }

    public VertexBuilder setColor(double... data) {
        this.colorCache = data;
        return this;
    }

    public VertexBuilder setTextureCoordinate(double... data) {
        this.textureCache = data;
        return this;
    }

    public VertexBuilder setNormal(double... data) {
        this.normalCache = data;
        return this;
    }

    public int getVertexCount() {
        return this.vertexCount.get();
    }

    public DrawMode getDrawMode() {
        return this.drawMode;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void reset() {
        this.vertexCount.set(0);
        if (this.colorCache != null) {
            Arrays.fill(this.colorCache, 1.0f);
        }
        this.data.clear().position(0);
    }

    public VertexFormat getFormat() {
        return format;
    }

    public ByteBuffer getData() {
        return data;
    }

    public ByteBuffer generateData() {
        return getData();
    }

    public VertexBuilderAllocator getMemoryAllocator() {
        return allocator;
    }

    @Override
    protected void finalize() {
        LOGGER.warn("unexpected free: {}[v={}]", hashCode(), this.getCapacity());

        this.free();
    }
}
