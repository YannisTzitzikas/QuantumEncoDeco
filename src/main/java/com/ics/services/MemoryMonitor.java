package com.ics.services;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class MemoryMonitor {
    private final double threshold;
    private final MemoryMXBean memoryBean;
    private static final double FALLBACK_THRESHOLD = 0.95; // If max heap undefined

    public MemoryMonitor(double threshold) {
        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException("Threshold must be between 0-100");
        }
        this.threshold = threshold;
        this.memoryBean = ManagementFactory.getMemoryMXBean();
    }

    public boolean isMemoryExceeded() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();
        
        if (max == -1) { 
            long committed = heapUsage.getCommitted();
            return (double) used / committed >= FALLBACK_THRESHOLD;
        }
        
        return ((double) used / max * 100) >= threshold;
    }

    // For immediate cleanup
    public void triggerGarbageCollection() {
        System.gc();
    }
}