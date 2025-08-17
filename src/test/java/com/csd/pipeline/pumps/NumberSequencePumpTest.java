package com.csd.pipeline.pumps;

import com.csd.core.model.Message;
import com.csd.core.pipeline.*;

import static org.junit.Assert.assertEquals;

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
        InMemoryPipe<List<Integer>> outPipe = new InMemoryPipe<>();
        PortBindings bindings = new PortBindings();
        bindings.bindOutput(NumberSequencePump.OUT, outPipe);

        // Pump: 1..100 in batches of 16
        NumberSequencePump pump = new NumberSequencePump(1, 100, 16, bindings);
        Thread t = new Thread(pump);
        t.start();

        // Collect
        List<Integer> all = new ArrayList<>();
        boolean done = false;
        while (!done) {
            Message<List<Integer>> msg = outPipe.receive();
            switch (msg.getKind()) {
                case DATA: all.addAll(msg.getPayload()); break;
                case EOS : done = true; break;
            }
        }

        t.join();

        // Assertions
        assertEquals("Size differs from the expected one! ", 100 , all.size());
        for (int i = 0; i < 100; i++) {
            assertEquals("Values are not existent in the map", new Integer(i + 1), all.get(i));
        }
    }
}
