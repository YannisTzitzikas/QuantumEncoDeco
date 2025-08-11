package com.csd.config;

import java.util.Map;

final class NodeConfigMapper {

    public NodeConfig map(Map<String, Object> raw) {
        String name = getString(raw, "name");
        String stageId = getString(raw, "id");
        Map<String, Object> params = getMap(raw, "params");

        StageConfig stageConf = new StageConfig(stageId, params);
        return new NodeConfig(name, stageConf);
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof Map ? (Map<String, Object>) v : null;
    }
}
