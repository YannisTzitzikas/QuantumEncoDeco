package com.csd.examples.common.storage.mapper;

import java.util.Map;

import com.csd.examples.common.storage.core.StorageOptions;

public interface StorageOptionsMappingStrategy {
    StorageOptions fromMap(Map<String, Object> root);
}
