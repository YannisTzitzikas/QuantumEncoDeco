package com.csd.storage.options;

import com.csd.core.storage.GenericStorageStats;
import com.csd.core.storage.StorageOptions;

public final class RocksOptions implements StorageOptions {

    private final String path;
    private final boolean disableWAL;
    private final boolean syncWrites;
    private final int parallelism;
    private final boolean fillCacheOnReads;
    private final int blockCacheMB;
    private final Compression compression;
    private final int multiGetChunk;
    private final GenericStorageStats stats = new GenericStorageStats();

    private RocksOptions(Builder builder) {
        if (builder.path == null || builder.path.trim().isEmpty()) {
            throw new IllegalArgumentException("path required");
        }
        this.path = builder.path;
        this.disableWAL = builder.disableWAL;
        this.syncWrites = builder.syncWrites;
        this.parallelism = builder.parallelism > 0 ? builder.parallelism :
                Math.max(2, Runtime.getRuntime().availableProcessors());
        if (builder.blockCacheMB < 0) {
            throw new IllegalArgumentException("blockCacheMB must be >= 0");
        }
        this.blockCacheMB = builder.blockCacheMB;
        this.compression = builder.compression != null ? builder.compression : Compression.LZ4;
        this.multiGetChunk = builder.multiGetChunk > 0 ? builder.multiGetChunk : 2048;
        this.fillCacheOnReads = builder.fillCacheOnReads;
    }

    public String getPath() {
        return path;
    }

    public boolean isDisableWAL() {
        return disableWAL;
    }

    public boolean isSyncWrites() {
        return syncWrites;
    }

    public int getParallelism() {
        return parallelism;
    }

    public boolean isFillCacheOnReads() {
        return fillCacheOnReads;
    }

    public int getBlockCacheMB() {
        return blockCacheMB;
    }

    public Compression getCompression() {
        return compression;
    }

    public int getMultiGetChunk() {
        return multiGetChunk;
    }

    @Override
    public GenericStorageStats getStats() {
        return stats;
    }

    public static Builder builder(String path) {
        return new Builder(path);
    }

    public static final class Builder {
        private String path;
        private boolean disableWAL = false;
        private boolean syncWrites = false;
        private int parallelism = Math.max(2, Runtime.getRuntime().availableProcessors());
        private boolean fillCacheOnReads = true;
        private int blockCacheMB = 256;
        private Compression compression = Compression.LZ4;
        private int multiGetChunk = 2048;

        public Builder(String path) {
            this.path = path;
        }

        public Builder disableWAL(boolean v) {
            this.disableWAL = v;
            return this;
        }

        public Builder syncWrites(boolean v) {
            this.syncWrites = v;
            return this;
        }

        public Builder parallelism(int v) {
            this.parallelism = v;
            return this;
        }

        public Builder fillCacheOnReads(boolean v) {
            this.fillCacheOnReads = v;
            return this;
        }

        public Builder blockCacheMB(int v) {
            this.blockCacheMB = v;
            return this;
        }

        public Builder compression(Compression c) {
            this.compression = c;
            return this;
        }

        public Builder multiGetChunk(int v) {
            this.multiGetChunk = v;
            return this;
        }

        public RocksOptions build() {
            return new RocksOptions(this);
        }
    }
}