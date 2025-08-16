package com.csd.core.pipeline;

public interface Pipe<T> {
    void send(T msg) throws InterruptedException;
    T receive() throws InterruptedException;
}
