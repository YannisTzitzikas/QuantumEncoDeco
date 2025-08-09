package com.csd.stage;

import java.util.HashMap;
import java.util.Map;

public final class StageCatalog {
    private final Map<String, StageProfile> profiles = new HashMap<>();
    private final Map<String, StageFactory> factories = new HashMap<>();

    public void register(StageProfile profile, StageFactory factory) {
        profiles.put(profile.getStageId(), profile);
        factories.put(profile.getStageId(), factory);
    }

    public StageProfile getProfile(String type) { return profiles.get(type); }
    public StageFactory getFactory(String type) { return factories.get(type); }
}