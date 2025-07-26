package com.csd.core.codec;

public enum EncodingType {
    R1, 
    R2;

    public static EncodingType fromString(String encoding) {
        if (encoding == null || encoding.isEmpty()) {
            return R1; 
        }
        try {
            return EncodingType.valueOf(encoding.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Unknown encoding '" + encoding + "'. Using default.");
            return R1; 
        }
    }
}
