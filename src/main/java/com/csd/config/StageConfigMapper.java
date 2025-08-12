package com.csd.config;

import java.util.Map;

import com.csd.common.utils.mapper.BaseMapper;

final class StageConfigMapper extends BaseMapper {

    public StageConfig map(Map<String, Object> raw) {
        String stageId = getString(raw, "id");

        Map<String, Object> params = getMap(raw, "params");
        return new StageConfig(stageId, params);
    }
}
