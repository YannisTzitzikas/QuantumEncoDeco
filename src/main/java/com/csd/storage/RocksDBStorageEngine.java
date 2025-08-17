package com.csd.storage;

import org.rocksdb.*;

import com.csd.core.storage.StorageEngine;
import com.csd.core.storage.StorageException;
import com.csd.storage.options.RocksRuntime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * RocksDB-backed StorageEngine with byte[] keys and values.
 */
public final class RocksDBStorageEngine implements StorageEngine {

    static {
        RocksDB.loadLibrary();
    }

    private final String dbPath;
    private final Options options;
    private final ReadOptions readOptions;
    private final WriteOptions writeOptions;
    private final RocksDB db;

    private volatile boolean closed = false;

    // Tune if you expect very large containsAll calls
    private static final int MULTIGET_CHUNK = 2048;

    /**
     * Open or create a RocksDB database at the given path with sane defaults.
     */
    public RocksDBStorageEngine(Path path) throws StorageException {
        this(path, defaultOptions(), new ReadOptions(), defaultWriteOptions());
    }

    public RocksDBStorageEngine(RocksRuntime runTime) throws StorageException
    {
        this(runTime.path, runTime.options, runTime.readOptions, runTime.writeOptions);
    }

    /**
     * Advanced constructor with caller-supplied options.
     */
    public RocksDBStorageEngine(
            Path path,
            Options options,
            ReadOptions readOptions,
            WriteOptions writeOptions
    ) throws StorageException {
        Objects.requireNonNull(path, "path");
        this.dbPath = path.toAbsolutePath().toString();
        this.options = Objects.requireNonNull(options, "options");
        this.readOptions = Objects.requireNonNull(readOptions, "readOptions");
        this.writeOptions = Objects.requireNonNull(writeOptions, "writeOptions");

        try {
            Files.createDirectories(path);
            this.db = RocksDB.open(this.options, this.dbPath);
        } catch (Exception e) {
            // Ensure options are closed if open() fails
            closeQuietly(this.writeOptions);
            closeQuietly(this.readOptions);
            closeQuietly(this.options);
            throw toStorageException("Failed to open RocksDB at " + dbPath, e);
        }
    }

    @Override
    public void put(byte[] key, byte[] value) throws StorageException {
        ensureOpen();
        requireNonNullKV(key, value);
        try {
            db.put(writeOptions, key, value);
        } catch (RocksDBException e) {
            throw toStorageException("put failed", e);
        }
    }

    @Override
    public byte[] get(byte[] key) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(key, "key");
        try {
            return db.get(readOptions, key);
        } catch (RocksDBException e) {
            throw toStorageException("get failed", e);
        }
    }

    @Override
    public boolean contains(byte[] key) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(key, "key");
        try {
            // Accurate existence check (keyMayExist can return false positives)
            return db.get(readOptions, key) != null;
        } catch (RocksDBException e) {
            throw toStorageException("contains failed", e);
        }
    }

    @Override
    public void putAll(Map<byte[], byte[]> entries) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(entries, "entries");
        // Validate first to fail fast on nulls
        for (Map.Entry<byte[], byte[]> e : entries.entrySet()) {
            requireNonNullKV(e.getKey(), e.getValue());
        }

        try (WriteBatch batch = new WriteBatch()) {
            for (Map.Entry<byte[],byte[]> e: entries.entrySet())
            {
                byte[] keyCopy = Arrays.copyOf(e.getKey(), e.getKey().length);
                byte[] valCopy = Arrays.copyOf(e.getValue(), e.getValue().length);
                batch.put(keyCopy, valCopy);
            }
            db.write(writeOptions, batch);
        } catch (RocksDBException e) {
            throw toStorageException("putAll failed", e);
        }
    }

    @Override
    public BitSet containsAll(List<byte[]> keys) throws StorageException {
        ensureOpen();
        Objects.requireNonNull(keys, "keys");
        final BitSet bits = new BitSet(keys.size());

        // Validate upfront for clearer errors
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i) == null) {
                throw new StorageException("containsAll: null key at index " + i, new RocksDBException("RocksDB error"));
            }
        }

        int i = 0;
        while (i < keys.size()) {
            int end = Math.min(i + MULTIGET_CHUNK, keys.size());
            List<byte[]> slice = keys.subList(i, end);
            // multiGetAsList aligns outputs with inputs; null => not found
            List<byte[]> values;
            try {
                values = db.multiGetAsList(slice);
            } catch (RocksDBException e) {
                throw new StorageException("containsAll: null key at index " + i, e);
            }

            for (int j = 0; j < values.size(); j++) {
                if (values.get(j) != null) {
                    bits.set(i + j);
                }
            }
            i = end;
        }
        return bits;
    }

    @Override
    public Stream<Map.Entry<byte[], byte[]>> entries() throws StorageException {
        ensureOpen();
        RocksIterator iterator = db.newIterator(readOptions);
        iterator.seekToFirst();
    
        // Build a Stream backed by the iterator
        Spliterator<Map.Entry<byte[], byte[]>> spliterator =
            Spliterators.spliteratorUnknownSize(new Iterator<Map.Entry<byte[], byte[]>>() {
                @Override
                public boolean hasNext() {
                    return iterator.isValid();
                }
    
                @Override
                public Map.Entry<byte[], byte[]> next() {
                    byte[] key = iterator.key();
                    byte[] value = iterator.value();
                    iterator.next();
                    return new AbstractMap.SimpleImmutableEntry<>(key, value);
                }
            }, Spliterator.ORDERED | Spliterator.NONNULL);
    
        // Ensure RocksIterator is closed when stream is closed
        return StreamSupport.stream(spliterator, false)
                            .onClose(iterator::close);
    }

    @Override
    public void clear() throws StorageException {
        ensureOpen();
        // Delete all keys via iterator + batched deletes
        try (RocksIterator it = db.newIterator(readOptions);
             WriteBatch batch = new WriteBatch()) {
            it.seekToFirst();
            int pending = 0;
            while (it.isValid()) {
                batch.delete(it.key());
                pending++;
                if (pending >= 4096) {
                    db.write(writeOptions, batch);
                    batch.clear();
                    pending = 0;
                }
                it.next();
            }
            if (pending > 0) {
                db.write(writeOptions, batch);
            }
        } catch (RocksDBException e) {
            throw toStorageException("clear failed", e);
        }
    }

    @Override
    public void flush() throws StorageException {
        ensureOpen();
        try (FlushOptions fo = new FlushOptions()) {
            fo.setWaitForFlush(true);
            db.flush(fo);
        } catch (RocksDBException e) {
            throw toStorageException("flush failed", e);
        }
    }

    @Override
    public void close() throws StorageException {
        if (closed) return;
        closed = true;
        // Order matters: db depends on options
        try {
            db.close();
        } catch (Throwable ignore) { }
        closeQuietly(writeOptions);
        closeQuietly(readOptions);
        closeQuietly(options);
    }

    // ----------------- helpers -----------------

    private void ensureOpen() throws StorageException {
        if (closed) throw new StorageException("StorageEngine is closed", new RocksDBException("RocksDB Exception"));
    }

    private static void requireNonNullKV(byte[] key, byte[] value) {
        if (key == null) throw new NullPointerException("key");
        if (value == null) throw new NullPointerException("value");
    }

    private static StorageException toStorageException(String msg, Exception e) {
        return new StorageException(msg, e);
    }

    private static void closeQuietly(AbstractImmutableNativeReference r) {
        if (r != null) {
            try { r.close(); } catch (Throwable ignore) { }
        }
    }

    private static Options defaultOptions() {
        // Good general-purpose defaults
        Options opt = new Options();
        opt.setCreateIfMissing(true)
           .setIncreaseParallelism(Math.max(2, Runtime.getRuntime().availableProcessors()))
           .setUseFsync(false)
           .setAllowMmapReads(false)
           .setAllowMmapWrites(false);
        // You can tweak block cache, compaction, etc., here if needed.
        return opt;
    }

    private static WriteOptions defaultWriteOptions() {

        WriteOptions wo = new WriteOptions();

        wo.setDisableWAL(false)
          .setSync(false);

        return wo;
    }
}
