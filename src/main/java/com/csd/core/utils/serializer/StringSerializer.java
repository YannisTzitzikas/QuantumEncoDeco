package com.csd.core.utils.serializer;

import java.nio.charset.StandardCharsets;

// For String values
public class StringSerializer implements Serializer<String> {
    @Override
    public byte[] serialize(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String deserialize(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}