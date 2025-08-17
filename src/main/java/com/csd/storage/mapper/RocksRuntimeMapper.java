package com.csd.storage.mapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.CompressionType;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import com.csd.core.storage.StorageException;
import com.csd.storage.options.RocksOptions;
import com.csd.storage.options.RocksRuntime;


public final class RocksRuntimeMapper {

    public static RocksRuntime toRuntime(RocksOptions ro) throws StorageException {
        try {
            Options o = new Options();
            CompressionType type = CompressionType.NO_COMPRESSION;

            switch (ro.getCompression()) {
                case NONE:  type = CompressionType.NO_COMPRESSION; break;
                case LZ4 :  type = CompressionType.LZ4_COMPRESSION; break;
                case ZSTD:  type = CompressionType.ZSTD_COMPRESSION; break;
            }

            o.setCreateIfMissing(true)
             .setIncreaseParallelism(ro.getParallelism())
             .setAllowMmapReads(false)
             .setAllowMmapWrites(false)
             .setCompressionType(type);

            // Optional: block cache
            if (ro.getBlockCacheMB() > 0) {
                LRUCache              cache     = new LRUCache((long) ro.getBlockCacheMB() * 1024 * 1024);
                BlockBasedTableConfig tableCfg  = new BlockBasedTableConfig()
                        .setBlockCache(cache);
                o.setTableFormatConfig(tableCfg);
            }


            ReadOptions r = new ReadOptions();
            r.setFillCache(ro.isFillCacheOnReads());
            
            WriteOptions w = new WriteOptions();

            w.setDisableWAL(ro.isDisableWAL())
             .setSync(ro.isSyncWrites());

            Path p = Paths.get(ro.getPath());
            Files.createDirectories(p);

            return new RocksRuntime(o, r, w, p, ro.getMultiGetChunk());
        } catch (Exception e) {
            throw new StorageException("Failed to build Rocks runtime", e);
        }
    }
}