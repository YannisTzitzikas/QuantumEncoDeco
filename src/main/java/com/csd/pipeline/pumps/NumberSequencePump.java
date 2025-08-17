package com.csd.pipeline.pumps;

import com.csd.core.model.Message;
import com.csd.core.pipeline.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class NumberSequencePump extends AbstractPump {

    public static final OutputPort<List<Number>> OUT =
        new OutputPort<>("numbers");

    private int next;
    private final int endInclusive;
    private final int batchSize;

    public NumberSequencePump(int startInclusive,
                              int endInclusive,
                              int batchSize,
                              PortBindings bindings) {
        super(Arrays.asList(OUT), bindings, 0); // 0ms -> yield when idle
        this.next = startInclusive;
        this.endInclusive = endInclusive;
        this.batchSize = Math.max(1, batchSize);
    }

    @Override
    protected boolean step(Emitter out) throws Exception {
        if (next > endInclusive) {
            stop();            // done -> trigger EOS in loop exit
            return false;      // idle so loop can back off
        }

        List<Number> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize && next <= endInclusive; i++, next++) {
            batch.add(next);
        }

        out.emit(OUT, Message.data(batch));
        return true;
    }
}
