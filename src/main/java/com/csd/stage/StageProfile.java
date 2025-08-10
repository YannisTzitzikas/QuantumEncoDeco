package com.csd.stage;

import java.util.*;

import com.csd.common.type.TypeRef;

public final class StageProfile {
    private final String stageId; // global stage id
    private final TypeRef inputType;
    private final TypeRef outputType;
    private final Map<String, ParameterDescriptor> params; // schema

    public StageProfile(String StageId, TypeRef inputType, TypeRef outputType,
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

    public TypeRef getInputType() {
        return inputType;
    }

    public TypeRef getOutputType() {
        return outputType;
    }

    public Map<String, ParameterDescriptor> getParams() {
        return params;
    }
}
