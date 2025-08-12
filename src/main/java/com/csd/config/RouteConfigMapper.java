package com.csd.config;

import java.util.Map;

final class RouteConfigMapper {

    public RouteConfig map(Map<String, Object> rawStage,
                           Map<String, Object> rawSplitter) 
    {
        StageConfigMapper stageMapper = new StageConfigMapper();
        SplitterConfigMapper splitterMapper = new SplitterConfigMapper();
        
        if (rawStage == null || rawStage.isEmpty()) {
            throw new IllegalArgumentException("RouteConfig must include a valid stageConf.");
        }

        StageConfig stageConf = stageMapper.map(rawStage);
        SplitterConfig splitterConf = (rawSplitter != null && !rawSplitter.isEmpty())
            ? splitterMapper.map(rawSplitter)
            : null;

        return new RouteConfig(stageConf, splitterConf);
    }
}
