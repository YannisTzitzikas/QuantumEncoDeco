package com.csd.config;

import java.util.HashMap;
import java.util.Map;

public final class StageConfig {
    private final String stageId;        // the id of the stage it's about
    private final Map<String, Object> params;

    public StageConfig(String stageId, Map<String, Object> params) {
        this.stageId = stageId;
        this.params = params != null ? params : new HashMap<>();
    }

    public String getStageId() {
        return stageId;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
