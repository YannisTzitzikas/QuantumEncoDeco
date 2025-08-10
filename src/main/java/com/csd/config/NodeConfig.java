package com.csd.config;

public final class NodeConfig {
    private final String name;           // unique graph name
    private final StageConfig stageConf; // configuration for the stage

    public NodeConfig(String name, StageConfig stageConf) {
        this.name = name;
        this.stageConf = stageConf;
    }

    public String getName() {
        return name;
    }

    public StageConfig getStageConf() {
        return stageConf;
    }

    @Override
    public String toString() {
        return "NodeConfig{name='" + name + "', stageConf=" + stageConf + "}";
    }
}
