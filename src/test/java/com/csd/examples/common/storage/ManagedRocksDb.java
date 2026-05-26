package com.csd.examples.common.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.rocksdb.*;

/**
 * A safe wrapper around RocksDB that manages native C++ JNI memory allocations
 * and ensures parent directories exist before opening the database.
 */
public class ManagedRocksDb implements AutoCloseable {
    private final RocksDB db;
    private final Options options;
    private final LRUCache cache;
    private final BloomFilter filter;

    @SuppressWarnings("resource")
    public ManagedRocksDb(String path, boolean forBulkLoad) throws RocksDBException, IOException {
        RocksDB.loadLibrary();
        
        // Fix for: "Failed to create a directory"
        Path dbPath = Paths.get(path);
        if (!Files.exists(dbPath)) {
            Files.createDirectories(dbPath);
        }

        // Fix for: Resource leaks (assigning to fields to close later)
        this.options = new Options().setCreateIfMissing(true);

        if (forBulkLoad) {
            this.options.prepareForBulkLoad();
            this.cache = null;
            this.filter = null;
        } else {
            // Production Read/Write Mode
            this.cache = new LRUCache(2L * 1024 * 1024 * 1024);
            this.filter = new BloomFilter(10, false);
            
            BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
                tableConfig.setBlockCache(this.cache);
                tableConfig.setFilterPolicy(this.filter);
                this.options.setTableFormatConfig(tableConfig);
            
            this.options.setIncreaseParallelism(Runtime.getRuntime().availableProcessors());
        }

        this.db = RocksDB.open(options, path);
    }

    public RocksDB get() {
        return db;
    }

    @Override
    public void close() {
        // Close in reverse order of creation to prevent native segmentation faults
        if (db != null) db.close();
        if (filter != null) filter.close();
        if (cache != null) cache.close();
        if (options != null) options.close();
    }
}