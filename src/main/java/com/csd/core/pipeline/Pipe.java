package com.csd.core.pipeline;

import com.csd.model.Message;

public interface Pipe<T> {
    void send(Message<T> msg)   throws InterruptedException;
    Message<T> receive()        throws InterruptedException;
}
