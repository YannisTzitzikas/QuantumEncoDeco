package com.csd.metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.csd.core.event.EventBus;
import com.csd.events.RecordBatchEvent;

public class UriTripleMetricsWithCSV extends UriTripleMetrics {
    private final BufferedWriter csvWriter;
    private final DateTimeFormatter timestampFormatter;
    private final Path csvFilePath;
    
    public UriTripleMetricsWithCSV(EventBus eventBus, String csvFileName) throws IOException {
        super(eventBus);
        
        // Create metrics directory if it doesn't exist
        Path metricsDir = Paths.get("results");
        Files.createDirectories(metricsDir);
        
        // Set up CSV file
        this.csvFilePath = metricsDir.resolve(csvFileName);
        this.csvWriter = Files.newBufferedWriter(csvFilePath);
        this.timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                                                  .withZone(ZoneId.systemDefault());
        
        // Write CSV header
        csvWriter.write("timestamp,total_triples,total_entities,total_predicates,unique_entities,unique_predicates,unique_components,total_components,entity_uniqueness_ratio,predicate_uniqueness_ratio,component_uniqueness_ratio\n");
        csvWriter.flush();
        
        // Subscribe to BatchProcessedEvent
        eventBus.subscribe(RecordBatchEvent.class, this::handleBatchProcessed);
    }
    
    private void handleBatchProcessed(RecordBatchEvent event) {
        try {
            String timestamp = timestampFormatter.format(Instant.now());
            String line = String.format(Locale.US ,"%s,%d,%d,%d,%d,%d,%d,%d,%.2f,%.2f,%.2f\n",
                    timestamp,
                    getTotalTriplesProcessed(),
                    getTotalEntitiesProcessed(),
                    getTotalPredicatesProcessed(),
                    getUniqueEntitiesCount(),
                    getUniquePredicatesCount(),
                    getUniqueComponentsCount(),
                    getTotalComponentsProcessed(),
                    getEntityUniquenessRatio(),
                    getPredicateUniquenessRatio(),
                    getComponentUniquenessRatio());
            
            csvWriter.write(line);
            csvWriter.flush();
        } catch (IOException e) {
            System.err.println("Failed to write CSV metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void close() throws IOException {
        if (csvWriter != null) {
            csvWriter.close();
        }
    }
    
    public Path getCsvFilePath() {
        return csvFilePath;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}