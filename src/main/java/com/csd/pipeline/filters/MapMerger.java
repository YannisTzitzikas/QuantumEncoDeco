package com.csd.pipeline.filters;

import com.csd.core.model.Message;
import com.csd.core.pipeline.AbstractFilter;
import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.JoinPolicy;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MapMerger extends AbstractFilter {

    public static final InputPort<Map<String, Integer>> IN_FIRST =
        new InputPort<>("first");
    public static final InputPort<Map<String, Integer>> IN_SECOND =
        new InputPort<>("second");
    public static final OutputPort<Map<String, Integer>> OUT =
        new OutputPort<>("merged");

    protected MapMerger(PortBindings bindings) {
        super(Arrays.asList(IN_FIRST, IN_SECOND), Arrays.asList(OUT), bindings, new JoinPolicy());
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {

        Message<Map<String, Integer>> left  = in.pop(IN_FIRST);
        Message<Map<String, Integer>> right = in.pop(IN_SECOND);
    
        if (left.getKind() == Message.MessageKind.EOS && right.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.eos());
            return;
        }
    
        if (left.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.data(right.getPayload()));
            return;
        }
    
        if (right.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.data(left.getPayload()));
            return;
        }

        Map<String, Integer> merged = new HashMap<>(Math.max(16, left.getPayload().size() + right.getPayload().size()));
        merged.putAll(left.getPayload());
        merged.putAll(right.getPayload());
    
        out.emit(OUT, Message.data(merged));
        return;
    }
}