package com.csd.config;

import java.util.Collections;
import java.util.Map;

final class StageConfigMapper {

    public StageConfig map(Map<String, Object> raw) {
        String stageId = getString(raw, "stageId");

        Map<String, Object> params = getMap(raw, "params");
        return new StageConfig(stageId, params);
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof Map ? (Map<String, Object>) v : Collections.emptyMap();
    }
}
