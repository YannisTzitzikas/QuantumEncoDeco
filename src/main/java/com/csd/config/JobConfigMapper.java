package com.csd.config;

import java.util.*;

/**
 * Maps a format-agnostic configuration map into one or more {@link JobConfig}
 * instances.
 * This class assumes the input map has already been parsed from JSON, YAML,
 * etc.
 *
 * @author George Theodorakis (csd4881@csd.uoc.gr)
 */
public final class JobConfigMapper {

    /**
     * Converts a parsed configuration map into a list of {@link JobConfig} objects.
     * The input may represent a single config object or a list of them.
     *
     * @param raw the parsed configuration map (from IReader)
     * @return list of Config instances
     */
    public static JobConfig map(Map<String, Object> raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Config input is null");
        }

        return mapSingle(raw);
    }

    private static JobConfig mapSingle(Map<String, Object> obj) {
        String inputPath   = getString(obj, "input");
        String outputPath  = getString(obj, "output");
        String storagePath = getString(obj, "storage");

        return new JobConfig.Builder()
            .withInputPath(inputPath)
            .withOutputPath(outputPath)
            .withStorageSettingsPath(storagePath)
            .build();
    }

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : null;
    }
}