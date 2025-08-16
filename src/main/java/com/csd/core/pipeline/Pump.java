package com.csd.core.pipeline;

public interface Pump <O> extends AutoCloseable {
    void next() throws Exception;
    @Override default void close() throws Exception {}
}
