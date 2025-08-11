package com.csd.common.io.reader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Factory and registry for {@link IQEDReader} implementations.
 * Allows lookup of readers by format name (e.g., "json", "yaml").
 * Readers must be registered explicitly or via static initialization.
 *
 * This class is thread-safe for read operations.
 *
 * @author George Theodorakis (csd4881@csd.uoc.gr)
 */
public final class QEDReaderFactory {

    private static final Map<String, IQEDReader> registry = new HashMap<>();

    private QEDReaderFactory() {
    }

    // Registry
    static {
        register("json", new QEDJsonReader());
    }

    /**
     * Registers a reader for a given format name.
     *
     * @param format the format name (e.g., "json")
     * @param reader the reader implementation
     */
    public static void register(String format, IQEDReader reader) {
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Format name cannot be null or empty");
        }
        if (reader == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }
        registry.put(format.toLowerCase(), reader);
    }

    /**
     * Retrieves a reader for the given format name.
     *
     * @param format the format name (e.g., "json")
     * @return the reader instance
     * @throws IllegalArgumentException if no reader is registered for the format
     */
    public static IQEDReader get(String format) {
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Format name cannot be null or empty");
        }
        IQEDReader reader = registry.get(format.toLowerCase());
        if (reader == null) {
            throw new IllegalArgumentException("No reader registered for format: " + format);
        }
        return reader;
    }

    /**
     * Checks if a reader is registered for the given format.
     *
     * @param format the format name
     * @return true if a reader is registered
     */
    public static boolean isRegistered(String format) {
        return registry.containsKey(format.toLowerCase());
    }

    /**
     * Returns all registered format names.
     *
     * @return set of format names
     */
    public static Set<String> getRegisteredFormats() {
        return new HashSet<>(registry.keySet());
    }

    /**
     * Determines the appropriate reader based on a file path or filename.
     *
     * @param filePath the full path or name of the file (e.g., "config.yaml", "data.json")
     * @return the registered reader that matches the file format
     * @throws IllegalArgumentException if no reader is available for the inferred format
     */
    public static IQEDReader getForFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filePath.length() - 1) {
            throw new IllegalArgumentException("Unable to determine format from file path: " + filePath);
        }

        String format = filePath.substring(dotIndex + 1).toLowerCase();
        return get(format); // delegate to existing method
    }

}
