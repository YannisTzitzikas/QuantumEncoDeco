package com.csd.config;

import java.util.Map;
import com.csd.common.utils.mapper.BaseMapper;

final class EdgeConfigMapper extends BaseMapper {

    public EdgeConfig map(Map<String, Object> raw) {
        String id = getString(raw, "id"); // optional
        String to = getString(raw, "to");
        
        String   rawFrom   = getString(raw, "from");
        String[] fromParts = rawFrom.split("\\.");

        String from = fromParts[0];
        String port = (fromParts[1] == null || fromParts[1].isEmpty()) ? "out" : fromParts[1];
        
        return new EdgeConfig(id, from, to, port);
    }
}
