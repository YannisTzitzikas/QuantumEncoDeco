package com.csd.examples.r2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.api.StreamEnvironment;
import com.csd.examples.common.storage.ManagedRocksDb;
import com.csd.examples.common.storage.RocksDbBulkLoader;
import com.csd.examples.common.filters.sources.UriTripleBatchSourceSupplier;
import com.csd.examples.common.metrics.PipelineContext;

public class R2Encoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(R2Encoder.class);

    @Test
    public void testR2BitstringEncoding() throws Exception {
        PipelineContext metricsContext = new PipelineContext();
        Path inputDataFile = Paths.get("src", "test", "resources", "data.xml").toAbsolutePath();
        Path outputEncodedFile = Paths.get("results", "r2", "encoded_triples.bits");

        Path finalEntitiesFile = Paths.get("results", "r2", "global_entities_mappings.dat");
        Path finalPredicatesFile = Paths.get("results", "r2", "global_predicates_mappings.dat");
        String forwardDbDir = "results/r2/rocks_forward_db";

        // 1. Populate both files safely into a unified forward DB mapping layer
        RocksDbBulkLoader.populateFromMultipleTextFiles(
            List.of(finalEntitiesFile, finalPredicatesFile),
            List.of("ENTITY", "PREDICATE"),
            forwardDbDir,
            false, // Forward Mode: String URI -> Numerical ID
            true   // Prepend labels to isolate keyspace scopes
        );

        // 2. Fetch independent optimal layout bit-widths
        int bitWidthEntity = calculateBitWidth(finalEntitiesFile);
        int bitWidthPredicate = calculateBitWidth(finalPredicatesFile);

        LOGGER.info("Configuring R2 Encoding Matrix. Entities: {} bits, Predicates: {} bits.", bitWidthEntity, bitWidthPredicate);

        // 3. Setup Stream topology using ManagedRocksDb wrapper to completely rule out leaks
        try (ManagedRocksDb forwardDb = new ManagedRocksDb(forwardDbDir, false)) {
            UriTripleBatchSourceSupplier sourceSupplier = new UriTripleBatchSourceSupplier(
                inputDataFile, "*.ttl",  false, metricsContext
            );

            StreamEnvironment graph = new StreamEnvironment();

            graph.fromSource(sourceSupplier)
                 .map(triple -> {
                     try {
                         // Namespaced subjective query lookups
                         byte[] sBytes = forwardDb.get().get(("ENTITY:" + triple.getSubject().getValue()).getBytes());
                         if (sBytes == null) return null;
                         long sId = Long.parseLong(new String(sBytes));

                         byte[] pBytes = forwardDb.get().get(("PREDICATE:" + triple.getPredicate().getValue()).getBytes());
                         if (pBytes == null) return null;
                         long pId = Long.parseLong(new String(pBytes));

                         byte[] oBytes = forwardDb.get().get(("ENTITY:" + triple.getObject().getValue()).getBytes());
                         if (oBytes == null) return null;
                         long oId = Long.parseLong(new String(oBytes));

                         // Format into distinct asymmetric zero-padded fields
                         String sBits = String.format("%" + bitWidthEntity + "s", Long.toBinaryString(sId)).replace(' ', '0');
                         String pBits = String.format("%" + bitWidthPredicate + "s", Long.toBinaryString(pId)).replace(' ', '0');
                         String oBits = String.format("%" + bitWidthEntity + "s", Long.toBinaryString(oId)).replace(' ', '0');

                         return sBits + pBits + oBits;
                     } catch (Exception e) {
                         return null; // Gracefully drop anomalies
                     }
                 })
                 .filter(encodedRow -> encodedRow != null)
                 .sink(outputEncodedFile);

            LOGGER.info("Starting R2 Variable-Bit Graph Streaming Execution Pass...");
            long startTime = System.nanoTime();
            graph.execute();
            LOGGER.info("R2 Forward Graph Encoding completed in {} ms.", (System.nanoTime() - startTime) / 1_000_000.0);
        }
    }

    public static int calculateBitWidth(Path textMappingFile) throws IOException {
        try (BufferedReader headerReader = Files.newBufferedReader(textMappingFile)) {
            String firstLine = headerReader.readLine();
            if (firstLine == null || firstLine.trim().isEmpty()) {
                throw new IllegalStateException("Mapping dictionary at [" + textMappingFile + "] has no count header.");
            }
            long totalUniqueMappings = Long.parseLong(firstLine.trim());
            return totalUniqueMappings <= 1 ? 1 : (int) Math.ceil(Math.log(totalUniqueMappings) / Math.log(2));
        }
    }
}