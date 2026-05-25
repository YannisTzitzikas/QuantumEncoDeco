package com.csd.core.execution.operations;

import java.util.List;
import java.util.function.Supplier;

// --- SOURCE (Returns Optional.empty() to signal EOF) ---
public record BatchSourceOp<T>(Supplier<List<T>> batchSupplier) implements Operator<Void, T> {
    @Override
    public List<T> processBatch(List<Void> ignored) {
        return batchSupplier.get(); 
    }
}
    