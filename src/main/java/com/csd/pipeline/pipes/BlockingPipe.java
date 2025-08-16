package com.csd.pipeline.pipes;

import com.csd.core.pipeline.Pipe;

public class BlockingPipe<T> implements Pipe<T> {

    @Override
    public void send(T msg) throws InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }

    @Override
    public T receive() throws InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'receive'");
    }
}
