package com.csd.core.storage;

import java.util.HashMap;
import java.util.Map;

public class HashMapStorageEngine<V> implements StorageEngine<V> {
    private final Map<String, V> storage = new HashMap<>();

    @Override
    public void put(String key, V value) {
        storage.put(key, value);
    }

    @Override
    public V get(String key) {
        return storage.get(key);
    }

    @Override
    public boolean contains(String key) {
        return storage.containsKey(key);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public void close() {
        // No resources to release
    }
}