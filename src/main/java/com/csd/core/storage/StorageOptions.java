package com.csd.core.storage;

/**
 * Base interface for all storage option types.
 * Provides common knobs and runtime statistics.
 */
public interface StorageOptions {
    GenericStorageStats getStats();
}
