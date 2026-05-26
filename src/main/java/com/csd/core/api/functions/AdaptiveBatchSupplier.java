package com.csd.core.api.functions;

import java.util.List;

@FunctionalInterface
public interface AdaptiveBatchSupplier<T> {
    /**
     * Fetches a microbatch whose size scales dynamically based on downstream capacity.
     * * @param targetSize the max number of elements to read in this cycle.
     * @return a list of elements, or null/empty to signal End-Of-Stream (EOS).
     */
    List<T> getBatch(int targetSize);
}