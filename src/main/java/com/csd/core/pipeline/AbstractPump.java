package com.csd.core.pipeline;

import com.csd.core.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractPump implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<OutputPort<?>> outputs;
    private final PortBindings bindings;
    private final long idleSleepMillis;
    private volatile boolean interrupted = false;

    protected AbstractPump(List<OutputPort<?>> outputs,
                           PortBindings bindings) {
        this(outputs, bindings, 1); // default tiny sleep to avoid busy spin
    }

    protected AbstractPump(List<OutputPort<?>> outputs,
                           PortBindings bindings,
                           long idleSleepMillis) {
        this.outputs = outputs;
        this.bindings = bindings;
        this.idleSleepMillis = Math.max(0, idleSleepMillis);
    }

    @Override
    public final void run() {
        try {
            loop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Pump interrupted");
        } catch (Exception e) {
            logger.error("Pump crashed", e);
            propagateEosOnCrash();
        }
    }

    private void loop() throws Exception {
        Emitter emitter = new Emitter(bindings);
        while (!Thread.currentThread().isInterrupted() && !interrupted) {
            boolean produced = step(emitter);
            if (!produced) {
                if (idleSleepMillis > 0) Thread.sleep(idleSleepMillis);
                else Thread.yield();
            }
        }
        sendEosToAllOutputs();
    }

    /**
     * Do one unit of work. Return true if anything was emitted,
     * false if idle (to allow the loop to back off briefly).
     */
    protected abstract boolean step(Emitter out) throws Exception;

    protected void stop() { interrupted = true; }

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
