package com.csd.metrics.writers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.csd.metrics.UriTripleMetrics;

public class UriTripleMetricsWriter {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                         .withZone(ZoneId.systemDefault());

    private final Path metricsBaseDir;
    private final Path metricsFile;

    public UriTripleMetricsWriter() throws IOException {
        this(Paths.get("results"));
    }

    public UriTripleMetricsWriter(Path baseDir) throws IOException {
        this.metricsBaseDir = baseDir;
        this.metricsFile = baseDir.resolve("uri_triple_metrics.log");
        Files.createDirectories(metricsBaseDir);
    }

    public void writeMetrics(UriTripleMetrics metrics) throws IOException {
        if (metrics == null) return;

        try (BufferedWriter writer = Files.newBufferedWriter(metricsFile)) {
            writer.write(String.format("URI Triple Metrics%n"));
            writer.write(String.format("Generated at: %s%n", TIMESTAMP_FORMATTER.format(Instant.now())));
            writer.write("========================================\n");

            writer.write(String.format("Total triples processed: %,d%n", metrics.getTotalTriplesProcessed()));
            writer.write(String.format("Total entities processed: %,d%n", metrics.getTotalEntitiesProcessed()));
            writer.write(String.format("Total predicates processed: %,d%n", metrics.getTotalPredicatesProcessed()));
            writer.write(String.format("Unique entities count: %,d%n", metrics.getUniqueEntitiesCount()));
            writer.write(String.format("Unique predicates count: %,d%n", metrics.getUniquePredicatesCount()));
            writer.write(String.format("Unique components count: %,d%n", metrics.getUniqueComponentsCount()));
            writer.write(String.format("Total components processed: %,d%n", metrics.getTotalComponentsProcessed()));
            writer.write(String.format("Entity uniqueness ratio: %.2f%%%n", metrics.getEntityUniquenessRatio()));
            writer.write(String.format("Predicate uniqueness ratio: %.2f%%%n", metrics.getPredicateUniquenessRatio()));
            writer.write(String.format("Component uniqueness ratio: %.2f%%%n", metrics.getComponentUniquenessRatio()));

            writer.write("========================================\n");
        }
    }
}
