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

import com.csd.metrics.FileMetrics;

public class FileMetricsWriter {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                         .withZone(ZoneId.systemDefault());

    private final Path fileMetricsDir;

    public FileMetricsWriter() throws IOException {
        this(Paths.get("results"));
    }

    public FileMetricsWriter(Path baseDir) throws IOException {
        this.fileMetricsDir = baseDir.resolve("file");
        Files.createDirectories(fileMetricsDir);
    }

    /**
     * Format nanoseconds into a human-readable string with appropriate units.
     */
    private String formatDuration(long nanoseconds) {
        if (nanoseconds < 1_000) {
            return String.format("%,d ns", nanoseconds);
        } else if (nanoseconds < 1_000_000) {
            return String.format("%,.3f µs", nanoseconds / 1_000.0);
        } else if (nanoseconds < 1_000_000_000L) {
            return String.format("%,.3f ms", nanoseconds / 1_000_000.0);
        } else if (nanoseconds < 60_000_000_000L) {
            return String.format("%,.3f s", nanoseconds / 1_000_000_000.0);
        } else if (nanoseconds < 3_600_000_000_000L) {
            return String.format("%,.3f min", nanoseconds / 60_000_000_000.0);
        } else {
            return String.format("%,.3f hours", nanoseconds / 3_600_000_000_000.0);
        }
    }

    public void writeAllMetrics(FileMetrics fileMetrics) throws IOException {
        if (fileMetrics == null) return;

        for (FileMetrics.FileStats stats : fileMetrics.getFileStats().values()) {
            writeFileMetrics(stats);
        }

        writeSummary(fileMetrics);
    }

    public void writeFileMetrics(FileMetrics.FileStats stats) throws IOException {
        if (stats == null) return;

        String safeName = stats.getFilePath()
            .replaceAll("[^a-zA-Z0-9._-]", "_")
            .replaceAll("_+", "_");

        Path metricsFile = fileMetricsDir.resolve(safeName + ".log");

        try (BufferedWriter writer = Files.newBufferedWriter(metricsFile)) {
            writer.write(String.format("Metrics for file: %s%n", stats.getFilePath()));
            writer.write(String.format("Generated at: %s%n", TIMESTAMP_FORMATTER.format(Instant.now())));
            writer.write(String.format("========================================%n"));

            writer.write(String.format("File size: %s%n", stats.getFileSizeBytes(), " MB"));
            writer.write(String.format("Processing time: %s%n", formatDuration(stats.getProcessingTimeNanos())));
            writer.write(String.format("Bytes per second: %,.3f%n", stats.getBytesPerSecond()));

            writer.write(String.format("========================================%n"));
        }
    }

    public void writeSummary(FileMetrics fileMetrics) throws IOException {
        if (fileMetrics == null) return;

        Path summaryFile = fileMetricsDir.resolve("summary.log");

        try (BufferedWriter writer = Files.newBufferedWriter(summaryFile)) {
            writer.write(String.format("File Metrics Summary%n"));
            writer.write(String.format("Generated at: %s%n", TIMESTAMP_FORMATTER.format(Instant.now())));
            writer.write(String.format("========================================%n%n"));

            writer.write(String.format("Total files processed: %,d%n", fileMetrics.getTotalFilesProcessed()));
            writer.write(String.format("Total processing time: %s%n", formatDuration(fileMetrics.getTotalProcessingTimeNanos())));
            writer.write(String.format("%n"));

            for (Map.Entry<String, FileMetrics.FileStats> entry : fileMetrics.getFileStats().entrySet()) {
                FileMetrics.FileStats stats = entry.getValue();

                writer.write(String.format("File: %s%n", stats.getFilePath()));
                writer.write(String.format("  Size: %s%n",stats.getFileSizeBytes(), " MB" ));
                writer.write(String.format("  Processing time: %s%n", formatDuration(stats.getProcessingTimeNanos())));
                writer.write(String.format("  Bytes per second: %,.3f%n", stats.getBytesPerSecond()));
                writer.write(String.format("%n"));
            }

            writer.write(String.format("========================================%n"));
        }
    }
}