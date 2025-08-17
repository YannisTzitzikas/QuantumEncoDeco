package com.csd.core.pipeline;

import java.util.Collections;
import java.util.List;

// A sink is basically a filter which does not have output pipes
public abstract class AbstractSink extends AbstractFilter {

    protected AbstractSink(List<InputPort<?>> inputs,
                           PortBindings bindings,
                           CoordinationPolicy policy) {
        super(inputs, Collections.emptyList(), bindings, policy);
    }
}
