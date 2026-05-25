package com.csd.core.execution.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record FlatMapOp<In, Out>(Function<In, List<Out>> flatMapper) implements Operator<In, Out> {
    @Override
    public List<Out> processBatch(List<In> batch) {
        List<Out> result = new ArrayList<>(batch.size() * 2); 
        
        for (In item : batch) {
            List<Out> mappedItems = flatMapper.apply(item);
            if (mappedItems != null) {
                result.addAll(mappedItems);
            }
        }
        return result;
    }
}