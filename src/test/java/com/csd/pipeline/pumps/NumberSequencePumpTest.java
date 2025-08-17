package com.csd.pipeline.pumps;

import com.csd.core.model.Message;
import com.csd.core.pipeline.*;
import com.csd.pipeline.filters.SplitPositiveNegativeFilter;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

public class NumberSequencePumpTest {

    static class InMemoryPipe<T> implements Pipe<T> {
        private final BlockingQueue<Message<T>> q = new LinkedBlockingQueue<>();
        @Override public void send(Message<T> msg) throws InterruptedException { q.put(msg); }
        @Override public Message<T> receive() throws InterruptedException { return q.take(); }
    }

    @Test
    public void emitsSequenceThenEos() throws Exception {
        // Pipe and bindings
        InMemoryPipe<List<Number>> outPipe = new InMemoryPipe<>();
        PortBindings bindings = new PortBindings();
        bindings.bindOutput(NumberSequencePump.OUT, outPipe);
        NumberSequencePump pump = new NumberSequencePump(1, 100, 16, bindings);

        // Setup pipes
        InMemoryPipe<List<Number>> posPipe = new InMemoryPipe<>();
        InMemoryPipe<List<Number>> negPipe = new InMemoryPipe<>();

        // Bind ports
        PortBindings filterBindings = new PortBindings();
        filterBindings.bindInput(SplitPositiveNegativeFilter.IN, outPipe);
        filterBindings.bindOutput(SplitPositiveNegativeFilter.OUT_POS, posPipe);
        filterBindings.bindOutput(SplitPositiveNegativeFilter.OUT_NEG, negPipe);
        SplitPositiveNegativeFilter filter = new SplitPositiveNegativeFilter(filterBindings);

        // Pump: 1..100 in batches of 16
        Thread t = new Thread(pump);
        Thread filterThread = new Thread(filter);
        t.start();
        filterThread.start();

        // Collect outputs
        List<Number> positives = new ArrayList<>();
        List<Number> negatives = new ArrayList<>();

        boolean posDone = false, negDone = false;
        while (!posDone || !negDone) {
            if (!posDone) {
                Message<List<Number>> msg = posPipe.receive();
                if (msg.getKind() == Message.MessageKind.EOS) posDone = true;
                else positives.addAll(msg.getPayload());
            }
            if (!negDone) {
                Message<List<Number>> msg = negPipe.receive();
                if (msg.getKind() == Message.MessageKind.EOS) negDone = true;
                else negatives.addAll(msg.getPayload());
            }
        }

        // Assertions
        for (Number n : positives) assertTrue("Expected positive: " + n, n.doubleValue() > 0);
        for (Number n : negatives) assertTrue("Expected negative: " + n, n.doubleValue() < 0);

        filterThread.join(); // Wait for filter to finish
        t.join();
    }
}
