package com.csd.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Declarative splitter configuration.
 *
 * Rules:
 * - Must include at least one port mapping.
 * 
 * Examples:
 * type = "BeanFieldsSplitStrategy", portMappings = {"subjects":"subject"}
 * type = "ListAwareSplitter", delegate = { ... BeanFieldsSplitStrategy ... }
 */
public final class SplitterConfig {

    private final String type;                        // strategy class name
    private final SplitterConfig delegate;            // optional nested splitter
    private       Map<String, String> portMappings;   // portId → path/expression
    private final Map<String, Object> params;         // optional strategy params

    public SplitterConfig(String type,
                          SplitterConfig delegate,
                          Map<String, String> portMappings,
                          Map<String, Object> params) {
        this.type = Objects.requireNonNull(type, "type");
        this.delegate = delegate;

        if (portMappings == null || portMappings.isEmpty()) {
            throw new IllegalArgumentException("SplitterConfig must include at least one port mapping.");
        }

        this.portMappings = new LinkedHashMap<>(portMappings);
        this.params = params == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(params));
    }

    public String getType() {
        return type;
    }

    public SplitterConfig getDelegate() {
        return delegate;
    }

    public Map<String, String> getPortMappings() {
        return portMappings;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "SplitterConfig{" +
               "type='" + type + '\'' +
               (delegate != null ? ", delegate=" + delegate.type : "") +
               ", portMappings=" + portMappings.keySet() +
               (params.isEmpty() ? "" : ", params=" + params.keySet()) +
               '}';
    }
}
