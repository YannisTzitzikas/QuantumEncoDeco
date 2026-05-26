package com.csd.examples.r1;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.api.StreamEnvironment;
import com.csd.examples.common.filters.operations.R1RocksDbEncoderMap;
import com.csd.examples.common.filters.sources.UriTripleBatchSourceSupplier;
import com.csd.examples.common.metrics.PipelineContext;
import com.csd.examples.common.storage.ManagedRocksDb;
import com.csd.examples.common.storage.RocksDbBulkLoader;

public class R1Encoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(R1Encoder.class);

    @Test
    public void R1EncodeURIs() throws Exception {
        PipelineContext metricsContext = new PipelineContext();
        Path inputDataFile = Paths.get("src", "test", "resources", "data.xml").toAbsolutePath(); 
        Path outputEncodedFile = Paths.get("results", "r1",  "encoded_triples.bits");
        Path textMappingFile = Paths.get("results", "r1", "global_mappings.dat");
        
        // Note: Forward DB uses 'false' (URI -> ID)
        String forwardDbDir = "results/r1/rocks_forward_db"; 
        RocksDbBulkLoader.populateFromTextFile(textMappingFile, forwardDbDir, false);
        
        int bitWidthN = calculateBitWidth(textMappingFile);

        UriTripleBatchSourceSupplier sourceSupplier = new UriTripleBatchSourceSupplier(
            inputDataFile, "*.ttl", 25_000, false, metricsContext
        );
        
        // try-with-resources guarantees no memory leaks
        try (ManagedRocksDb forwardDb = new ManagedRocksDb(forwardDbDir, false)) {
            StreamEnvironment graph = new StreamEnvironment();

            graph.fromSource(sourceSupplier)
                 .map(new R1RocksDbEncoderMap(forwardDb.get(), bitWidthN))
                 .sink(outputEncodedFile);

            LOGGER.info("Starting Forward Encoding Phase...");
            long startTime = System.nanoTime();
            graph.execute();
            long duration = System.nanoTime() - startTime;
            LOGGER.info("Encoding Complete. Time: {} ms", duration / 1_000_000.0);
        }
    }

    public static int calculateBitWidth(Path textMappingFile) throws IOException {
        long totalUniqueMappings;
        try (BufferedReader headerReader = Files.newBufferedReader(textMappingFile)) {
            String firstLine = headerReader.readLine();
            if (firstLine == null || firstLine.trim().isEmpty()) {
                throw new IllegalStateException("Mapping file is empty.");
            }
            totalUniqueMappings = Long.parseLong(firstLine.trim());
        }
        if (totalUniqueMappings <= 1) return 1;
        return (int) Math.ceil(Math.log(totalUniqueMappings) / Math.log(2));
    }
}