package com.csd.services;

public class BatchSizeMonitor {
    
    private final int DEFAULT_MAX_SIZE = 0x400;
    private final int maxBatchSize;

    public BatchSizeMonitor() {
        this.maxBatchSize = DEFAULT_MAX_SIZE; 
    }

    public BatchSizeMonitor(int maxBatchSize) {
        this.maxBatchSize = (maxBatchSize <= 0) ? DEFAULT_MAX_SIZE : maxBatchSize ; 
    }

    public boolean isBatchLimitExceeded(int currentBatchSize) {
        return currentBatchSize >= maxBatchSize;
    }

    public boolean shouldFlush(int currentSize, int newElements) {
        return (currentSize + newElements) >= maxBatchSize;
    }
}