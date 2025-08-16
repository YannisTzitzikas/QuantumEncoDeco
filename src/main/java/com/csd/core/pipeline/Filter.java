package com.csd.core.pipeline;

public interface Filter<I, O> extends Runnable {
    void apply() throws Exception;
}