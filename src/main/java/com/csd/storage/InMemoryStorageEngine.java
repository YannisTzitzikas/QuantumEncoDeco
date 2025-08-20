package com.csd.storage;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.csd.core.storage.StorageEngine;
import com.csd.core.storage.StorageException;
import com.csd.storage.options.InMemoryOptions;

/**
 * In-memory implementation of StorageEngine using a ConcurrentHashMap.
 * Keys are content-addressed (byte[] equality by content), values are stored as copies.
 */
public final class InMemoryStorageEngine implements StorageEngine {

    private final ConcurrentHashMap<Key, byte[]> map;
    private volatile boolean closed = false;

       public InMemoryStorageEngine(InMemoryOptions options) {
        if (options == null) throw new IllegalArgumentException("options is null");
        int cap = Math.max(16, options.getInitialCapacity());
        float lf = options.getLoadFactor();

        this.map = new ConcurrentHashMap<>(cap, lf);
    }

    @Override
    public void put(byte[] key, byte[] value) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        map.put(new Key(key), copy(value));
    }

    @Override
    public byte[] get(byte[] key) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(key, "key");
        byte[] val = map.get(new Key(key));
        return val == null ? null : copy(val);
    }

    @Override
    public boolean contains(byte[] key) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(key, "key");
        return map.containsKey(new Key(key));
    }

    @Override
    public void putAll(Map<byte[], byte[]> entries) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(entries, "entries");
        // Validate first to fail fast on nulls
        for (Map.Entry<byte[], byte[]> e : entries.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                throw new StorageException("putAll contains null key or value", new Exception());
            }
        }
        for (Map.Entry<byte[], byte[]> e : entries.entrySet()) {
            map.put(new Key(e.getKey()), copy(e.getValue()));
        }
    }

    @Override
    public BitSet containsAll(List<byte[]> keys) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(keys, "keys");
        BitSet result = new BitSet(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            byte[] k = keys.get(i);
            if (k == null) {
                throw new StorageException("containsAll: null key at index " + i, new Exception());
            }
            if (map.containsKey(new Key(k))) {
                result.set(i);
            }
        }
        return result;
    }

    @Override
    public List<byte[]> getAll(List<byte[]> keys) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(keys, "keys");
        List<byte[]> result = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
  
        }
        return result;
    }


    @Override
    public void clear() throws StorageException {
        ensureOpen();
        map.clear();
    }

    @Override
    public void flush() throws StorageException {
        ensureOpen();
    }

    @Override
    public void close() throws StorageException {
        if (closed) return;
        map.clear();
        closed = true;
    }

    private void ensureOpen() throws StorageException {
        if (closed) throw new StorageException("StorageEngine is closed", new Exception());
    }

    private static byte[] copy(byte[] src) {
        return Arrays.copyOf(src, src.length);
    }

    /**
     * Content-addressed key wrapper for byte[].
     * Stores an internal copy for immutability and caches hashCode.
     */
    private static final class Key {
        private final byte[] bytes;
        private final int hash; // cached

        Key(byte[] bytes) {
            this.bytes = Arrays.copyOf(bytes, bytes.length);
            this.hash = Arrays.hashCode(this.bytes);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key other = (Key) o;
            return Arrays.equals(this.bytes, other.bytes);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    @Override
    public Stream<Map.Entry<byte[], byte[]>> entries() throws StorageException {
        ensureOpen();
        // Wrap in new Key/Value copies for safety
        return map.entrySet().stream()
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(
                        copy(e.getKey().bytes), 
                        copy(e.getValue())));
    }

}
