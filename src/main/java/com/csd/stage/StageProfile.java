package com.csd.stage;

import java.util.*;

public final class StageProfile {
    private final String stageId; // global stage id
    private final Class<?> inputType;
    private final Class<?> outputType;
    private final Map<String, ParameterDescriptor> params; // schema

    public StageProfile(String StageId, Class<?> inputType, Class<?> outputType,
                     List<ParameterDescriptor> params) {
        this.stageId = StageId;
        this.inputType = inputType;
        this.outputType = outputType;

        Map<String, ParameterDescriptor> m = new LinkedHashMap<>();
        for (ParameterDescriptor p : params) {
            m.put(p.getName(), p);
        }
        this.params = Collections.unmodifiableMap(m);
    }

    public String getStageId() {
        return stageId;
    }

    public Class<?> getInputType() {
        return inputType;
    }

    public Class<?> getOutputType() {
        return outputType;
    }

    public Map<String, ParameterDescriptor> getParams() {
        return params;
    }
}
