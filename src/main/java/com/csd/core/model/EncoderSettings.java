package com.csd.core.model;

import java.util.Map;
import java.util.HashMap;

public class EncoderSettings {
    private boolean stateful;
    private Map<String, Object> params;

    public EncoderSettings(boolean stateful, Map<String, Object> params) {
        this.stateful  = stateful;
        this.params    = params != null ? params : new HashMap<>();
    }

    public boolean isStateful()             { return stateful; }
    public Map<String, Object> getParams()  { return params; }

    public Object getParam(String key) {
        return params.get(key);
    }
}
