package com.csd.common.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class StatisticsCollector {
    // Singleton instance
    private static volatile StatisticsCollector instance;
    private static final ReentrantLock lock = new ReentrantLock();
    
    // Overall statistics (atomic)
    private final AtomicInteger totalTriples = new AtomicInteger(0);
    private final AtomicInteger totalComponents = new AtomicInteger(0);
    private final AtomicInteger uniquePredicates = new AtomicInteger(0);
    private final AtomicInteger uniqueEntities = new AtomicInteger(0);
    private final AtomicInteger uniqueComponents = new AtomicInteger(0);
    private final AtomicInteger batchCount = new AtomicInteger(0);
    private final AtomicInteger fileCount = new AtomicInteger(0);

    // Per file statistics (need synchronization)
    private final ReentrantFileStats fileStats = new ReentrantFileStats();

    // Timing statistics (atomic)
    private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxTime = new AtomicLong(0);
    private final AtomicLong totalTime = new AtomicLong(0);

    // Private constructor for singleton
    private StatisticsCollector() {}

    public static StatisticsCollector getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new StatisticsCollector();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public void recordTriple() {
        fileStats.recordTriple();
        totalTriples.incrementAndGet();
        totalComponents.addAndGet(3);
    }

    public void recordUniqueEntity() {
        uniqueEntities.incrementAndGet();
        uniqueComponents.incrementAndGet();
    }

    public void recordUniquePredicate() {
        uniquePredicates.incrementAndGet();
        uniqueComponents.incrementAndGet();
    }

    public void recordBatch() {
        batchCount.incrementAndGet();
    }

    public void recordFile(String fileName) {
        fileStats.recordFile(fileName);
        fileCount.incrementAndGet();
    }

    public void addTime(long ns) {
        // Update min time
        long currentMin;
        do {
            currentMin = minTime.get();
        } while (ns < currentMin && !minTime.compareAndSet(currentMin, ns));
        
        // Update max time
        long currentMax;
        do {
            currentMax = maxTime.get();
        } while (ns > currentMax && !maxTime.compareAndSet(currentMax, ns));
        
        totalTime.addAndGet(ns);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("=== Encoding Statistics ===")
                .append("\nTotal Triples: ").append(totalTriples.get())
                .append("\nTotal Components: ").append(totalComponents.get())
                .append("\nUnique Entities (Objects + Subjects): ").append(uniqueEntities.get())
                .append("\nUnique Predicates: ").append(uniquePredicates.get())
                .append("\nUnique Components: ").append(uniqueComponents.get())
                .append("\nFiles Processed: ").append(fileCount.get())
                .append("\nBatches Processed: ").append(batchCount.get())
                .append("\nMax Triples per file: ").append(fileStats.getMaxTriplesPerFile())
                .append(" in ").append(fileStats.getFileWithMostTriples())
                .append("\nMin Triples per file: ").append(fileStats.getMinTriplesPerFile())
                .append(" in ").append(fileStats.getFileWithLeastTriples())
                .append("\n\n========================================")
                .append("\nAvg Time: ").append(formatMinutes(getAvgTime()))
                .append("\nMin Time: ").append(formatMinutes(minTime.get()))
                .append("\nMax Time: ").append(formatMinutes(maxTime.get()))
                .append("\nTotal Time: ").append(formatMinutes(totalTime.get()));
        
        return builder.toString();
    }

    // Getters
    public int getTotalTriples() {
        return totalTriples.get();
    }

    public int getTotalComponents() {
        return totalComponents.get();
    }

    public int getUniquePredicates() {
        return uniquePredicates.get();
    }

    public int getUniqueEntities() {
        return uniqueEntities.get();
    }

    public int getUniqueComponents() {
        return uniqueComponents.get();
    }

    public int getBatchCount() {
        return batchCount.get();
    }

    public int getFileCount() {
        return fileCount.get();
    }

    public int getMaxTriplesPerFile() {
        return fileStats.getMaxTriplesPerFile();
    }
    
    public String getFileWithMostTriples() {
        return fileStats.getFileWithMostTriples();
    }
    
    public int getMinTriplesPerFile() {
        return fileStats.getMinTriplesPerFile();
    }
    
    public String getFileWithLeastTriples() {
        return fileStats.getFileWithLeastTriples();
    }

    public long getMinTime() {
        return minTime.get();
    }

    public long getMaxTime() {
        return maxTime.get();
    }

    public long getTotalTime() {
        return totalTime.get();
    } 

    public long getAvgTime() {
        int count = fileCount.get();
        return count > 0 ? totalTime.get() / count : 0;
    }

    // Utility method
    private String formatMinutes(long nanoseconds) {
        double minutes = nanoseconds / 1_000_000_000.0 / 60.0;
        return String.format("%.2f minutes", minutes);
    }

    // Thread-safe file statistics container
    private static class ReentrantFileStats {
        private int triplesInCurrFile = 0;
        private int maxTriplesPerFile = 0;
        private String fileWithMostTriples = null;
        private int minTriplesPerFile = Integer.MAX_VALUE;
        private String fileWithLeastTriples = null;
        private final ReentrantLock lock = new ReentrantLock();

        public void recordTriple() {
            lock.lock();
            try {
                triplesInCurrFile++;
            } finally {
                lock.unlock();
            }
        }

        public void recordFile(String fileName) {
            lock.lock();
            try {
                // Check for new minimum
                if (triplesInCurrFile < minTriplesPerFile) {
                    minTriplesPerFile = triplesInCurrFile;
                    fileWithLeastTriples = fileName;
                }

                // Check for new maximum
                if (triplesInCurrFile > maxTriplesPerFile) {
                    maxTriplesPerFile = triplesInCurrFile;
                    fileWithMostTriples = fileName;
                }

                triplesInCurrFile = 0;
            } finally {
                lock.unlock();
            }
        }

        public int getMaxTriplesPerFile() {
            lock.lock();
            try {
                return maxTriplesPerFile;
            } finally {
                lock.unlock();
            }
        }
        
        public String getFileWithMostTriples() {
            lock.lock();
            try {
                return fileWithMostTriples;
            } finally {
                lock.unlock();
            }
        }
        
        public int getMinTriplesPerFile() {
            lock.lock();
            try {
                return minTriplesPerFile;
            } finally {
                lock.unlock();
            }
        }
        
        public String getFileWithLeastTriples() {
            lock.lock();
            try {
                return fileWithLeastTriples;
            } finally {
                lock.unlock();
            }
        }
    }
}