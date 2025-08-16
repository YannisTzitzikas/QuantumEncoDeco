package com.csd.core.pipeline;

public interface Sink<I> extends AutoCloseable {
    void accept() throws Exception;
    @Override default void close() throws Exception {}
}