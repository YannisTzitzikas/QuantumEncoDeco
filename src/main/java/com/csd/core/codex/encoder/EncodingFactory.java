package com.csd.core.codex.encoder;

import com.csd.core.storage.StorageEngine;

public class EncodingFactory {
        public static IEncoder<?> createEncoder(String encodingType, StorageEngine<?> storage) {
        switch (encodingType) {
            case "R1": return new R1Encoder((StorageEngine<Integer>)storage);
            case "R2": return new R2Encoder((StorageEngine<Integer>)storage);
            default: throw new IllegalArgumentException("Unknown encoder: " + encodingType);
        }
    }
}
