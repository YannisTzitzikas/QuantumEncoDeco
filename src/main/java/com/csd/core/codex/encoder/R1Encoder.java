package com.csd.core.codex.encoder;

import java.util.concurrent.atomic.AtomicInteger;

import com.csd.core.model.EncodingData;
import com.csd.core.storage.StorageEngine;

public class R1Encoder extends StatefulEncoder<Integer> {

    private final AtomicInteger counter = new AtomicInteger(0);

    public R1Encoder(StorageEngine<Integer> engine) {
        super(engine);
    }

    @Override
    protected Integer createEncoding(EncodingData data) {
        return counter.getAndIncrement();
    }
}
