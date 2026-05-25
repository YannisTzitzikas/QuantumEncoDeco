package com.csd.core.dag.execution.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// --- TRANSFORMATIONS ---
public record MapOp<In, Out>(Function<In, Out> mapper) implements Operator<In, Out> {
    @Override
    public List<Out> processBatch(List<In> batch) {
        List<Out> result = new ArrayList<>(batch.size());
        for (In item : batch) result.add(mapper.apply(item));
        return result;
    }
}
