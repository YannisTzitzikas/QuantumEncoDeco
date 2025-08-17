package com.csd.core.storage;

import java.util.concurrent.atomic.AtomicLong;

public class GenericStorageStats {
    private final AtomicLong elementCount = new AtomicLong();
    private volatile long lastUpdatedEpochMs;

    public long getElementCount() {
        return elementCount.get();
    }

    public void setElementCount(long count) {
        elementCount.set(count);
        this.lastUpdatedEpochMs = System.currentTimeMillis();
    }

    public void incrementElementCount(long delta) {
        elementCount.addAndGet(delta);
        this.lastUpdatedEpochMs = System.currentTimeMillis();
    }

    public long getLastUpdatedEpochMs() {
        return lastUpdatedEpochMs;
    }
}
