package com.csd.core.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.csd.core.execution.VirtualThreadGraphExecutor;
import com.csd.core.execution.operations.BatchSourceOp;
import com.csd.core.schema.StreamVertex;

public class StreamEnvironment {
    private final List<StreamVertex<?, ?>> nodes = new ArrayList<>();

    public void execute() 
    {
        VirtualThreadGraphExecutor executor = new VirtualThreadGraphExecutor();
        
        try
        {
            executor.execute(this);
        } catch(InterruptedException e)
        {
            System.out.println(e.getStackTrace());
        }

    }

    /**
     * Entry Point 1: Static Collection (One-Time)
     */
    public <T> StreamBuilder<T> source(Collection<T> collection) {
        return fromSource(new CollectionSupplier<>(new ArrayList<>(collection)));
    }

    /**
     * Entry Point 2: One-Time Stream (Consumes the stream once and finishes)
     */
    public <T> StreamBuilder<T> source(Supplier<Stream<T>> stream) {
        return fromSource(new OneTimeStreamSupplier<>(stream.get()));
    }

    /**
     * Entry Point 3: Continuous Polling / Dynamic UDF
     * This will loop indefinitely UNTIL the supplier returns an empty stream or null.
     */
    public <T> StreamBuilder<T> continuousSource(Supplier<Stream<T>> streamSupplier) {
        return fromSource(() -> {
            Stream<T> stream = streamSupplier.get();
            if (stream == null) return null;
            return stream.collect(Collectors.toList());
        });
    }

    // Generic Internal Entry Point
    public <T> StreamBuilder<T> fromSource(Supplier<List<T>> batchSupplier) {
        StreamVertex<Void, T> sourceNode = new StreamVertex<>(new BatchSourceOp<>(batchSupplier));
        registerNode(sourceNode);
        return new StreamBuilder<>(this, List.of(sourceNode));
    }

    void registerNode(StreamVertex<?, ?> node) { nodes.add(node); }
    public List<StreamVertex<?, ?>> getNodes() { return nodes; }

    // --- Stateful Helpers to protect against infinite loops ---

    private static class CollectionSupplier<T> implements Supplier<List<T>> {
        private List<T> data;
        CollectionSupplier(List<T> data) { this.data = data; }
        @Override
        public List<T> get() {
            List<T> batch = data;
            data = null; // Kill switch after first delivery
            return batch;
        }
    }

    private static class OneTimeStreamSupplier<T> implements Supplier<List<T>> {
        private Stream<T> stream;
        OneTimeStreamSupplier(Stream<T> stream) { this.stream = stream; }
        @Override
        public List<T> get() {
            if (stream == null) return null;
            List<T> batch = stream.collect(Collectors.toList());
            stream = null; // Kill switch: cannot consume a stream twice anyway
            return batch;
        }
    }


}