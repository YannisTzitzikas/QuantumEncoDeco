package com.csd.pipeline.filters;

import com.csd.core.model.Message;
import com.csd.core.pipeline.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;


public class SplitPositiveNegativeFilterTest {

    static class InMemoryPipe<T> implements Pipe<T> {
        private final BlockingQueue<Message<T>> queue = new LinkedBlockingQueue<>();
        @Override public void send(Message<T> msg) throws InterruptedException { queue.put(msg); }
        @Override public Message<T> receive() throws InterruptedException { return queue.take(); }
    }

    @Test
    public void testSplitFilter() throws Exception {
        // Setup pipes
        InMemoryPipe<List<Number>> inputPipe = new InMemoryPipe<>();
        InMemoryPipe<List<Number>> posPipe = new InMemoryPipe<>();
        InMemoryPipe<List<Number>> negPipe = new InMemoryPipe<>();

        // Bind ports
        PortBindings bindings = new PortBindings();
        bindings.bindInput(SplitPositiveNegativeFilter.IN, inputPipe);
        bindings.bindOutput(SplitPositiveNegativeFilter.OUT_POS, posPipe);
        bindings.bindOutput(SplitPositiveNegativeFilter.OUT_NEG, negPipe);

        // Create filter
        SplitPositiveNegativeFilter filter = new SplitPositiveNegativeFilter(bindings);
        Thread filterThread = new Thread(filter);
        filterThread.start();

        // Generate random numbers
        List<Number> input = IntStream.range(0, 100)
            .map(i -> new Random().nextInt(201) - 100) // range [-100, 100]
            .boxed()
            .collect(Collectors.toList());

        // Send input
        inputPipe.send(Message.data(input));
        inputPipe.send(Message.eos());

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

        int expectedPos = (int) input.stream().filter(n -> n.doubleValue() > 0).count();
        int expectedNeg = (int) input.stream().filter(n -> n.doubleValue() < 0).count();

        assertEquals( "Mismatch in positive count", expectedPos, positives.size());
        assertEquals( "Mismatch in negative count", expectedNeg, negatives.size());

        filterThread.join(); // Wait for filter to finish
    }
}
