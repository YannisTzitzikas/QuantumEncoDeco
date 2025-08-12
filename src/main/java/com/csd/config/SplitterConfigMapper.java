package com.csd.config;

import java.util.Map;

import com.csd.common.utils.mapper.BaseMapper;

public final class SplitterConfigMapper extends BaseMapper {

    public SplitterConfig map(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) return null;

        String type = getString(raw, "type");
        Map<String, Object> params = getMap(raw, "params");

        // Delegate (recursive)
        SplitterConfig delegate = null;
        Map<String, Object> rawDelegate = getMap(raw, "delegate");
        if (!rawDelegate.isEmpty()) {
            delegate = map(rawDelegate);
        }

        // Support both "portMappings" and the typo "protMappings"
        Map<String, String> portMappings = getStringStringMap(raw, "portMappings");
        if (portMappings.isEmpty()) {
            portMappings = getStringStringMap(raw, "portMappings");
        }

        // SplitterConfig constructor enforces non-empty port mappings
        return new SplitterConfig(type, delegate, portMappings, params);
    }
}
