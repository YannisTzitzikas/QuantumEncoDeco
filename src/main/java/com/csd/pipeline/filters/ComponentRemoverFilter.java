package com.csd.pipeline.filters;

import com.csd.common.utils.serializer.Serializer;
import com.csd.common.utils.serializer.StringSerializer;
import com.csd.core.event.EventBus;
import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.pipeline.AbstractFilter;
import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.pipeline.StreamPolicy;
import com.csd.core.storage.StorageEngine;
import com.csd.core.storage.StorageException;
import com.csd.events.RecordBatchEvent;
import com.csd.events.UniqueEntityEvent;
import com.csd.events.UniquePredicateEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Filter that removes TripleComponents already present in the given StorageEngine.
 * Accepts a Set<TripleComponent> and emits a Set<String> of values not yet in storage.
 */
public class ComponentRemoverFilter extends AbstractFilter {

    public static final InputPort<Set<TripleComponent>> IN =
        new InputPort<>("components");

    public static final OutputPort<Set<TripleComponent>> OUT =
        new OutputPort<>("remaining");

    private final StorageEngine storage;
    private final Serializer<String> serializer;

    @Override
    protected void onStart() {
        super.onStart();
    }

    public ComponentRemoverFilter(PortBindings bindings, StorageEngine storage, EventBus bus) {
        super(Arrays.asList(IN), Arrays.asList(OUT), bindings, new StreamPolicy(), bus);
        this.storage = Objects.requireNonNull(storage, "storage");
        this.serializer = new StringSerializer();
    }   

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        Message<Set<TripleComponent>> msg = in.pop(IN);

        if (msg.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.eos());
            return;
        }

        final long startNanos = System.nanoTime();

        Set<TripleComponent> inputSet = msg.getPayload();
        if (inputSet == null || inputSet.isEmpty()) {

            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            logger.info("StorageFilter processed empty batch: input=0, removed=0, remaining=0, durationMs={}", elapsedMs);
            
            out.emit(OUT, Message.data(new HashSet<TripleComponent>()));
            return;
        }

        // Collect keys and their byte[] form
        List<TripleComponent> components = new ArrayList<>(inputSet.size());
        List<byte[]> keys = new ArrayList<>(inputSet.size());
        for (TripleComponent tc : inputSet) {
            String val = tc.getValue();
            components.add(tc);
            keys.add(serializer.serialize(val));
        }

        // Bulk check existence
        Set<TripleComponent> remaining = new HashSet<>();
        try {
            BitSet existsBits = storage.containsAll(keys);
            for (int i = 0; i < components.size(); i++) {
                if (!existsBits.get(i)) {
                    TripleComponent c = components.get(i);
                    remaining.add(c);

                    switch (c.getRole()) {
                        case PREDICATE: eventBus.get().publish(new UniquePredicateEvent()); break;
                        default:        eventBus.get().publish(new UniqueEntityEvent());
                    }
                }
            }
        } catch (StorageException e) {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            logger.error("StorageFilter failed during storage check after {} ms (input={}): {}",
                    elapsedMs, inputSet.size(), e.getMessage(), e);
            throw e;
        }

        int inputCount = inputSet.size();
        int remainingCount = remaining.size();
        int removedCount = inputCount - remainingCount;

        logger.info("StorageFilter processed batch: input={}, removed={}, remaining={}",
                inputCount, removedCount, remainingCount);

        eventBus.get().publish(new RecordBatchEvent());
        // Emit remaining values
        out.emit(OUT, Message.data(remaining));
    }
}
