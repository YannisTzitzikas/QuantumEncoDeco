package com.csd.core.codex.encoder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.csd.core.model.EncoderSettings;
import com.csd.core.model.EncodingData;

public class R1Encoder implements IEncoder<Integer> {

    private final EncoderSettings   settings = new EncoderSettings(true, null); 
    private final AtomicInteger     counter  = new AtomicInteger(0);

    @Override
    public Integer encode(EncodingData data) {
        return counter.getAndIncrement();
    }

    @Override
    public EncoderSettings getSettings() {
        return settings;
    } 

    @Override
    public void acceptParams(Map<String, Object> params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'acceptParams'");
    }
}
