package com.csd.pipeline.filters;

import com.csd.core.event.EventBus;
import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.model.uri.URITriple;
import com.csd.core.pipeline.AbstractFilter;

import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.pipeline.StreamPolicy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TripleComponentExtractorFilter extends AbstractFilter {

    public static final InputPort<List<URITriple>> IN =
        new InputPort<>("triples");
    public static final OutputPort<Set<TripleComponent>> OUT =
        new OutputPort<>("components");


    public TripleComponentExtractorFilter(PortBindings bindings, EventBus bus) {
        super(Arrays.asList(IN), Arrays.asList(OUT), bindings, new StreamPolicy(), bus);
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        Message<List<URITriple>> msg = in.pop(IN);
        if (msg.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.eos());
            return;
        }

        Set<TripleComponent> components = new HashSet<>();
        for (URITriple t : msg.getPayload()) {
            components.add(t.getPredicate());
            components.add(t.getSubject());
            components.add(t.getObject());
        }

        out.emit(OUT, Message.data(components));
    }
}
