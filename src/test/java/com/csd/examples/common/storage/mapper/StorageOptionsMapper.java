package com.csd.examples.common.storage.mapper;

import static com.csd.examples.common.storage.mapper.MappingUtils.getString;

import java.util.HashMap;
import java.util.Map;

import com.csd.examples.common.storage.core.StorageOptions;

public final class StorageOptionsMapper {

    private static final Map<String, StorageOptionsMappingStrategy> strategies = new HashMap<>();
    static {
        strategies.put("inmemory", new InMemoryMapperStrategy());
        strategies.put("in_memory", new InMemoryMapperStrategy());
        strategies.put("rocksdb", new RocksMapperStrategy());
        strategies.put("rocks", new RocksMapperStrategy());
    }

    private StorageOptionsMapper() {}

    public static StorageOptions fromMap(Map<String, Object> root) {
        if (root == null) throw new IllegalArgumentException("config map is null");

        String backend = getString(root,"backend");
        StorageOptionsMappingStrategy strategy = strategies.get(backend);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown backend: " + backend);
        }
        return strategy.fromMap(root);
    }
}
