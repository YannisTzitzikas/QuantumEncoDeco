package com.csd.storage.mapper;

import com.csd.core.storage.StorageOptions;
import com.csd.storage.options.InMemoryOptions;

import java.util.Map;

import static com.csd.common.utils.mapper.MappingUtils.*;

public class InMemoryMapperStrategy implements StorageOptionsMappingStrategy {
    @Override
    public StorageOptions fromMap(Map<String, Object> root) {
        Map<String, Object> m = getMap(root, "inMemory");
        InMemoryOptions.Builder b = InMemoryOptions.builder();
        m  = m != null ? m : root; 

        Integer initCap = getInt(m ,"initialCapacity");
        if (initCap != null) b.initialCapacity(initCap);
        
        Float load = getFloat(m, "loadFactor");
        if (load != null) b.loadFactor(load);

        return b.build();
    }
}
