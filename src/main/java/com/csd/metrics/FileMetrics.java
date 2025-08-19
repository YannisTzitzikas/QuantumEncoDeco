package com.csd.metrics;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.csd.core.event.EventBus;
import com.csd.events.FileProcessingCompletedEvent;
import com.csd.events.FileProcessingStartedEvent;

public class FileMetrics {
    private final AtomicLong totalFilesProcessed = new AtomicLong(0);
    private final AtomicLong totalProcessingTimeNanos = new AtomicLong(0);
    private final Map<String, FileStats> fileStats = new ConcurrentHashMap<>();
    
    public FileMetrics(EventBus eventBus) {
        if (eventBus != null) {
            registerEventHandlers(eventBus);
        }
    }
    
    private void registerEventHandlers(EventBus eventBus) {
        eventBus.subscribe(FileProcessingStartedEvent.class, this::handleFileProcessingStarted);
        eventBus.subscribe(FileProcessingCompletedEvent.class, this::handleFileProcessingCompleted);
    }
    
    private void handleFileProcessingStarted(FileProcessingStartedEvent event) {
        fileStats.put(event.getFilePath(), new FileStats(event.getFilePath()));
    }
    
    private void handleFileProcessingCompleted(FileProcessingCompletedEvent event) {
        totalFilesProcessed.incrementAndGet();
        totalProcessingTimeNanos.addAndGet(event.getDurationNanos());
        
        FileStats stats = fileStats.get(event.getFilePath());
        if (stats != null) {
            stats.setProcessingTimeNanos(event.getDurationNanos());
        }
    }
    
    // Getters
    public long getTotalFilesProcessed() { return totalFilesProcessed.get(); }
    public long getTotalProcessingTimeNanos() { return totalProcessingTimeNanos.get(); }
    public Map<String, FileStats> getFileStats() { return fileStats; }
    
    public static class FileStats {
        private final String filePath;
        private final double fileSizeBytes;
        private long processingTimeNanos;
        
        public FileStats(String filePath) {
            this.filePath = filePath;
            this.fileSizeBytes = getFileSizeInMB(filePath);
        }
        
        public void setProcessingTimeNanos(long processingTimeNanos) {
            this.processingTimeNanos = processingTimeNanos;
        }
        
        // Getters
        public String getFilePath() { return filePath; }
        public double getFileSizeBytes() { return fileSizeBytes; }
        public long getProcessingTimeNanos() { return processingTimeNanos; }
        
        public double getProcessingTimeSeconds() {
            return processingTimeNanos / 1_000_000_000.0;
        }
        
        public double getBytesPerSecond() {
            return fileSizeBytes / getProcessingTimeSeconds();
        }
    }

    private static double getFileSizeInMB(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File not found or is not a valid file: " + filePath);
        }
        long bytes = file.length();
        double megabytes = (double) bytes / (1024 * 1024);
        return megabytes;
    }
}