package com.csd.core.pipeline;

import com.csd.core.event.EventBus;
import com.csd.core.event.FilterLoopStartedEvent;
import com.csd.core.event.FilterStartedEvent;
import com.csd.core.event.FilterStoppedEvent;
import com.csd.core.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public abstract class AbstractPump implements Runnable {
    protected final Optional<EventBus> eventBus;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final List<OutputPort<?>> outputs;
    private final PortBindings bindings;
    private final long idleSleepMillis;
    private volatile boolean interrupted = false;
    private final String pumpName = getClass().toString();

    protected AbstractPump(List<OutputPort<?>> outputs,
                           PortBindings bindings) {
        this(outputs, bindings, 1, null); // default tiny sleep to avoid busy spin
    }

    protected AbstractPump(List<OutputPort<?>> outputs,
                           PortBindings bindings,
                           long idleSleepMillis,
                           EventBus eventBus) {
        this.outputs = outputs;
        this.bindings = bindings;
        this.idleSleepMillis = Math.max(0, idleSleepMillis);
        this.eventBus = eventBus != null ? Optional.of(eventBus) : Optional.empty();
    }

    @Override
    public final void run() {
        long startTime = System.nanoTime();
        try {
            onStart();
            eventBus.get().publish(new FilterStartedEvent(pumpName));
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
            eventBus.get().publish(new FilterStoppedEvent(pumpName, duration));
        }
    }

    private void loop() throws Exception {
        Emitter emitter = new Emitter(bindings);
        while (!Thread.currentThread().isInterrupted() && !interrupted) {
            long loopStart = System.nanoTime();
            eventBus.get().publish(new FilterLoopStartedEvent(pumpName));

            boolean produced = step(emitter);
            if (!produced) {
                if (idleSleepMillis > 0) Thread.sleep(idleSleepMillis);
                else Thread.yield();
            }

            long duration = System.nanoTime() - loopStart;
            eventBus.get().publish(new FilterStoppedEvent(pumpName, duration));
        }
        sendEosToAllOutputs();
    }

    /**
     * Do one unit of work. Return true if anything was emitted,
     * false if idle (to allow the loop to back off briefly).
     */
    protected abstract boolean step(Emitter out) throws Exception;

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
    private static <T> OutputPort<T> unsafe(OutputPort<?> p) { return (OutputPort<T>) p; }

    // Mirror the small helper from AbstractFilter for convenience
    public static final class Emitter {
        private final PortBindings bindings;
        Emitter(PortBindings bindings) { this.bindings = bindings; }

        public <T> void emit(OutputPort<T> port, Message<T> msg) throws InterruptedException {
            bindings.send(port, msg);
        }
    }
}
