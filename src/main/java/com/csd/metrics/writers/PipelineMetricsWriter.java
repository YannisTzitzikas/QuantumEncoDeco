package com.csd.metrics.writers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.csd.metrics.PipelineMetrics;

public class PipelineMetricsWriter {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                         .withZone(ZoneId.systemDefault());
    
    private final Path metricsBaseDir;
    private final Path filterMetricsDir;
    
    public PipelineMetricsWriter() throws IOException {
        this(Paths.get("results"));
    }
    
    public PipelineMetricsWriter(Path baseDir) throws IOException {
        this.metricsBaseDir = baseDir;
        this.filterMetricsDir = baseDir.resolve("filter");
        
        // Create directories if they don't exist
        Files.createDirectories(filterMetricsDir);
    }
    
    /**
     * Format nanoseconds into a human-readable string with appropriate units
     */
    private String formatDuration(long nanoseconds) {
        if (nanoseconds < 1000) {
            return String.format("%,d ns", nanoseconds);
        } else if (nanoseconds < 1_000_000) {
            return String.format("%,.3f ms", nanoseconds / 1_000_000.0);
        } else if (nanoseconds < 1_000_000_000L) {
            return String.format("%,.3f s", nanoseconds / 1_000_000_000.0);
        } else if (nanoseconds < 60_000_000_000L) {
            return String.format("%,.3f min", nanoseconds / 60_000_000_000.0);
        } else {
            return String.format("%,.3f hours", nanoseconds / 3_600_000_000_000.0);
        }
    }
    
    /**
     * Format nanoseconds into a human-readable string with multiple units
     */
    private String formatDurationDetailed(long nanoseconds) {
        if (nanoseconds < 1000) {
            return String.format("%,d nanoseconds", nanoseconds);
        }
        
        StringBuilder result = new StringBuilder();
        long remaining = nanoseconds;
        
        // Extract hours
        if (remaining >= 3_600_000_000_000L) {
            long hours = remaining / 3_600_000_000_000L;
            result.append(String.format("%,d hours ", hours));
            remaining %= 3_600_000_000_000L;
        }
        
        // Extract minutes
        if (remaining >= 60_000_000_000L) {
            long minutes = remaining / 60_000_000_000L;
            result.append(String.format("%,d minutes ", minutes));
            remaining %= 60_000_000_000L;
        }
        
        // Extract seconds
        if (remaining >= 1_000_000_000L) {
            long seconds = remaining / 1_000_000_000L;
            result.append(String.format("%,d seconds ", seconds));
            remaining %= 1_000_000_000L;
        }
        
        // Extract milliseconds
        if (remaining >= 1_000_000L) {
            long milliseconds = remaining / 1_000_000L;
            result.append(String.format("%,d ms ", milliseconds));
            remaining %= 1_000_000L;
        }
        
        // Extract microseconds
        if (remaining >= 1_000L) {
            long microseconds = remaining / 1_000L;
            result.append(String.format("%,d μs ", microseconds));
            remaining %= 1_000L;
        }
        
        // Add remaining nanoseconds
        if (remaining > 0) {
            result.append(String.format("%,d ns", remaining));
        }
        
        return result.toString().trim();
    }
    
    /**
     * Write all metrics from a PipelineMetrics instance
     */
    public void writeAllMetrics(PipelineMetrics pipelineMetrics) throws IOException {
        if (pipelineMetrics == null) return;
        
        Map<String, PipelineMetrics.FilterMetrics> allMetrics = pipelineMetrics.getAllMetrics();
        for (PipelineMetrics.FilterMetrics metrics : allMetrics.values()) {
            writeFilterMetrics(metrics);
        }
        
        // Write summary after individual metrics
        writeSummary(pipelineMetrics);
    }
    
    /**
     * Write a single FilterMetrics instance
     */
    public void writeFilterMetrics(PipelineMetrics.FilterMetrics metrics) throws IOException {
        if (metrics == null) return;
        
        // Create safe filename from filter name
        String safeName = metrics.getFilterName()
                .replaceAll("[^a-zA-Z0-9_-]", "_")
                .replaceAll("_+", "_");
        
        Path metricsFile = filterMetricsDir.resolve(safeName + ".log");
        
        try (BufferedWriter writer = Files.newBufferedWriter(metricsFile)) {
            writer.write(String.format("Metrics for filter: %s%n", metrics.getFilterName()));
            writer.write(String.format("Generated at: %s%n", 
                    TIMESTAMP_FORMATTER.format(Instant.now())));
            writer.write("========================================%n");
            
            // Format total runtime
            String totalRuntime = formatDuration(metrics.getTotalRuntimeNanos());
            writer.write(String.format("Total runtime: %s%n", totalRuntime));
            
            writer.write(String.format("Loop count: %,d%n", metrics.getLoopCount()));
            
            // Format loop duration
            String totalLoopDuration = formatDuration(metrics.getTotalLoopDuration());
            writer.write(String.format("Total loop duration: %s%n", totalLoopDuration));
            
            if (metrics.getLoopCount() > 0) {
                long avgLoopTimeNs = metrics.getTotalLoopDuration() / metrics.getLoopCount();
                String avgLoopTime = formatDuration(avgLoopTimeNs);
                writer.write(String.format("Average loop time: %s%n", avgLoopTime));
            }
            
            writer.write("========================================%n");
        }
    }
    
    /**
     * Write a summary file with all filter metrics
     */
    public void writeSummary(PipelineMetrics pipelineMetrics) throws IOException {
        if (pipelineMetrics == null) return;
        
        Path summaryFile = metricsBaseDir.resolve("summary.log");
        
        try (BufferedWriter writer = Files.newBufferedWriter(summaryFile)) {
            writer.write("Pipeline Metrics Summary%n");
            writer.write(String.format("Generated at: %s%n", 
                    TIMESTAMP_FORMATTER.format(Instant.now())));
            writer.write("========================================%n%n");
            
            Map<String, PipelineMetrics.FilterMetrics> allMetrics = pipelineMetrics.getAllMetrics();
            for (PipelineMetrics.FilterMetrics metrics : allMetrics.values()) {
                writer.write(String.format("Filter: %s%n", metrics.getFilterName()));
                
                // Format total runtime with detailed breakdown
                String totalRuntime = formatDurationDetailed(metrics.getTotalRuntimeNanos());
                writer.write(String.format("  Total runtime: %s%n", totalRuntime));
                
                writer.write(String.format("  Loop count: %,d%n", metrics.getLoopCount()));
                
                if (metrics.getLoopCount() > 0) {
                    long avgLoopTimeNs = metrics.getTotalLoopDuration() / metrics.getLoopCount();
                    String avgLoopTime = formatDuration(avgLoopTimeNs);
                    writer.write(String.format("  Average loop time: %s%n", avgLoopTime));
                }
                writer.write("%n");
            }
            
            writer.write("========================================%n");
        }
    }
    
    /**
     * Write metrics in JSON format for machine processing
     */
    public void writeJsonMetrics(PipelineMetrics pipelineMetrics) throws IOException {
        if (pipelineMetrics == null) return;
        
        Path jsonFile = metricsBaseDir.resolve("metrics.json");
        
        try (BufferedWriter writer = Files.newBufferedWriter(jsonFile)) {
            writer.write("{%n");
            writer.write("  \"timestamp\": \"" + TIMESTAMP_FORMATTER.format(Instant.now()) + "\",%n");
            writer.write("  \"filters\": {%n");
            
            Map<String, PipelineMetrics.FilterMetrics> allMetrics = pipelineMetrics.getAllMetrics();
            boolean first = true;
            
            for (PipelineMetrics.FilterMetrics metrics : allMetrics.values()) {
                if (!first) {
                    writer.write(",%n");
                }
                first = false;
                
                writer.write(String.format("    \"%s\": {%n", metrics.getFilterName()));
                writer.write(String.format("      \"totalRuntimeNs\": %d,%n", metrics.getTotalRuntimeNanos()));
                writer.write(String.format("      \"loopCount\": %d,%n", metrics.getLoopCount()));
                writer.write(String.format("      \"totalLoopDurationNs\": %d,%n", metrics.getTotalLoopDuration()));
                
                if (metrics.getLoopCount() > 0) {
                    long avgLoopTimeNs = metrics.getTotalLoopDuration() / metrics.getLoopCount();
                    writer.write(String.format("      \"avgLoopTimeNs\": %d%n", avgLoopTimeNs));
                } else {
                    writer.write(String.format("      \"avgLoopTimeNs\": 0%n"));
                }
                
                writer.write("    }");
            }
            
            writer.write("%n  }%n");
            writer.write("}%n");
        }
    }
}