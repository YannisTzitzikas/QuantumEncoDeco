package com.csd.examples.common.storage;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDbBulkLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDbBulkLoader.class);

    /**
     * Ingests multiple distinct mapping files into a single consolidated RocksDB instance using labels.
     * * @param textMappingFiles List of paths pointing to your generated metadata mapping files (.dat)
     * @param labels Corresponding namespace prefixes for each file (e.g., "ENTITY", "PREDICATE")
     * @param rocksDbTargetDir Target folder where the binary structures will be saved
     * @param isReverseMapping True for Decoding (ID -> URI), False for Encoding (URI -> ID)
     * @param prependLabel If true, scopes keys using "LABEL:" to avoid cross-domain key collisions
     */
    public static void populateFromMultipleTextFiles(List<Path> textMappingFiles,
                                                     List<String> labels,
                                                     String rocksDbTargetDir,
                                                     boolean isReverseMapping,
                                                     boolean prependLabel) throws Exception {
        if (textMappingFiles.size() != labels.size()) {
            throw new IllegalArgumentException("The number of text mapping files must strictly match the number of labels.");
        }

        // Skip rebuilding if the target database already exists and has a valid structure
        if (Files.exists(Paths.get(rocksDbTargetDir, "CURRENT"))) {
            LOGGER.info("RocksDB instance already populated at [{}]. Skipping multi-file bulk load.", rocksDbTargetDir);
            return;
        }

        LOGGER.info("Building consolidated multi-label RocksDB index at [{}]...", rocksDbTargetDir);

        // Uses ManagedRocksDb with forBulkLoad = true for high performance batch writes
        try (ManagedRocksDb managedDb = new ManagedRocksDb(rocksDbTargetDir, true);
             WriteOptions writeOptions = new WriteOptions().setDisableWAL(true);
             WriteBatch batch = new WriteBatch()) {

            long totalCount = 0;

            for (int i = 0; i < textMappingFiles.size(); i++) {
                Path file = textMappingFiles.get(i);
                String label = labels.get(i);
                String prefix = prependLabel ? label + ":" : "";

                LOGGER.info("Ingesting mapping data from [{}] into namespace prefix: '{}'", file.getFileName(), prefix);

                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    String line = reader.readLine(); // Skip total element count row header
                    if (line == null) continue;

                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;

                        String[] parts = line.split(" ");
                        if (parts.length < 2) continue;

                        String uri = parts[0];
                        String id = parts[1];

                        // Build scoped keys depending on mapping target needs
                        byte[] key = isReverseMapping ? (prefix + id).getBytes() : (prefix + uri).getBytes();
                        byte[] value = isReverseMapping ? uri.getBytes() : id.getBytes();

                        batch.put(key, value);
                        totalCount++;

                        // Periodic flush to stay balanced on heap memory overheads
                        if (totalCount % 50_000 == 0) {
                            managedDb.get().write(writeOptions, batch);
                            batch.clear();
                        }
                    }
                }
            }

            // Flush trailing remainder elements
            if (batch.count() > 0) {
                managedDb.get().write(writeOptions, batch);
            }

            LOGGER.info("Compacting multi-label index to achieve peak storage density...");
            managedDb.get().compactRange();
            LOGGER.info("Multi-file bulk load complete. Consolidated {} total mapping values.", totalCount);
        }
    }

    // Deprecated single file operation left as a fallback layer matching original signature
    public static void populateFromTextFile(Path textMappingFile, String rocksDbTargetDir, boolean isReverseMapping) throws Exception {
        populateFromMultipleTextFiles(List.of(textMappingFile), List.of(""), rocksDbTargetDir, isReverseMapping, false);
    }
}