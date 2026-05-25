package com.csd.examples.common.metrics;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * High-performance, lock-free operational telemetry context.
 * Replaces EventBus architecture by leveraging CPU-level atomic instruction updates.
 */
public class PipelineContext {

    private final LongAdder totalTriplesProcessed = new LongAdder();
    private final LongAdder totalBatchesProcessed = new LongAdder();
    
    // Tracks unique element counts extracted during pipeline processing stages
    private final LongAdder uniqueEntitiesCount = new LongAdder();
    private final LongAdder uniquePredicatesCount = new LongAdder();

    // Map for tracing processed file names to their file profiling metrics
    private final Map<String, FileStats> fileMetricsMap = new ConcurrentHashMap<>();

    public void incrementBatches() {
        totalBatchesProcessed.increment();
    }

    public void addTriples(long count) {
        totalTriplesProcessed.add(count);
    }

    public void addUniqueEntities(long count) {
        uniqueEntitiesCount.add(count);
    }

    public void addUniquePredicates(long count) {
        uniquePredicatesCount.add(count);
    }

    /**
     * Inline tracking for tracking raw file processing statistics
     */
    public void recordFileCompletion(String filePath, long durationNanos) {
        fileMetricsMap.put(filePath, new FileStats(filePath, durationNanos));
    }

    // --- High Performance Getters for Reporting Outputs ---
    
    public long getTotalTriples() { return totalTriplesProcessed.sum(); }
    public long getTotalBatches() { return totalBatchesProcessed.sum(); }
    public long getUniqueEntities() { return uniqueEntitiesCount.sum(); }
    public long getUniquePredicates() { return uniquePredicatesCount.sum(); }
    public Map<String, FileStats> getFileStatsMap() { return fileMetricsMap; }

    /**
     * Inner data record representing an immutable file profiling metrics snapshot
     */
    public static class FileStats {
        private final String filePath;
        private final double fileSizeMB;
        private final long processingTimeNanos;

        public FileStats(String filePath, long processingTimeNanos) {
            this.filePath = filePath;
            this.processingTimeNanos = processingTimeNanos;
            this.fileSizeMB = calculateFileSizeInMB(filePath);
        }

        public String getFilePath() { return filePath; }
        public double getFileSizeMB() { return fileSizeMB; }
        public long getProcessingTimeNanos() { return processingTimeNanos; }

        public double getProcessingTimeSeconds() {
            return processingTimeNanos / 1_000_000_000.0;
        }

        public double getMegabytesPerSecond() {
            double seconds = getProcessingTimeSeconds();
            return seconds > 0 ? fileSizeMB / seconds : 0;
        }

        private static double calculateFileSizeInMB(String filePath) {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                return 0.0; // Fail-safe fallback if paths disappear mid-run
            }
            return (double) file.length() / (1024 * 1024);
        }
    }
}