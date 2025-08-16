package com.csd.core.pipeline;

public interface Port<T> {
    String id();
    Class<T> payloadType();
    PortDirection direction();
}