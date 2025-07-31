package com.csd.core.storage;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import com.csd.core.utils.serializer.Serializer;

public class RocksDBStorageEngine<V> implements StorageEngine<V> {
    private final RocksDB db;
    private final Serializer<V> serializer;
    
    public RocksDBStorageEngine(String dbPath, Serializer<V> serializer) throws RocksDBException {
        RocksDB.loadLibrary();
        this.serializer = serializer;
        Options options = new Options().setCreateIfMissing(true);
        this.db = RocksDB.open(options, dbPath);
    }

    @Override
    public void put(String key, V value) throws StorageException {
        try {
            byte[] serialized = serializer.serialize(value);
            db.put(key.getBytes(), serialized);
        } catch (RocksDBException e) {
            throw new StorageException("Put operation failed", e);
        }
    }

    @Override
    public V get(String key) throws StorageException {
        try {
            byte[] bytes = db.get(key.getBytes());
            return bytes != null ? serializer.deserialize(bytes) : null;
        } catch (RocksDBException e) {
            throw new StorageException("Get operation failed", e);
        }
    }

    @Override
    public boolean contains(String key) throws StorageException {
        return get(key) != null;
    }

    @Override
    public void clear() throws StorageException {
        try (RocksIterator it = db.newIterator()) {
            for (it.seekToFirst(); it.isValid(); it.next()) {
                db.delete(it.key());
            }
        } catch (RocksDBException e) {
            throw new StorageException("Clear operation failed", e);
        }
    }

    @Override
    public void close() throws StorageException {
        db.close();
    }
}