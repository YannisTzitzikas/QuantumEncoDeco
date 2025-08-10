package com.csd.config;

import java.util.List;

public final class JobConfig {
    private final String name;
    private final String description;
    private final List<NodeConfig> nodes; // DAG execution model
    private final List<EdgeConfig> edges; // connections

    public JobConfig(String name, String description, List<NodeConfig> nodes, List<EdgeConfig> edges) {
        this.name = name;
        this.description = description;
        this.nodes = nodes;
        this.edges = edges;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<NodeConfig> getNodes() {
        return nodes;
    }

    public List<EdgeConfig> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return "JobConfig{name='" + name + "', description='" + description + "', nodes=" + nodes + ", edges=" + edges + "}";
    }
}
