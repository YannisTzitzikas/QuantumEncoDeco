package com.csd.events;

import com.csd.core.event.Event;

public final class BatchProcessedEvent extends Event {
    private final int batchSize;
    private final long processingTimeNanos;

    public BatchProcessedEvent(int batchSize, long processingTimeNanos) {
        this.batchSize = batchSize;
        this.processingTimeNanos = processingTimeNanos;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public long getProcessingTimeNanos() {
        return processingTimeNanos;
    }
}