package com.csd.core.storage;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class RocksDBStorageEngine implements StorageEngine {
    private final RocksDB db;
    
    public RocksDBStorageEngine(String dbPath) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true);
        this.db = RocksDB.open(options, dbPath);
    }

    @Override
    public void put(String key, byte[] value) throws StorageException {
        try {
            db.put(key.getBytes(), value);
        } catch (RocksDBException e) {
            throw new StorageException("Put operation failed", e);
        }
    }

    @Override
    public byte[] get(String key) throws StorageException {
        try {
            byte[] bytes = db.get(key.getBytes());
            return bytes;
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