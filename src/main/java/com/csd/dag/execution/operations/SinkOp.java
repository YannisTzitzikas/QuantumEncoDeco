package com.csd.dag.execution.operations;

import java.util.List;
import java.util.function.Consumer;

// --- SINK ---
public record SinkOp<T>(Consumer<T> batchConsumer) implements Operator<T, Void> {
    @Override
    public List<Void> processBatch(List<T> batch) {
      for (T item : batch) {
            batchConsumer.accept(item);
        }
    
        return List.of(); // Sinks produce no output
    }
}