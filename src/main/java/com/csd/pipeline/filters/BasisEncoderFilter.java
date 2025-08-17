package com.csd.pipeline.filters;

import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.pipeline.AbstractFilter;

import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.pipeline.StreamPolicy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BasisEncoderFilter extends AbstractFilter {

    public static final InputPort<Iterable<TripleComponent>> IN =
        new InputPort<>("components");
    public static final OutputPort<Map<String,Integer>> OUT =
        new OutputPort<>("basis-mapping");

    int count = 0;

    public BasisEncoderFilter(PortBindings bindings) {
        super(Arrays.asList(IN), Arrays.asList(OUT), bindings, new StreamPolicy());
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        Message<Iterable<TripleComponent>> msg = in.pop(IN);
        if (msg.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.eos());
            return;
        }

        Map<String, Integer> map = new HashMap<>();

        for (TripleComponent component : msg.getPayload()) {
            map.putIfAbsent(component.getValue(), count);
        }

        out.emit(OUT, Message.data(map));
    }
}
