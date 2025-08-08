package com.csd.common.utils.serializer;

import java.nio.ByteBuffer;

public class IntegerSerializer implements Serializer<Integer> {
    @Override
    public byte[] serialize(Integer value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    @Override
    public Integer deserialize(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}