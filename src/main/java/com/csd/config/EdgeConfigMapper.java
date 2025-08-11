package com.csd.config;

import java.util.Map;

final class EdgeConfigMapper {

    public EdgeConfig map(Map<String, Object> raw) {
        String id = getString(raw, "id"); // optional
        String from = getString(raw, "from");
        String to = getString(raw, "to");

        return new EdgeConfig(id, from, to);
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : null;
    }
}
