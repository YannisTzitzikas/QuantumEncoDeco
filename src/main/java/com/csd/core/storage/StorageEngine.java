package com.csd.core.storage;

public interface StorageEngine {
    void    put(String key, byte[] value)    throws StorageException;
    byte[]  get(String key)             throws StorageException;
    boolean contains(String key)        throws StorageException;
    void    clear()                     throws StorageException;
    void    close()                     throws StorageException;
}

