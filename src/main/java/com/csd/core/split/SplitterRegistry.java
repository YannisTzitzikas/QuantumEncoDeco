package com.csd.core.split;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

public final class SplitterRegistry {
    private final Map<String, SplitterDescriptor> descriptors = new HashMap<>();

    public void register(SplitterDescriptor descriptor) {
        descriptors.put(descriptor.getId(), descriptor);
    }

    public Optional<SplitterDescriptor> getDescriptor(String id) {
        return Optional.ofNullable(descriptors.get(id));
    }

    public void discoverViaSPI() {
        ServiceLoader.load(SplitterDescriptor.class).forEach(this::register);
    }
}
