package com.csd.storage.options;

import com.csd.core.storage.StorageOptions;
import com.csd.core.storage.GenericStorageStats;

public final class InMemoryOptions implements StorageOptions {

    private final int initialCapacity;
    private final float loadFactor;
    private final GenericStorageStats stats = new GenericStorageStats();

    private InMemoryOptions(Builder builder) {
        this.initialCapacity = builder.initialCapacity;
        this.loadFactor = builder.loadFactor;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public float getLoadFactor() {
        return loadFactor;
    }

    @Override
    public GenericStorageStats getStats() {
        return stats;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int initialCapacity = 16_384;
        private float loadFactor = 0.75f;

        public Builder initialCapacity(int v) {
            this.initialCapacity = v;
            return this;
        }

        public Builder loadFactor(float v) {
            this.loadFactor = v;
            return this;
        }

        public InMemoryOptions build() {
            return new InMemoryOptions(this);
        }
    }
}
