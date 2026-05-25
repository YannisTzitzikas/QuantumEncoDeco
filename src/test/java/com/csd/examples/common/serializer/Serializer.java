package com.csd.examples.common.serializer;

public interface Serializer<V> {
    byte[] serialize(V value);
    V deserialize(byte[] bytes);
}