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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BasisEncoderFilter extends AbstractFilter {

    public static final InputPort<Set<TripleComponent>> IN =
        new InputPort<>("components");
    public static final OutputPort<Map<String,Integer>> OUT =
        new OutputPort<>("basis-mapping");

    AtomicInteger count = new AtomicInteger(0);

    public BasisEncoderFilter(PortBindings bindings) {
        super(Arrays.asList(IN), Arrays.asList(OUT), bindings, new StreamPolicy());
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        Message<Set<TripleComponent>> msg = in.pop(IN);
        if (msg.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.eos());
            return;
        }

        Map<String, Integer> map = new HashMap<>();

        for (TripleComponent component : msg.getPayload()) {
            map.computeIfAbsent(component.getValue(), k -> count.getAndIncrement());
        }

        logger.info("Count at end of batch equals {}", count.get());
        out.emit(OUT, Message.data(map));
    }
}
