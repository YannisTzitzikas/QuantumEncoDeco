package com.csd.core.execution.operations;

import java.util.List;
import com.csd.core.api.functions.AdaptiveBatchSupplier;

/**
 * SOURCE Operational Node: Adapts dynamically to downstream backpressure capacity
 * by requesting precise microbatch sizes.
 */
public record BatchSourceOp<T>(AdaptiveBatchSupplier<T> batchSupplier) implements Operator<Integer, T> {
    
    @Override
    public List<T> processBatch(List<Integer> targetSizes) {
        int targetSize = (targetSizes != null && !targetSizes.isEmpty()) ? targetSizes.get(0) : 1000;
        return batchSupplier.getBatch(targetSize);
    }

    /**
     * Direct high-performance helper for the internal graph executor loop 
     * to bypass overhead from list-wrapping primitives.
     */
    public List<T> processBatchDirect(int targetSize) {
        return batchSupplier.getBatch(targetSize);
    }
}