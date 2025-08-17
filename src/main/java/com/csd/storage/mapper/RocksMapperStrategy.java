package com.csd.storage.mapper;

import com.csd.core.storage.StorageOptions;
import com.csd.storage.options.Compression;
import com.csd.storage.options.RocksOptions;

import java.util.Locale;
import java.util.Map;

import static com.csd.common.utils.mapper.MappingUtils.*;

public class RocksMapperStrategy implements StorageOptionsMappingStrategy {
    @Override
    public StorageOptions fromMap(Map<String, Object> root) {
        Map<String,Object> m = getMap(root, "rocksdb");
        m = m != null ? m : root;

        String path = getString(m, "path");
        
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("rocksdb.path is required");
        }
        RocksOptions.Builder b = RocksOptions.builder(path);

        Boolean disableWAL = getBoolean(m, "disableWAL");
        if (disableWAL != null) b.disableWAL(disableWAL);

        Boolean syncWrites = getBoolean(m, "syncWrites");
        if (syncWrites != null) b.syncWrites(syncWrites);

        Integer parallelism = getInt(m, "parallelism");
        if (parallelism != null) b.parallelism(parallelism);

        Boolean fillCache = getBoolean(m, "fillCacheOnReads");
        if (fillCache != null) b.fillCacheOnReads(fillCache);

        Integer cacheMB = getInt(m, "blockCacheMB");
        if (cacheMB != null) b.blockCacheMB(cacheMB);

        String compStr = getString(m, "compression");
        if (compStr != null) b.compression(parseCompression(compStr));

        Integer multiGetChunk = getInt(m, "multiGetChunk");
        if (multiGetChunk != null) b.multiGetChunk(multiGetChunk);

        return b.build();
    }

    public static Compression parseCompression(String s) {
        String v = s.trim().toUpperCase(Locale.ROOT);
        if ("NONE".equals(v)) return Compression.NONE;
        if ("ZSTD".equals(v)) return Compression.ZSTD;
        return Compression.LZ4;
    }
}
