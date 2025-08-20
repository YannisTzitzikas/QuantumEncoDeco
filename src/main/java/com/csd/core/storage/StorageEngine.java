package com.csd.core.storage;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface StorageEngine {

    /**
     * Insert or overwrite a single key/value pair.
     */
    void put(byte[] key, byte[] value) throws StorageException;

    /**
     * Retrieve the value for a key, or null if absent.
     */
    byte[] get(byte[] key) throws StorageException;

    /**
     * True if the key exists in the store.
     */
    boolean contains(byte[] key) throws StorageException;

    /**
     * Bulk insert or overwrite.
     */
    void putAll(Map<byte[], byte[]> entries) throws StorageException;

    /**
     * Bulk existence check; returns a BitSet aligned with the input order
     * where bit i is true if keys.get(i) exists.
     */
    BitSet containsAll(List<byte[]> keys) throws StorageException;


    List<byte[]> getAll(List<byte[]> keys) throws StorageException;

    /**
     * Sequentially stream all key/value pairs.
     * The caller MUST close the stream to release resources.
     */
    Stream<Map.Entry<byte[], byte[]>> entries() throws StorageException;

    /**
     * Remove all entries (optional operation).
     */
    default void clear() throws StorageException { }

    /**
     * Flush any pending writes (optional; depends on backend).
     */
    default void flush() throws StorageException { }

    /**
     * Close and release any resources.
     */
    default void close() throws StorageException { };
}
