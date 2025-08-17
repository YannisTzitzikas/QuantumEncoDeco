package com.csd.pipeline.filters;

import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.model.uri.URITriple;
import com.csd.core.pipeline.AbstractFilter;

import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.pipeline.StreamPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TripleComponentExtractorFilter extends AbstractFilter {

    public static final InputPort<List<URITriple>> IN =
        new InputPort<>("triples");
    public static final OutputPort<List<TripleComponent>> OUT =
        new OutputPort<>("components");


    public TripleComponentExtractorFilter(PortBindings bindings) {
        super(Arrays.asList(IN), Arrays.asList(OUT), bindings, new StreamPolicy());
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        Message<List<URITriple>> msg = in.pop(IN);
        if (msg.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.eos());
            return;
        }

        List<TripleComponent> components = new ArrayList<>();
        for (URITriple t : msg.getPayload()) {
            components.add(t.getPredicate());
            components.add(t.getSubject());
            components.add(t.getObject());
        }

        out.emit(OUT, Message.data(components));
    }
}
