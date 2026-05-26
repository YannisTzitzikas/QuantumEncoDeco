package com.csd.examples.r2;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.api.StreamEnvironment;
import com.csd.examples.common.storage.ManagedRocksDb;
import com.csd.examples.common.storage.RocksDbBulkLoader;
import com.csd.examples.common.filters.sources.EncodedBitstringSourceSupplier;

public class R2Decoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(R2Decoder.class);

    @Test
    public void testR2BitstringDecoding() throws Exception {
        Path encodedFile = Paths.get("results", "r2", "encoded_triples.bits").toAbsolutePath();
        Path decodedOutputFile = Paths.get("results", "r2", "decoded_triples.nt");

        Path finalEntitiesFile = Paths.get("results", "r2", "global_entities_mappings.dat");
        Path finalPredicatesFile = Paths.get("results", "r2", "global_predicates_mappings.dat");
        String reverseDbDir = "results/r2/rocks_reverse_db";

        // 1. Build unified structural storage layer targeting Reverse lookup needs
        RocksDbBulkLoader.populateFromMultipleTextFiles(
            List.of(finalEntitiesFile, finalPredicatesFile),
            List.of("ENTITY", "PREDICATE"),
            reverseDbDir,
            true, // Reverse Mode: Numerical ID -> String URI
            true  // Prepend labels to avoid collisions between identically numbered IDs
        );

        // 2. Map asymmetric dimensional bounds to safely partition incoming lines
        int bitWidthEntity = R2Encoder.calculateBitWidth(finalEntitiesFile);
        int bitWidthPredicate = R2Encoder.calculateBitWidth(finalPredicatesFile);
        int expectedLineLength = (bitWidthEntity * 2) + bitWidthPredicate;

        EncodedBitstringSourceSupplier bitSource = new EncodedBitstringSourceSupplier(encodedFile, 25_000);

        // 3. Open optimized managed environment context
        try (ManagedRocksDb reverseDb = new ManagedRocksDb(reverseDbDir, false)) {
            StreamEnvironment graph = new StreamEnvironment();

            graph.fromSource(bitSource)
                 .map(bitString -> {
                     if (bitString.length() != expectedLineLength) return null;

                     // Partition bits by extracting substring sequences using offset lengths
                     String sBin = bitString.substring(0, bitWidthEntity);
                     String pBin = bitString.substring(bitWidthEntity, bitWidthEntity + bitWidthPredicate);
                     String oBin = bitString.substring(bitWidthEntity + bitWidthPredicate);

                     long sId = Long.parseLong(sBin, 2);
                     long pId = Long.parseLong(pBin, 2);
                     long oId = Long.parseLong(oBin, 2);

                     try {
                         // Perform namespaced lookups against the reverse tracking index
                         byte[] sVal = reverseDb.get().get(("ENTITY:" + sId).getBytes());
                         byte[] pVal = reverseDb.get().get(("PREDICATE:" + pId).getBytes());
                         byte[] oVal = reverseDb.get().get(("ENTITY:" + oId).getBytes());

                         if (sVal == null || pVal == null || oVal == null) return null;

                         // Emit standard valid NTriple output string record line
                         return "<" + new String(sVal) + "> <" + new String(pVal) + "> <" + new String(oVal) + "> .";
                     } catch (Exception e) {
                         return null;
                     }
                 })
                 .filter(decodedTripleLine -> decodedTripleLine != null)
                 .sink(decodedOutputFile);

            LOGGER.info("Starting R2 Reverse Decoding Graph Execution Pass...");
            long startTime = System.nanoTime();
            graph.execute();
            LOGGER.info("R2 Decoder tracking pass terminated in {} ms. Output located at: {}", 
                        (System.nanoTime() - startTime) / 1_000_000.0, decodedOutputFile.toAbsolutePath());
        }
    }
}