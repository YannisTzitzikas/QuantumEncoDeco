package com.csd.core.storage;

import java.util.HashMap;
import java.util.Map;

public class HashMapStorageEngine implements StorageEngine {
    private final Map<String, byte[]> storage = new HashMap<>();

    @Override
    public void put(String key, byte[] value) {
        storage.put(key, value);
    }

    @Override
    public byte[] get(String key) {
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
    }
}