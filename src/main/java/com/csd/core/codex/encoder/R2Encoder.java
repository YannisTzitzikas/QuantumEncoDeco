package com.csd.core.codex.encoder;

import java.util.concurrent.atomic.AtomicInteger;

import com.csd.core.model.EncodingData;
import com.csd.core.storage.StorageEngine;

public class R2Encoder extends StatefulEncoder<Integer> {

    private final AtomicInteger entityCounter = new AtomicInteger(0);
    private final AtomicInteger predicateCounter = new AtomicInteger(0);

    public R2Encoder(StorageEngine<Integer> engine) {
        super(engine);
    }

    @Override
    protected Integer createEncoding(EncodingData data) {
        return (data.getType() == EncodingData.TripleComponent.PREDICATE) ?
                predicateCounter.incrementAndGet() : entityCounter.incrementAndGet();
    }

}
