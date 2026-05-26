package com.csd.core.execution.operations;

import java.util.List;
import java.util.function.Function;

// --- TRANSFORMATIONS ---
public record BatchMapOp<In, Out>(Function<List<In>, List<Out>> mapper) implements Operator<In, Out> {
    @Override
    public List<Out> processBatch(List<In> batch) {
        return mapper.apply(batch);
    }
}
