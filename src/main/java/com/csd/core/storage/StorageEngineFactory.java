package com.csd.core.storage;

import org.rocksdb.RocksDBException;

// TODO(gtheo): Add some better abstractions for the Storage Configuration
public class StorageEngineFactory {

    private StorageEngineFactory() {
        // Prevent instantiation
    }

    /**
     * Returns a new StorageEngine instance based on the backend name.
     * Defaults to HashMapStorageEngine if name is unknown.
     */
    public static StorageEngine getStorageEngine(String backend, String storagePath) {
        if (backend == null) return new HashMapStorageEngine();

        switch (backend.trim().toLowerCase()) {
            case "hashmap": return new HashMapStorageEngine();
            case "rocksdb": try {
                    return new RocksDBStorageEngine(storagePath);
                } catch (RocksDBException e) {
                    e.printStackTrace();
                }
            default:
                return new HashMapStorageEngine(); // Default fallback
        }
    }
}
