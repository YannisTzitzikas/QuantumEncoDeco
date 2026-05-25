package com.csd.examples.common.metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Consolidated IO Reporter.
 * Replaces the old fragmented writer interfaces with an integrated reporting API.
 */
public class PipelineMetricsReportWriter {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private final Path outputDir;

    public PipelineMetricsReportWriter() throws IOException {
        this.outputDir = Paths.get("results");
        Files.createDirectories(outputDir);
    }

    /**
     * Master logging print routine replacing individual summary logs.
     */
    public void generateExecutionReports(PipelineContext context) throws IOException {
        if (context == null) return;

        long totalTriples = context.getTotalTriples();
        long totalEntities = totalTriples * 2;
        long totalPredicates = totalTriples;
        
        long uniqueEntities = context.getUniqueEntities();
        long uniquePredicates = context.getUniquePredicates();

        // Calculate ratios dynamically during generation
        double entityRatio = totalEntities > 0 ? (uniqueEntities * 100.0) / totalEntities : 0.0;
        double predicateRatio = totalPredicates > 0 ? (uniquePredicates * 100.0) / totalPredicates : 0.0;

        // 1. Write the Unified Summary Log File
        Path summaryFile = outputDir.resolve("execution_summary.log");
        try (BufferedWriter writer = Files.newBufferedWriter(summaryFile)) {
            writer.write(String.format("========================================%n"));
            writer.write(String.format("       PIPELINE EXECUTION METRICS       %n"));
            writer.write(String.format("Generated at: %s%n", TIMESTAMP_FORMATTER.format(Instant.now())));
            writer.write(String.format("========================================%n%n"));

            writer.write(String.format("--- Processing Throughput ---%n"));
            writer.write(String.format("Total Batches Processed: %,d%n", context.getTotalBatches()));
            writer.write(String.format("Total Triples Processed: %,d%n%n", totalTriples));

            writer.write(String.format("--- Component Uniqueness Analysis ---%n"));
            writer.write(String.format("Unique Entities: %,d / %,d (%.2f%% uniqueness)%n", uniqueEntities, totalEntities, entityRatio));
            writer.write(String.format("Unique Predicates: %,d / %,d (%.2f%% uniqueness)%n%n", uniquePredicates, totalPredicates, predicateRatio));

            writer.write(String.format("--- File System Analytics ---%n"));
            long totalFileTimeNanos = 0;
            for (PipelineContext.FileStats fStats : context.getFileStatsMap().values()) {
                totalFileTimeNanos += fStats.getProcessingTimeNanos();
                writer.write(String.format("File: %s%n", fStats.getFilePath()));
                writer.write(String.format("  Size: %,.2f MB | Runtime: %,.3f s%n", fStats.getFileSizeMB(), fStats.getProcessingTimeSeconds()));
                writer.write(String.format("  Throughput: %,.2f MB/sec%n", fStats.getMegabytesPerSecond()));
            }
            writer.write(String.format("%nTotal Combined Processing Time: %,.3f s%n", totalFileTimeNanos / 1_000_000_000.0));
            writer.write(String.format("========================================%n"));
        }

        // 2. Append the tracking data entry into the persistent tracking CSV
        Path csvFile = outputDir.resolve("pipeline_historical_metrics.csv");
        boolean exists = Files.exists(csvFile);
        
        try (BufferedWriter csvWriter = Files.newBufferedWriter(csvFile, 
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
            
            if (!exists) {
                csvWriter.write("timestamp,total_triples,total_batches,unique_entities,unique_predicates,entity_ratio,predicate_ratio\n");
            }
            
            String csvLine = String.format(Locale.US, "%s,%d,%d,%d,%d,%.2f,%.2f\n",
                TIMESTAMP_FORMATTER.format(Instant.now()),
                totalTriples,
                context.getTotalBatches(),
                uniqueEntities,
                uniquePredicates,
                entityRatio,
                predicateRatio
            );
            csvWriter.write(csvLine);
            csvWriter.flush();
        }
    }
}