package com.csd.config;

import java.util.*;

public final class GraphConfigMapper {

    public static GraphConfig map(Map<String, Object> raw) {
        String name = getString(raw, "name");
        String description = getString(raw, "description");

        List<Map<String, Object>> rawNodes = getList(raw, "nodes");
        List<Map<String, Object>> rawEdges = getList(raw, "edges");

        List<NodeConfig> nodes = new ArrayList<>();
        NodeConfigMapper nodeMapper = new NodeConfigMapper();
        for (Map<String, Object> nodeMap : rawNodes) {
            nodes.add(nodeMapper.map(nodeMap));
        }

        List<EdgeConfig> edges = new ArrayList<>();
        EdgeConfigMapper edgeMapper = new EdgeConfigMapper();
        for (Map<String, Object> edgeMap : rawEdges) {
            edges.add(edgeMapper.map(edgeMap));
        }

        return new GraphConfig(name, description, nodes, edges);
    }

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getList(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof List ? (List<Map<String, Object>>) v : Collections.emptyList();
    }
}
