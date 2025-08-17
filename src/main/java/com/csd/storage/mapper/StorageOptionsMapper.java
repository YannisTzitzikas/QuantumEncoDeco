package com.csd.storage.mapper;

import com.csd.core.storage.StorageOptions;

import java.util.HashMap;
import java.util.Map;

import static com.csd.common.utils.mapper.MappingUtils.getString;

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
