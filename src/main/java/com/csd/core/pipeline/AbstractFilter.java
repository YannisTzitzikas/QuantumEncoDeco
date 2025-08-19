package com.csd.core.pipeline;

import com.csd.core.event.EventBus;
import com.csd.core.event.FilterLoopStartedEvent;
import com.csd.core.event.FilterLoopStoppedEvent;
import com.csd.core.event.FilterStartedEvent;
import com.csd.core.event.FilterStoppedEvent;

import com.csd.core.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractFilter implements Runnable {

    protected final Optional<EventBus> eventBus;
    protected final Logger   logger = LoggerFactory.getLogger(getClass());

    private final List<InputPort<?>>    inputs;
    private final List<OutputPort<?>>   outputs;
    private final PortBindings          bindings;
    private final CoordinationPolicy    policy;
    private final String                filterName;

    private final Map<String, Deque<Message<?>>> inbox = new LinkedHashMap<>();
    private volatile boolean interrupted = false;

    protected AbstractFilter(List<InputPort<?>> inputs,
                             List<OutputPort<?>> outputs,
                             PortBindings bindings,
                             CoordinationPolicy policy,
                             EventBus eventBus) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.bindings = bindings;
        this.policy  = policy;
        this.filterName = getClass().toString();
        this.eventBus = eventBus != null ? Optional.of(eventBus) : Optional.empty();
        inputs.forEach(p -> inbox.put(p.id(), new ArrayDeque<>()));
    }

    @Override
    public final void run() {
        long startTime = System.nanoTime();
        try {
            onStart();
            eventBus.get().publish(new FilterStartedEvent(filterName));
            loop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Filter interrupted");
        } catch (Exception e) {
            logger.error("Filter crashed", e);
            propagateEosOnCrash();
            onStop();
        }
        finally
        {
            long duration = System.nanoTime() - startTime;
            eventBus.get().publish(new FilterStoppedEvent(filterName, duration));
        }
    }

    private void loop() throws Exception {
        while (!Thread.currentThread().isInterrupted() && !interrupted) {
            long loopStart = System.nanoTime();
            eventBus.get().publish(new FilterLoopStartedEvent(filterName));

            // 1) Pull one message from each input that currently has an empty queue (non-greedy)
            for (InputPort<?> in : inputs) {
                Deque<Message<?>> q = inbox.get(in.id());
                if (q.isEmpty()) {
                    Message<?> msg = bindings.receive(unsafe(in));
                    q.addLast(msg);
                }
            }

            // 2) Decide if we can process
            PortInbox snapshot = new PortInbox(inbox);
            if (policy.shouldProcess(snapshot)) {
                // Create a view that allows safe pop by type
                Batch batch = new Batch(inbox);
                Emitter emitter = new Emitter(bindings);
                process(batch, emitter);
                long duration = System.nanoTime() - loopStart;
                eventBus.get().publish(new FilterLoopStoppedEvent(filterName, duration));
                // If any batch input queue still carries EOS as head without being consumed,
                // leave it — policy will terminate when all are EOS.
            } else if (policy.shouldTerminate(snapshot)) {
                long duration = System.nanoTime() - loopStart;
                eventBus.get().publish(new FilterLoopStoppedEvent(filterName, duration));
                sendEosToAllOutputs();
                return;
            } else {
                // Blockingly fetch at least one more message to make progress
                // Pick the first input with empty queue, otherwise just read round-robin
                boolean needMore = inbox.values().stream().anyMatch(Deque::isEmpty);
                InputPort<?> target = needMore ? firstEmptyInput() : inputs.get(0);
                Message<?> msg = bindings.receive(unsafe(target));
                inbox.get(target.id()).addLast(msg);
                long duration = System.nanoTime() - loopStart;
                eventBus.get().publish(new FilterLoopStoppedEvent(filterName, duration));
            }
        }
    }

    protected abstract void process(Batch in, Emitter out) throws Exception;

    protected void onStart() {}  // Default no-op
    protected void onStop() { interrupted = true; }

    private void sendEosToAllOutputs() throws InterruptedException {
        for (OutputPort<?> out : outputs) {
            bindings.send(unsafe(out), Message.eos());
        }
    }

    private void propagateEosOnCrash() {
        try { sendEosToAllOutputs(); } catch (InterruptedException ignored) {}
    }

    @SuppressWarnings("unchecked")
    private static <T> InputPort<T> unsafe(InputPort<?> p) { return (InputPort<T>) p; }
    @SuppressWarnings("unchecked")
    private static <T> OutputPort<T> unsafe(OutputPort<?> p) { return (OutputPort<T>) p; }

    private InputPort<?> firstEmptyInput() {
        for (InputPort<?> in : inputs) if (inbox.get(in.id()).isEmpty()) return in;
        return inputs.get(0);
    }

    // Helper types exposed to concrete filters
    public static final class Batch {
        private final Map<String, Deque<Message<?>>> inbox;
        Batch(Map<String, Deque<Message<?>>> inbox) { this.inbox = inbox; }

        @SuppressWarnings("unchecked")
        public <T> Optional<Message<T>> peek(InputPort<T> port) {
            Deque<Message<?>> q = inbox.get(port.id());
            if (q == null || q.isEmpty()) return Optional.empty();
            return Optional.of((Message<T>) q.peekFirst());
        }

        @SuppressWarnings("unchecked")
        public <T> Message<T> pop(InputPort<T> port) {
            return (Message<T>) inbox.get(port.id()).removeFirst();
        }
    }

    public static final class Emitter {
        private final PortBindings bindings;
        Emitter(PortBindings bindings) { this.bindings = bindings; }

        public <T> void emit(OutputPort<T> port, Message<T> msg) throws InterruptedException {
            bindings.send(port, msg);
        }
    }
}
