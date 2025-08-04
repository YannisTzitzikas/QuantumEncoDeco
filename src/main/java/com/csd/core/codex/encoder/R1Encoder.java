package com.csd.core.codex.encoder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.csd.core.model.EncoderInfo;
import com.csd.core.model.JobContext;
import com.csd.core.model.EncodingData;
import com.csd.core.storage.StorageException;
import com.csd.core.model.JobContext.JobStatus;

import com.csd.core.utils.serializer.Serializer;
import com.csd.core.utils.serializer.IntegerSerializer;

public class R1Encoder implements IEncoder<Integer> {

    private final EncoderInfo info;
    private final AtomicInteger counter;

    private final Serializer<Integer> serializer;

    private JobContext context;

    public R1Encoder() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("startOffset", 0);

        this.info = new EncoderInfo(
                "R1",
                true,
                true,
                Collections.unmodifiableMap(defaults));

        counter     = new AtomicInteger(0);
        serializer  = new IntegerSerializer(); 
    }

    @Override
    public void setContext(JobContext context) {
        if (this.context != null && this.context.getStatus() == JobStatus.RUNNING) {
            System.err.println("Current encoding Process is still running.");
            return;
        }

        if (context == null)
            throw new IllegalArgumentException("Context cannot be null");

        if (info.isStateful() && context.getStorageEngine() == null)
            throw new IllegalArgumentException("Encoder is Stateful, yet no Storage Engine was provided");

        this.context = context;
        Map<String, Object> resolvedParams = resolveParametersFiltered();

        counter.set((Integer) resolvedParams.getOrDefault("startOffset", 0));
    }

    @Override
    public JobContext getContext() {
        return context;
    }

    @Override
    public EncoderInfo getInfo() {
        return info;
    }

    @Override
    public Integer encode(EncodingData data) {
        if (context == null) {
            throw new IllegalStateException("Encoder context not set.");
        }
        String key = data.getValue();

        try {

            byte[] raw = context.getStorageEngine().get(key);
            if (raw != null) {
                Integer existingCode = serializer.deserialize(raw);
                return existingCode;
            }

            Integer newCode = counter.getAndIncrement();
            context.getStorageEngine().put(key, serializer.serialize(newCode));

            return newCode;
        } catch (StorageException e) {
            throw new RuntimeException("Failed to access storage engine", e);
        }
    }

    private Map<String, Object> resolveParametersFiltered() {
        Map<String, Object> resolved = new HashMap<>(info.getDefaultParameters());

        if (context != null && context.getParameters() != null) {
            for (String key : info.getDefaultParameters().keySet()) {
                if (context.getParameters().containsKey(key)) {
                    resolved.put(key, context.getParameters().get(key));
                }
            }
        }
        return resolved;
    }

    @Override
    public String getFinalEncoding(EncodingData data) {
        if (context.getStatus() != JobStatus.DONE) {
            System.err.println("The encoding process is not finalized yet");
            return null;
        }
        
        Integer encodedData = encode(data);
        String binaryString = Integer.toBinaryString(encodedData);

        return String.format("%" + counter.get() + "s", binaryString).replace(' ', '0');
    }

}
