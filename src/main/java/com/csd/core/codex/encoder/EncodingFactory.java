package com.csd.core.codex.encoder;

public class EncodingFactory {
        public static IEncoder<?> createEncoder(String encodingType) {
        switch (encodingType) {
            case "R1": return new R1Encoder();
            case "R2": return new R2Encoder();
            default: throw new IllegalArgumentException("Unknown encoder: " + encodingType);
        }
    }
}
