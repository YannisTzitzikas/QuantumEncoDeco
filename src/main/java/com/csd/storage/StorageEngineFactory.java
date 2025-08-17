package com.csd.storage;

import com.csd.core.storage.StorageEngine;
import com.csd.core.storage.StorageException;
import com.csd.core.storage.StorageOptions;
import com.csd.storage.mapper.RocksRuntimeMapper;
import com.csd.storage.options.InMemoryOptions;
import com.csd.storage.options.RocksOptions;
import com.csd.storage.options.RocksRuntime;

public final class StorageEngineFactory {

    private StorageEngineFactory() {/* Prevent instantiation */ }

    public static StorageEngine open(StorageOptions options) throws StorageException {
        if (options instanceof InMemoryOptions) {
            InMemoryOptions mem = (InMemoryOptions) options;
            return new InMemoryStorageEngine(mem);
        } else if (options instanceof RocksOptions ) {
            RocksOptions ro = (RocksOptions) options;
            RocksRuntime rt = RocksRuntimeMapper.toRuntime(ro);
            return new RocksDBStorageEngine(rt);
        } else {
            throw new StorageException("Unsupported StorageOptions: " + options.getClass().getName(), new IllegalArgumentException());
        }
    }

    // Optional convenience
    public static StorageEngine inMemory() throws StorageException {
        return open(InMemoryOptions.builder().build());
    }

    public static StorageEngine rocks(String path) throws StorageException {
        return open(RocksOptions.builder(path).build());
    }
}
