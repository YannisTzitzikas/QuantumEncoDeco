package com.csd.examples.common.storage;

import com.csd.examples.common.storage.core.StorageEngine;
import com.csd.examples.common.storage.core.StorageException;
import com.csd.examples.common.storage.core.StorageOptions;
import com.csd.examples.common.storage.mapper.RocksRuntimeMapper;
import com.csd.examples.common.storage.options.InMemoryOptions;
import com.csd.examples.common.storage.options.RocksOptions;
import com.csd.examples.common.storage.options.RocksRuntime;

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
