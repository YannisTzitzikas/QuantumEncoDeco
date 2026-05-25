package com.csd.core.dag.execution.operations;

import java.util.List;

public interface Operator<In, Out> {
    List<Out> processBatch(List<In> batch);
}


