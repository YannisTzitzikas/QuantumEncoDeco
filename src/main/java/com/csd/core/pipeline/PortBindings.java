package com.csd.core.pipeline;

import com.csd.core.model.Message;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PortBindings {
    private final Map<String, Pipe<?>> inputs = new ConcurrentHashMap<>();
    private final Map<String, Pipe<?>> outputs = new ConcurrentHashMap<>();

    public <T> void bindInput(InputPort<T> port, Pipe<T> pipe) {
        inputs.put(port.id(), pipe);
    }
    public <T> void bindOutput(OutputPort<T> port, Pipe<T> pipe) {
        outputs.put(port.id(), pipe);
    }

    @SuppressWarnings("unchecked")
    public <T> Message<T> receive(InputPort<T> port) throws InterruptedException {
        Pipe<T> pipe = (Pipe<T>) inputs.get(port.id());
        return pipe.receive();
    }

    @SuppressWarnings("unchecked")
    public <T> void send(OutputPort<T> port, Message<T> msg) throws InterruptedException {
        Pipe<T> pipe = (Pipe<T>) outputs.get(port.id());
        pipe.send(msg);
    }
}
