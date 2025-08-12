package com.csd.common.utils.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseMapper {

    protected String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : null;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof Map ? (Map<String, Object>) v : Collections.emptyMap();
    }

    
    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> getList(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof List ? (List<Map<String, Object>>) v : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> getListOfMaps(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (!(v instanceof List)) return Collections.emptyList();
        List<?> rawList = (List<?>) v;
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : rawList) {
            if (o instanceof Map) out.add((Map<String, Object>) o);
        }
        return out;
    }

    protected Map<String, String> getStringStringMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (!(v instanceof Map)) return Collections.emptyMap();
        Map<?, ?> m = (Map<?, ?>) v;
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : m.entrySet()) {
            if (e.getKey() instanceof String && e.getValue() instanceof String) {
                out.put((String) e.getKey(), (String) e.getValue());
            }
        }
        return out;
    }

    protected Map<String, String> coalescePortMappings(Map<String, Object> raw, String preferredKey, String altKey) {
        Map<String, String> preferred = getStringStringMap(raw, preferredKey);
        if (!preferred.isEmpty()) return preferred;
        return getStringStringMap(raw, altKey);
    }

    protected String nullIfBlank(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    protected Integer getInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try {
                return Integer.parseInt((String) v);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
