package com.csd.pipeline.filters;

import com.csd.core.event.EventBus;
import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.pipeline.AbstractFilter;
import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.pipeline.StreamPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SplitPredicatesAndEntitiesFilter extends AbstractFilter {
    
    public static final InputPort<Iterable<TripleComponent>> IN =
        new InputPort<>("components");
    public static final OutputPort<List<TripleComponent>> OUT_PREDICATES =
        new OutputPort<>("predicates");
    public static final OutputPort<List<TripleComponent>> OUT_ENTITIES   = 
        new OutputPort<>("entities");

    public SplitPredicatesAndEntitiesFilter(PortBindings bindings, EventBus bus) {
        super(Arrays.asList(IN), Arrays.asList(OUT_PREDICATES, OUT_ENTITIES), bindings, new StreamPolicy(), bus);
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        Message<Iterable<TripleComponent>> msg = in.pop(IN);
        if (msg.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT_PREDICATES, Message.eos());
            out.emit(OUT_ENTITIES, Message.eos());
            return;
        }

        List<TripleComponent> entities   = new ArrayList<>();
        List<TripleComponent> predicates = new ArrayList<>();
        for (TripleComponent t : msg.getPayload()) {
            switch (t.getRole()) {
                case PREDICATE: predicates.add(t); break;
                default:        entities.add(t);
            }
        }

        out.emit(OUT_ENTITIES, Message.data(entities));
        out.emit(OUT_PREDICATES, Message.data(predicates));
    }
}
