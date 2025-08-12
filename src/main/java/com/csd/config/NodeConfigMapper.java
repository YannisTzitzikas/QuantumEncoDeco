package com.csd.config;

import java.util.Map;

import com.csd.common.utils.mapper.BaseMapper;

final class NodeConfigMapper extends BaseMapper {

    public NodeConfig map(Map<String, Object> rawNodeMap) {

        RouteConfigMapper routeMapper = new RouteConfigMapper();

        if (rawNodeMap == null || rawNodeMap.isEmpty()) {
            throw new IllegalArgumentException("NodeConfig must include a valid Map.");
        }

        // Extract stageConf and splitterConf from routeConf map
        Map<String, Object> stageConfMap = getMap(rawNodeMap, "stage");
        Map<String, Object> splitterConfMap = getMap(rawNodeMap, "splitter");

        String      name      = getString(rawNodeMap,"name");
        RouteConfig routeConf = routeMapper.map(stageConfMap, splitterConfMap);

        return new NodeConfig(name, routeConf);
    }
}
