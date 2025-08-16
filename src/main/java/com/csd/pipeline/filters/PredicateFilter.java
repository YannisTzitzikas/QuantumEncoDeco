package com.csd.pipeline.filters;

import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.pipeline.Pipe;
import com.csd.core.pipeline.Filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PredicateFilter implements Filter<List<TripleComponent>, List<TripleComponent>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PredicateFilter.class);

    private final List<Pipe<List<TripleComponent>>> inputPipes;
    private final List<Pipe<List<TripleComponent>>> outputPipes;

    public PredicateFilter(
            List<Pipe<List<TripleComponent>>> inputPipes,
            List<Pipe<List<TripleComponent>>> outputPipes) {
        
        this.inputPipes = validatePipes(inputPipes, "inputPipes");
        this.outputPipes = validatePipes(outputPipes, "outputPipes");
    }

    @Override
    public void run() {
        try {
            apply();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Filter interrupted: {}", e.getMessage());
        }
    }

    @Override
    public void apply() throws Exception {
        while (!Thread.currentThread().isInterrupted()) {
            for (Pipe<List<TripleComponent>> inputPipe : inputPipes) {
                Message<List<TripleComponent>> msg = inputPipe.receive();
                
                if (msg.getKind() == Message.MessageKind.EOS) {
                    forwardEos();
                    return;
                }
                
                List<TripleComponent> filtered = msg.getPayload().stream()
                    .filter(c -> c.getRole() == TripleComponent.Role.PREDICATE)
                    .collect(Collectors.toList());
                
                Message<List<TripleComponent>> outputMsg = Message.data(filtered);
                for (Pipe<List<TripleComponent>> outputPipe : outputPipes) {
                    outputPipe.send(outputMsg);
                }
            }
        }
    }

    private void forwardEos() throws InterruptedException {
        for (Pipe<List<TripleComponent>> pipe : outputPipes) {
            pipe.send(Message.eos());
        }
    }

    private <T> List<T> validatePipes(List<T> pipes, String name) {
        Objects.requireNonNull(pipes, name + " cannot be null");
        if (pipes.isEmpty()) throw new IllegalArgumentException(name + " cannot be empty");
        return pipes;
    }
}