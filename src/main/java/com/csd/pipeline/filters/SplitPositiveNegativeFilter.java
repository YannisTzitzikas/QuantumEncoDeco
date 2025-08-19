package com.csd.pipeline.filters;

import com.csd.core.event.EventBus;
import com.csd.core.model.Message;
import com.csd.core.pipeline.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SplitPositiveNegativeFilter extends AbstractFilter {

    public static final InputPort<List<Number>> IN =
        new InputPort<>("numbers");
    public static final OutputPort<List<Number>> OUT_POS =
        new OutputPort<>("positives");
    public static final OutputPort<List<Number>> OUT_NEG =
        new OutputPort<>("negatives");

    public SplitPositiveNegativeFilter(PortBindings bindings, EventBus bus) {
        super(Arrays.asList(IN), Arrays.asList(OUT_POS, OUT_NEG), bindings, new StreamPolicy(), bus);
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        Message<List<Number>> msg = in.pop(IN);

        if (msg.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT_POS, Message.eos());
            out.emit(OUT_NEG, Message.eos());
            onStop();
            return;
        }

        List<Number> positives = new ArrayList<>();
        List<Number> negatives = new ArrayList<>();

        for (Number n : msg.getPayload()) {
            if (n == null) continue;
            double val = n.doubleValue();
            if (val > 0) positives.add(n);
            else if (val < 0) negatives.add(n);
        }

        if (!positives.isEmpty()) out.emit(OUT_POS, Message.data(positives));
        if (!negatives.isEmpty()) out.emit(OUT_NEG, Message.data(negatives));
    }
}
