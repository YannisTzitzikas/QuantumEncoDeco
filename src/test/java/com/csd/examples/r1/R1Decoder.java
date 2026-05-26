package com.csd.examples.r1;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.api.StreamEnvironment;
import com.csd.examples.common.filters.operations.R1RocksDbDecoderMap;
import com.csd.examples.common.filters.sources.EncodedBitstringSourceSupplier;
import com.csd.examples.common.storage.ManagedRocksDb;
import com.csd.examples.common.storage.RocksDbBulkLoader;

public class R1Decoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(R1Decoder.class);

    @Test
    public void testDecodeBitstringsToTriples() throws Exception {
        Path encodedFile = Paths.get("results", "r1", "encoded_triples.bits").toAbsolutePath();
        Path decodedOutputFile = Paths.get("results", "r1", "decoded_triples.nt");
        Path textMappingFile = Paths.get("results", "r1", "global_mappings.dat");
        
        // Note: Reverse DB uses 'true' (ID -> URI)
        String reverseDbDir = "results/r1/rocks_reverse_db";
        RocksDbBulkLoader.populateFromTextFile(textMappingFile, reverseDbDir, true);
        
        // Calculate bit-width layout again to know how to chunk the incoming lines
        int bitWidthN = R1Encoder.calculateBitWidth(textMappingFile);

        // Uses a generic text reader source
        EncodedBitstringSourceSupplier bitSource = new EncodedBitstringSourceSupplier(encodedFile, 25_000);

        try (ManagedRocksDb reverseDb = new ManagedRocksDb(reverseDbDir, false)) {
            StreamEnvironment graph = new StreamEnvironment();

            graph.fromSource(bitSource)
                 .map(new R1RocksDbDecoderMap(reverseDb.get(), bitWidthN))
                 .sink(decodedOutputFile);

            LOGGER.info("Starting Reverse Decoding Phase...");
            long startTime = System.nanoTime();
            
            graph.execute();
            
            long duration = System.nanoTime() - startTime;
            LOGGER.info("Decoding Complete. Output located at {}. Time: {} ms", 
                        decodedOutputFile.toAbsolutePath(), duration / 1_000_000.0);
        }
    }
}