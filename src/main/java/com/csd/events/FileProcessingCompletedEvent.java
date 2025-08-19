package com.csd.events;

import com.csd.core.event.Event;

public final class FileProcessingCompletedEvent extends Event {
    private final String filePath;
    private final long durationNanos;

    public FileProcessingCompletedEvent(String filePath, long durationNanos) {
        this.filePath = filePath;
        this.durationNanos = durationNanos;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getDurationNanos() {
        return durationNanos;
    }
}