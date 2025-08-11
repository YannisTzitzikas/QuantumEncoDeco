package com.csd.config;

import java.util.List;

public final class GraphConfig {
    private final String name;
    private final String description;
    private final List<NodeConfig> nodes; // DAG execution model
    private final List<EdgeConfig> edges; // connections

    public GraphConfig(String name, String description, List<NodeConfig> nodes, List<EdgeConfig> edges) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("GraphConfig {\n");
        sb.append("  Name: ").append(name).append("\n");
        sb.append("  Description: ").append(description).append("\n");
    
        sb.append("  Nodes:\n");
        if (nodes != null && !nodes.isEmpty()) {
            for (NodeConfig node : nodes) {
                sb.append("    - ").append(node).append("\n");
            }
        } else {
            sb.append("    (none)\n");
        }
    
        sb.append("  Edges:\n");
        if (edges != null && !edges.isEmpty()) {
            for (EdgeConfig edge : edges) {
                sb.append("    - ").append(edge).append("\n");
            }
        } else {
            sb.append("    (none)\n");
        }
    
        sb.append("}");
        return sb.toString();
    }

}
