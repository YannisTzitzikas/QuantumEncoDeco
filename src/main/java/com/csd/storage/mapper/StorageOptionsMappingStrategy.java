package com.csd.storage.mapper;

import com.csd.core.storage.StorageOptions;

import java.util.Map;

public interface StorageOptionsMappingStrategy {
    StorageOptions fromMap(Map<String, Object> root);
}
