package com.csd.core.stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

public final class StageRegistry {
    private final Map<String, StageDescriptor> descriptors = new HashMap<>();

    public void register(StageDescriptor p) {
        descriptors.putIfAbsent(p.getStageId(), p);
    }

    public Optional<StageDescriptor> getDescriptor(String id) {
        return Optional.ofNullable(descriptors.get(id));
    }

    public static StageRegistry withBuiltins(StageDescriptor... builtins) {
        StageRegistry c = new StageRegistry();
        for (StageDescriptor p : builtins) c.register(p);
        return c;
    }

    public void discoverViaSPI() {
        ServiceLoader.load(StageDescriptor.class).forEach(this::register);
    }
}
