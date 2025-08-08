package com.csd.common.utils.serializer;

public interface Serializer<V> {
    byte[] serialize(V value);
    V deserialize(byte[] bytes);
}