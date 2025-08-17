package com.csd.storage.options;

import java.nio.file.Path;

import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

public final class RocksRuntime {
    public final Options options;
    public final ReadOptions readOptions;
    public final WriteOptions writeOptions;
    public final Path path;
    public final int multiGetChunk;

    public RocksRuntime(Options o, ReadOptions r, WriteOptions w, Path p, int multiGetChunk) {
        this.options = o; this.readOptions = r; this.writeOptions = w; this.path = p; this.multiGetChunk = multiGetChunk;
    }
}