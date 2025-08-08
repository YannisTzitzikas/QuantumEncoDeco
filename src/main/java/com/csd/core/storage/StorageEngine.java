package com.csd.core.storage;

import java.util.Map;
import java.util.stream.Stream;

public interface StorageEngine {
    
    void                            put(String key, byte[] value)       throws StorageException;
    byte[]                          get(String key)                     throws StorageException;
    boolean                         contains(String key)                throws StorageException;
    void                            close()                             throws StorageException;


    // life cycle           
    default void                    clear()                             throws StorageException {}
    default void                    flush()                             throws StorageException {}

    // Streaming functions
    Stream<String>                   keys()                              throws StorageException;
    Stream<String>                   keys(String prefix)                 throws StorageException;
    Stream<Map.Entry<String,byte[]>> entries()                           throws StorageException;
}

