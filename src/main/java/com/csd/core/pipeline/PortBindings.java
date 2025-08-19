package com.csd.core.pipeline;

import com.csd.core.model.Message;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PortBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortBindings.class);
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
        if (pipe == null) {
            LOGGER.warn("Unbound input port: {}", port.id());
            return Message.data(null);
        }
        return pipe.receive();
    }

    @SuppressWarnings("unchecked")
    public <T> void send(OutputPort<T> port, Message<T> msg) throws InterruptedException {
        Pipe<T> pipe = (Pipe<T>) outputs.get(port.id());
        if (pipe == null) {
            LOGGER.warn("Unbound output port: {}", port.id());
            return;
        }
        pipe.send(msg);
    }
}
