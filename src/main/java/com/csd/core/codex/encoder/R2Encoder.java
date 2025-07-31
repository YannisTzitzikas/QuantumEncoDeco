package com.csd.core.codex.encoder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.csd.core.model.EncoderSettings;
import com.csd.core.model.EncodingData;
import com.csd.core.model.TripleComponent;

public class R2Encoder implements IEncoder<Integer> {

    private final EncoderSettings   settings         = new EncoderSettings(true, null); 

    private final AtomicInteger     entityCounter    = new AtomicInteger(0);
    private final AtomicInteger     predicateCounter = new AtomicInteger(0);

    @Override
    public Integer encode(EncodingData data) {
        return (data.getType() == TripleComponent.PREDICATE) ?
                predicateCounter.incrementAndGet() : entityCounter.incrementAndGet();
    }

    @Override
    public EncoderSettings getSettings() {
        return settings;
    }

    @Override
    public void acceptParams(Map<String, Object> params) {
        throw new UnsupportedOperationException("Unimplemented method 'acceptParams'");
    }
}
