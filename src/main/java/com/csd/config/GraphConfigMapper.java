package com.csd.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csd.common.utils.mapper.BaseMapper;

public final class GraphConfigMapper extends BaseMapper {

    public GraphConfig map(Map<String, Object> raw) {

        NodeConfigMapper nodeMapper = new NodeConfigMapper();
        EdgeConfigMapper edgeMapper = new EdgeConfigMapper();

        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("GraphConfig must not be null or empty.");
        }

        String name = getString(raw, "name");
        String description = getString(raw, "description");

        List<NodeConfig> nodes = new ArrayList<>();
        List<Map<String, Object>> rawNodes = getListOfMaps(raw, "nodes");
        for (Map<String, Object> nodeMap : rawNodes) {
            nodes.add(nodeMapper.map(nodeMap));
        }

        List<EdgeConfig> edges = new ArrayList<>();
        List<Map<String, Object>> rawEdges = getListOfMaps(raw, "edges");
        for (Map<String, Object> edgeMap : rawEdges) {
            edges.add(edgeMapper.map(edgeMap));
        }

        return new GraphConfig(name, description, nodes, edges);
    }
}
