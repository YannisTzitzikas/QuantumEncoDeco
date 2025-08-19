package com.csd.events;

import com.csd.core.event.Event;

public final class FileProcessingStartedEvent extends Event {
    private final String filePath;

    public FileProcessingStartedEvent(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}