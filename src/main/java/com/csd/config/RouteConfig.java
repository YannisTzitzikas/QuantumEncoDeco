package com.csd.config;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * RouteConfig defines:
 * - The stage to run
 * - The optional splitter (which implicitly defines ports)
 * 
 * If no splitter is present, a single "out" port is assumed.
 */
public final class RouteConfig {

    public static final String DEFAULT_OUT_PORT = "out";

    private final StageConfig stageConf;
    private final SplitterConfig splitterConf;  // nullable

    public RouteConfig(StageConfig stageConf, SplitterConfig splitterConf) {
        this.stageConf = Objects.requireNonNull(stageConf, "stageConf");
        this.splitterConf = splitterConf;
    }

    public StageConfig getStageConf() {
        return stageConf;
    }

    public SplitterConfig getSplitterConf() {
        return splitterConf;
    }

    /**
     * Returns port names as implied by the splitter.
     * If no splitter is configured, returns single "out" port.
     */
    public Map<String, String> getEffectivePorts() {
        if (splitterConf == null || splitterConf.getPortMappings().isEmpty()) {
            return Collections.singletonMap(DEFAULT_OUT_PORT, null);
        }
        return splitterConf.getPortMappings();
    }

    public boolean usesDefaultOutPort() {
        return splitterConf == null || splitterConf.getPortMappings().isEmpty();
    }

    @Override
    public String toString() {
        return "\nRouteConfig{" +
               "\nstageConf=" + stageConf +
               "\n, splitterConf=" + splitterConf +
               "\n, ports=" + getEffectivePorts().keySet() +
               "\n}";
    }
}
