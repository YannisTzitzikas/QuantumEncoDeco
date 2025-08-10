package com.csd.stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import com.csd.stage.provider.StageProvider;

public final class StageRegistry {
    private final Map<String, StageProvider> providers = new HashMap<>();

    public void register(StageProvider p) {
        StageProvider prev = providers.putIfAbsent(p.id(), p);
        if (prev != null) throw new IllegalArgumentException("Duplicate stage id: " + p.id());
    }

    public Optional<StageProvider> get(String id) {
        return Optional.ofNullable(providers.get(id));
    }

    public static StageRegistry withBuiltins(StageProvider... builtins) {
        StageRegistry c = new StageRegistry();
        for (StageProvider p : builtins) c.register(p);
        return c;
    }

    public void discoverViaSPI() {
        ServiceLoader.load(StageProvider.class).forEach(this::register);
    }
}
