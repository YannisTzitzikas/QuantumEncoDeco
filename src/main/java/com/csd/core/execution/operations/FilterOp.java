package com.csd.core.execution.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record FilterOp<T>(Predicate<T> predicate) implements Operator<T, T> {
    @Override
    public List<T> processBatch(List<T> batch) {
        List<T> result = new ArrayList<>(batch.size());
        for (T item : batch) {
            if (predicate.test(item)) result.add(item);
        }
        return result;
    }
}
