package com.csd.core.split;

import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.LinkedHashMap;

import com.csd.common.params.ParameterDescriptor;
import com.csd.common.type.TypeRef;

/**
 * Describes a splitter node:
 * - Its input type
 * - One or more output types (e.g., per branch)
 * - Whether a delegate is required
 * - Its parameter schema
 *
 * The class is immutable and preserves insertion order for maps.
 */
public abstract class SplitterDescriptor {
    private final String  id;
    private final TypeRef inputType;
    private final TypeRef outputType; // TypeRef of output type of branches. Usually is a bound arg
    private final boolean delegateRequired;
    private final Map<String, ParameterDescriptor> params; // parameter schema

    /**
     * Creates a splitter with multiple outputs (e.g., branch-labeled).
     */
    public SplitterDescriptor(
            String  id,
            TypeRef inputType,
            TypeRef outputType,
            boolean delegateRequired,
            List<ParameterDescriptor> params
    ) {
        this.id         = Objects.requireNonNull(id, "id");
        this.inputType  = Objects.requireNonNull(inputType, "inputType");
        this.outputType = Objects.requireNonNull(outputType, "outputType");
        this.delegateRequired = delegateRequired;

        Objects.requireNonNull(params, "params");
        Map<String, ParameterDescriptor> pmap = new LinkedHashMap<>();
        for (ParameterDescriptor p : params) {
            Objects.requireNonNull(p, "parameter");
            pmap.put(p.getName(), p);
        }
        this.params = Collections.unmodifiableMap(pmap);
    }

    public String getId() {
        return id;
    }

    public TypeRef getInputType() {
        return inputType;
    }

    public TypeRef getOutputType() {
        return outputType;
    }

    /**
     * True if a delegate must be supplied for this splitter at runtime.
     */
    public boolean isDelegateRequired() {
        return delegateRequired;
    }

    /**
     * Parameter schema for configuring the splitter.
     */
    public Map<String, ParameterDescriptor> getParams() {
        return params;
    }

    // TODO(gtheo): Decouple it 
    abstract public TypeRef projectTypeOutput(TypeRef passedInput, String param);
}
