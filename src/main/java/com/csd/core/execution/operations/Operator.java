package com.csd.core.execution.operations;

import java.util.List;

public interface Operator<In, Out> {
    List<Out> processBatch(List<In> batch);
}


