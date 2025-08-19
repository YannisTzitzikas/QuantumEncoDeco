package com.csd.core.pipeline;

import java.util.Collections;
import java.util.List;

import com.csd.core.event.EventBus;

// A sink is basically a filter which does not have output pipes
public abstract class AbstractSink extends AbstractFilter {

    protected AbstractSink(List<InputPort<?>> inputs,
                           PortBindings bindings,
                           CoordinationPolicy policy,
                           EventBus eventBus) {
        super(inputs, Collections.emptyList(), bindings, policy, eventBus);
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        drain(in, out);
        onStop();
    }

    abstract protected void drain(Batch in, Emitter out) throws Exception;
}
