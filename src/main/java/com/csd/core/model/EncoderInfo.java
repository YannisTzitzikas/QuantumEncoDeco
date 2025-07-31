package com.csd.core.model;

import java.util.Map;

public class EncoderInfo {
    private final boolean stateful;
    private final boolean twoPass;
    private final String name;
    private final Map<String, Object> defaultParameters;

    public EncoderInfo(String name, boolean stateful, boolean twoPass, Map<String, Object> defaults) {
        this.name = name;
        this.stateful = stateful;
        this.twoPass = twoPass;
        this.defaultParameters = defaults;
    }

    public boolean isStateful() { return stateful; }
    public boolean isTwoPass() { return twoPass; }
    public String getName() { return name; }

    public Map<String, Object> getDefaultParameters() {return defaultParameters; }
}

