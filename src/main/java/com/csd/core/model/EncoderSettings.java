package com.csd.core.model;

import java.util.Map;
import java.util.HashMap;

public class EncoderSettings {
    private boolean stateful;
    private boolean duplicate;
    private Map<String, Object> params;

    public EncoderSettings(boolean stateful, boolean duplicate, Map<String, Object> params) {
        this.stateful  = stateful;
        this.duplicate = duplicate;
        this.params    = params != null ? params : new HashMap<>();
    }

    public boolean isStateful()             { return stateful; }
    public boolean supportsDuplicates()     { return duplicate; }
    public Map<String, Object> getParams()  { return params; }

    public Object getParam(String key) {
        return params.get(key);
    }
}
