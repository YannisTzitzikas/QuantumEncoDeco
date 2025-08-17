package com.csd.storage.options;

public final class RocksRuntime implements AutoCloseable {
    public final org.rocksdb.Options options;
    public final org.rocksdb.ReadOptions readOptions;
    public final org.rocksdb.WriteOptions writeOptions;
    public final java.nio.file.Path path;
    public final int multiGetChunk;

    public RocksRuntime(org.rocksdb.Options o, org.rocksdb.ReadOptions r, org.rocksdb.WriteOptions w,
                 java.nio.file.Path p, int multiGetChunk) {
        this.options = o; this.readOptions = r; this.writeOptions = w; this.path = p; this.multiGetChunk = multiGetChunk;
    }

    @Override public void close() {
        try { writeOptions.close(); } catch (Throwable ignore) {}
        try { readOptions.close(); } catch (Throwable ignore) {}
        try { options.close(); } catch (Throwable ignore) {}
    }
}