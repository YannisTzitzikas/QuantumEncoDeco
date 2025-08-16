package com.csd.pipeline.pipes;

import com.csd.core.model.Message;
import com.csd.core.pipeline.Pipe;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingQueuePipe<T> implements Pipe<T> {
    private final BlockingQueue<Message<T>> queue;

    public BlockingQueuePipe(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public void send(Message<T> msg) throws InterruptedException {
        queue.put(msg);
    }

    @Override
    public Message<T> receive() throws InterruptedException {
        return queue.take();
    }
}
