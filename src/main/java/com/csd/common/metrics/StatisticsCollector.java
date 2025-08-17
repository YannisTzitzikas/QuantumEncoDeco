package com.csd.common.metrics;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

public class StatisticsCollector {

    // -------- Global counters (thread-safe, high-contention friendly)
    private final LongAdder totalTriples       = new LongAdder();
    private final LongAdder totalComponents    = new LongAdder();
    private final LongAdder uniquePredicates   = new LongAdder();
    private final LongAdder uniqueEntities     = new LongAdder();
    private final LongAdder uniqueComponents   = new LongAdder();
    private final LongAdder batchCount         = new LongAdder();
    private final LongAdder fileCount          = new LongAdder();

    private final LongAdder totalTimeNs        = new LongAdder();
    private final LongAccumulator minTimeNs    = new LongAccumulator(Math::min, Long.MAX_VALUE);
    private final LongAccumulator maxTimeNs    = new LongAccumulator(Math::max, Long.MIN_VALUE);

    // -------- Per-file aggregation
    private static final class FileStats {
        final LongAdder triples = new LongAdder();
        final LongAdder timeNs  = new LongAdder();
    }
    private final ConcurrentHashMap<String, FileStats> files = new ConcurrentHashMap<>();

    // -------- Optional thread-local "current file" for worker-style usage
    private final ThreadLocal<String> currentFile = new ThreadLocal<>();

    // =========================================================
    // Usage: choose either explicit file names or the thread-local API
    // =========================================================

    // -- Explicit file-name API (safe for any concurrency pattern)

    public void recordTriple(String fileName) {
        Objects.requireNonNull(fileName, "fileName");
        files.computeIfAbsent(fileName, f -> new FileStats()).triples.increment();
        totalTriples.increment();
        totalComponents.add(3);
    }

    public void recordUniqueEntity() {
        uniqueEntities.increment();
        uniqueComponents.increment();
    }

    public void recordUniquePredicate() {
        uniquePredicates.increment();
        uniqueComponents.increment();
    }

    public void recordBatch() {
        batchCount.increment();
    }

    /**
     * Call once per completed file to close it out with its processing time in nanoseconds.
     * Safe to call multiple times; time accumulates.
     */
    public void endFile(String fileName, long fileProcessingTimeNs) {
        Objects.requireNonNull(fileName, "fileName");
        FileStats fs = files.computeIfAbsent(fileName, f -> new FileStats());
        if (fileProcessingTimeNs > 0) {
            fs.timeNs.add(fileProcessingTimeNs);
            totalTimeNs.add(fileProcessingTimeNs);
            minTimeNs.accumulate(fileProcessingTimeNs);
            maxTimeNs.accumulate(fileProcessingTimeNs);
        }
        fileCount.increment();
    }

    // -- Thread-local API (one file per worker thread)

    /**
     * Associates the current thread with a file; subsequent recordTriple() calls
     * without a fileName are attributed to this file.
     */
    public void startFile(String fileName) {
        Objects.requireNonNull(fileName, "fileName");
        currentFile.set(fileName);
        files.computeIfAbsent(fileName, f -> new FileStats()); // ensure present
    }

    /**
     * Clears the current thread’s file association.
     */
    public void clearCurrentFile() {
        currentFile.remove();
    }

    /**
     * Records a triple against the thread's current file.
     * Throws if no current file is set.
     */
    public void recordTriple() {
        String file = currentFile.get();
        if (file == null) {
            throw new IllegalStateException("No current file set. Call startFile(fileName) or use recordTriple(fileName).");
        }
        recordTriple(file);
    }

    /**
     * Ends the current thread’s file with its processing time.
     */
    public void endCurrentFile(long fileProcessingTimeNs) {
        String file = currentFile.get();
        if (file == null) {
            throw new IllegalStateException("No current file set. Call startFile(fileName) first.");
        }
        endFile(file, fileProcessingTimeNs);
        clearCurrentFile();
    }

    // =========================================================
    // Snapshot + presentation
    // =========================================================

    public Snapshot snapshot() {
        // Aggregate min/max triples per file with filenames.
        int minTriples = Integer.MAX_VALUE;
        int maxTriples = 0;
        String fileWithMin = null;
        String fileWithMax = null;

        for (Map.Entry<String, FileStats> e : files.entrySet()) {
            int t = e.getValue().triples.intValue();
            if (t < minTriples) { minTriples = t; fileWithMin = e.getKey(); }
            if (t > maxTriples) { maxTriples = t; fileWithMax = e.getKey(); }
        }

        long filesProcessed = fileCount.longValue();
        long tt = totalTimeNs.longValue();
        long avg = (filesProcessed > 0) ? (tt / filesProcessed) : 0L;

        // Handle case when no times have been recorded
        long minT = (minTimeNs.get() == Long.MAX_VALUE) ? 0L : minTimeNs.get();
        long maxT = (maxTimeNs.get() == Long.MIN_VALUE) ? 0L : maxTimeNs.get();

        return new Snapshot(
            totalTriples.intValue(),
            totalComponents.intValue(),
            uniquePredicates.intValue(),
            uniqueEntities.intValue(),
            uniqueComponents.intValue(),
            batchCount.intValue(),
            (int) filesProcessed,
            maxTriples,
            fileWithMax,
            (minTriples == Integer.MAX_VALUE) ? 0 : minTriples,
            fileWithMin,
            minT,
            maxT,
            tt,
            avg
        );
    }

    @Override
    public String toString() {
        Snapshot s = snapshot();
        StringBuilder b = new StringBuilder();
        b.append("=== Encoding Statistics ===")
         .append("\nTotal Triples: ").append(s.totalTriples)
         .append("\nTotal Components: ").append(s.totalComponents)
         .append("\nUnique Entities (Objects + Subjects): ").append(s.uniqueEntities)
         .append("\nUnique Predicates: ").append(s.uniquePredicates)
         .append("\nUnique Components: ").append(s.uniqueComponents)
         .append("\nFiles Processed: ").append(s.fileCount)
         .append("\nBatches Processed: ").append(s.batchCount)
         .append("\nMax Triples per file: ").append(s.maxTriplesPerFile)
         .append(" in ").append(Optional.ofNullable(s.fileWithMostTriples).orElse("-"))
         .append("\nMin Triples per file: ").append(s.minTriplesPerFile)
         .append(" in ").append(Optional.ofNullable(s.fileWithLeastTriples).orElse("-"))
         .append("\n\n========================================")
         .append("\nAvg Time: ").append(formatMinutes(s.avgTimeNs))
         .append("\nMin Time: ").append(formatMinutes(s.minTimeNs))
         .append("\nMax Time: ").append(formatMinutes(s.maxTimeNs))
         .append("\nTotal Time: ").append(formatMinutes(s.totalTimeNs));
        return b.toString();
    }

    // -------- Snapshot DTO (immutable view)
    public static final class Snapshot {
        public final int totalTriples;
        public final int totalComponents;
        public final int uniquePredicates;
        public final int uniqueEntities;
        public final int uniqueComponents;
        public final int batchCount;
        public final int fileCount;

        public final int maxTriplesPerFile;
        public final String fileWithMostTriples;
        public final int minTriplesPerFile;
        public final String fileWithLeastTriples;

        public final long minTimeNs;
        public final long maxTimeNs;
        public final long totalTimeNs;
        public final long avgTimeNs;

        Snapshot(int totalTriples, int totalComponents, int uniquePredicates, int uniqueEntities,
                 int uniqueComponents, int batchCount, int fileCount,
                 int maxTriplesPerFile, String fileWithMostTriples,
                 int minTriplesPerFile, String fileWithLeastTriples,
                 long minTimeNs, long maxTimeNs, long totalTimeNs, long avgTimeNs) {
            this.totalTriples = totalTriples;
            this.totalComponents = totalComponents;
            this.uniquePredicates = uniquePredicates;
            this.uniqueEntities = uniqueEntities;
            this.uniqueComponents = uniqueComponents;
            this.batchCount = batchCount;
            this.fileCount = fileCount;
            this.maxTriplesPerFile = maxTriplesPerFile;
            this.fileWithMostTriples = fileWithMostTriples;
            this.minTriplesPerFile = minTriplesPerFile;
            this.fileWithLeastTriples = fileWithLeastTriples;
            this.minTimeNs = minTimeNs;
            this.maxTimeNs = maxTimeNs;
            this.totalTimeNs = totalTimeNs;
            this.avgTimeNs = avgTimeNs;
        }
    }

    // -------- Utilities

    private String formatMinutes(long nanoseconds) {
        double minutes = nanoseconds / 1_000_000_000.0 / 60.0;
        return String.format("%.2f minutes", minutes);
    }

    // -------- Backward-compat shims (optional)

    /**
     * Deprecated: ambiguous under concurrency. Prefer recordTriple(fileName) or set a current file.
     */
    @Deprecated
    public void recordTripleLegacy() {
        recordTriple();
    }

    /**
     * Deprecated: use endFile(fileName, ns). Kept for compatibility without time accounting.
     */
    @Deprecated
    public void recordFile(String fileName) {
        endFile(fileName, 0L);
    }

    /**
     * Deprecated: use endFile(fileName, ns). This updates only global time stats.
     */
    @Deprecated
    public void addTime(long ns) {
        if (ns > 0) {
            totalTimeNs.add(ns);
            minTimeNs.accumulate(ns);
            maxTimeNs.accumulate(ns);
        }
    }

    // -------- Direct getters (from snapshot for consistency)

    public int getTotalTriples()         { return snapshot().totalTriples; }
    public int getTotalComponents()      { return snapshot().totalComponents; }
    public int getUniquePredicates()     { return snapshot().uniquePredicates; }
    public int getUniqueEntities()       { return snapshot().uniqueEntities; }
    public int getUniqueComponents()     { return snapshot().uniqueComponents; }
    public int getBatchCount()           { return snapshot().batchCount; }
    public int getFileCount()            { return snapshot().fileCount; }
    public int getMaxTriplesPerFile()    { return snapshot().maxTriplesPerFile; }
    public String getFileWithMostTriples(){ return snapshot().fileWithMostTriples; }
    public int getMinTriplesPerFile()    { return snapshot().minTriplesPerFile; }
    public String getFileWithLeastTriples(){ return snapshot().fileWithLeastTriples; }
    public long getMinTime()             { return snapshot().minTimeNs; }
    public long getMaxTime()             { return snapshot().maxTimeNs; }
    public long getTotalTime()           { return snapshot().totalTimeNs; }
    public long getAvgTime()             { return snapshot().avgTimeNs; }
}
