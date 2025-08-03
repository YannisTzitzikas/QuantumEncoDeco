package com.csd.core.codex.encoder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.csd.core.model.EncoderInfo;
import com.csd.core.model.EncodingContext;
import com.csd.core.model.EncodingData;
import com.csd.core.model.TripleComponent;
import com.csd.core.model.EncodingContext.EncodingStatus;
import com.csd.core.storage.StorageException;

import com.csd.core.utils.serializer.Serializer;
import com.csd.core.utils.serializer.IntegerSerializer;

public class R2Encoder implements IEncoder<Integer> {

    private final EncoderInfo info;
    private final AtomicInteger predicateCounter;
    private final AtomicInteger entityCounter;

    private final Serializer<Integer> serializer;

    private EncodingContext context;

    public R2Encoder() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("predicateStartOffset", 0);
        defaults.put("entityStartOffset", 0);

        this.info = new EncoderInfo(
                "R2",
                true,
                true,
                Collections.unmodifiableMap(defaults));

        predicateCounter = new AtomicInteger(0);
        entityCounter = new AtomicInteger(0);

        serializer = new IntegerSerializer();
    }

    @Override
    public void setContext(EncodingContext context) {
        if (this.context != null && this.context.getStatus() == EncodingStatus.RUNNING) {
            System.err.println("Current encoding process is still running.");
            return;
        }

        if (context == null)
            throw new IllegalArgumentException("Context cannot be null");

        if (info.isStateful() && context.getStorageEngine() == null)
            throw new IllegalArgumentException("Encoder is stateful, yet no storage engine was provided");

        this.context = context;
        Map<String, Object> resolvedParams = resolveParametersFiltered();

        predicateCounter.set((Integer) resolvedParams.getOrDefault("predicateStartOffset", 0));
        entityCounter.set((Integer) resolvedParams.getOrDefault("entityStartOffset", 0));
    }

    @Override
    public EncodingContext getContext() {
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

        TripleComponent type = data.getType();
        String prefix = type == TripleComponent.PREDICATE ? "PREDICATE" : "ENTITY";
        String key = prefix + "::" + data.getValue();

        try {
            byte[] raw = context.getStorageEngine().get(key);

            if (raw != null) {
                Integer existingCode = serializer.deserialize(raw);
                return existingCode;
            }

            Integer newCode = (type == TripleComponent.PREDICATE)
                    ? predicateCounter.getAndIncrement()
                    : entityCounter.getAndIncrement();

            context.getStorageEngine().put(key, serializer.serialize(newCode));
            return newCode;
        } catch (StorageException e) {
            throw new RuntimeException("Failed to access storage engine", e);
        }
    }

    @Override
    public String getFinalEncoding(EncodingData data) {

        if (context.getStatus() != EncodingStatus.DONE) {
            System.err.println("The encoding process is not finalized yet");
            return null;
        }

        Integer encodedData = encode(data);
        TripleComponent type = data.getType();
        int length = (type == TripleComponent.PREDICATE)
                ? predicateCounter.get()
                : entityCounter.get();

        String binaryString = Integer.toBinaryString(encodedData);
        return String.format("%" + length + "s", binaryString).replace(' ', '0');
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
}
