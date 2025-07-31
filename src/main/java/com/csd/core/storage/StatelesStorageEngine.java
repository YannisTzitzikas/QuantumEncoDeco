package com.csd.core.storage;

public class StatelesStorageEngine implements StorageEngine<Void> {

    @Override
    public void put(String key, Void value) throws StorageException {
        // Intentionally does nothing
    }

    @Override
    public Void get(String key) throws StorageException {
        return null; // Nothing to retrieve
    }

    @Override
    public boolean contains(String key) throws StorageException {
        return false; // Stateless, so never contains anything
    }

    @Override
    public void clear() throws StorageException {
        // Nothing to clear
    }

    @Override
    public void close() throws StorageException {
        // Nothing to close
    }
}
