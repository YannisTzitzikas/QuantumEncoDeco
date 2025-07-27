package com.csd.core.storage;

import com.csd.core.exception.StorageException;

public interface StorageEngine<V> {
    void    put(String key, V value)    throws StorageException;
    V       get(String key)             throws StorageException;
    boolean contains(String key)        throws StorageException;
    void    clear()                     throws StorageException;
    void    close()                     throws StorageException;
}

