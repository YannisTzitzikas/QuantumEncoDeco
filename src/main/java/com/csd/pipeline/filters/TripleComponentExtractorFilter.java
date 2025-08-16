package com.csd.pipeline.filters;

import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.model.uri.URITriple;

import com.csd.core.pipeline.Filter;
import com.csd.core.pipeline.Pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripleComponentExtractorFilter implements Filter<Iterable<URITriple>, List<TripleComponent>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TripleComponentExtractorFilter.class);

    private final List<Pipe<Iterable<URITriple>>> inputPipes;
    private final List<Pipe<List<TripleComponent>>> outputPipes;

    public TripleComponentExtractorFilter(
            List<Pipe<Iterable<URITriple>>> inputPipes,
            List<Pipe<List<TripleComponent>>> outputPipes) {
        
        Objects.requireNonNull(inputPipes, "Input pipes cannot be null");
        Objects.requireNonNull(outputPipes, "Output pipes cannot be null");
        
        if (inputPipes.isEmpty()) {
            throw new IllegalArgumentException("At least one input pipe is required");
        }
        
        if (outputPipes.isEmpty()) {
            throw new IllegalArgumentException("At least one output pipe is required");
        }

        LOGGER.info("No. Input Pipes: {}", inputPipes.size());
        LOGGER.info("No. Output Pipes: {}",outputPipes.size());
        
        this.inputPipes = inputPipes;
        this.outputPipes = outputPipes;
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
            // Process all input pipes
            for (Pipe<Iterable<URITriple>> inputPipe : inputPipes) {
                Message<Iterable<URITriple>> inputMsg = inputPipe.receive();
                
                if (inputMsg.getKind() == Message.MessageKind.EOS) {
                    forwardEosToOutputs();
                    return;
                }
                
                // Process data message
                Iterable<URITriple> triples = inputMsg.getPayload();
                List<TripleComponent> components = extractComponents(triples);
                Message<List<TripleComponent>> outputMsg = Message.data(components);
                
                // Send to all output pipes
                for (Pipe<List<TripleComponent>> outputPipe : outputPipes) {
                    outputPipe.send(outputMsg);
                }
            }
        }
    }

    private List<TripleComponent> extractComponents(Iterable<URITriple> triples) {
        List<TripleComponent> components = new ArrayList<>();
        for (URITriple triple : triples) {
            components.add(triple.getSubject());
            components.add(triple.getPredicate());
            components.add(triple.getObject());
        }
        return components;
    }

    private void forwardEosToOutputs() throws InterruptedException {
        for (Pipe<List<TripleComponent>> outputPipe : outputPipes) {
            outputPipe.send(Message.eos());
        }
    }
}