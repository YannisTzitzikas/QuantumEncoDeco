package com.csd.core.api;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.csd.core.api.functions.AdaptiveBatchSupplier;
import com.csd.core.execution.VirtualThreadGraphExecutor;
import com.csd.core.execution.operations.BatchSourceOp;
import com.csd.core.schema.StreamVertex;

public class StreamEnvironment {
    private final List<StreamVertex<?, ?>> nodes = new ArrayList<>();

    public void execute() {
        VirtualThreadGraphExecutor executor = new VirtualThreadGraphExecutor();
        try {
            executor.execute(this);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public <T> StreamBuilder<T> source(Collection<T> collection) {
        return fromSource(new CollectionSupplier<>(new ArrayList<>(collection)));
    }

    public <T> StreamBuilder<T> source(Supplier<Stream<T>> stream) {
        return fromSource(new OneTimeStreamSupplier<>(stream.get()));
    }

    public <T> StreamBuilder<T> continuousSource(Supplier<Stream<T>> streamSupplier) {
        return fromSource(targetSize -> {
            Stream<T> stream = streamSupplier.get();
            if (stream == null) return null;
            return stream.limit(targetSize).collect(Collectors.toList());
        });
    }

    // Updated to accept our adaptive contract
    public <T> StreamBuilder<T> fromSource(AdaptiveBatchSupplier<T> batchSupplier) {
        // Specify Integer as the IN parameter type for dynamic batch sizes
        StreamVertex<Integer, T> sourceNode = new StreamVertex<>(new BatchSourceOp<>(batchSupplier));
        registerNode(sourceNode);
        return new StreamBuilder<>(this, List.of(sourceNode));
    }
    
    void registerNode(StreamVertex<?, ?> node) { nodes.add(node); }
    public List<StreamVertex<?, ?>> getNodes() { return nodes; }

    // --- Dynamic Stateful Helpers ---

    private static class CollectionSupplier<T> implements AdaptiveBatchSupplier<T> {
        private final List<T> data;
        private int cursor = 0;

        CollectionSupplier(List<T> data) { this.data = data; }

        @Override
        public List<T> getBatch(int targetSize) {
            if (cursor >= data.size()) return null;
            int end = Math.min(cursor + targetSize, data.size());
            List<T> batch = data.subList(cursor, end);
            cursor = end;
            return batch;
        }
    }

    private static class OneTimeStreamSupplier<T> implements AdaptiveBatchSupplier<T> {
        private final Iterator<T> iterator;
        private boolean active = true;

        OneTimeStreamSupplier(Stream<T> stream) { 
            this.iterator = stream != null ? stream.iterator() : Collections.emptyIterator(); 
        }

        @Override
        public List<T> getBatch(int targetSize) {
            if (!active || !iterator.hasNext()) {
                active = false;
                return null;
            }
            List<T> batch = new ArrayList<>(targetSize);
            while (batch.size() < targetSize && iterator.hasNext()) {
                batch.add(iterator.next());
            }
            return batch;
        }
    }
}