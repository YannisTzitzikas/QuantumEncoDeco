package com.csd.pipeline.filters;

import com.csd.common.utils.serializer.IntegerSerializer;
import com.csd.common.utils.serializer.StringSerializer;
import com.csd.core.model.Message;
import com.csd.core.pipeline.AbstractFilter;
import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.pipeline.StreamPolicy;
import com.csd.core.storage.StorageEngine;
import com.csd.core.storage.StorageException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Consumes a Map<String, Integer>, stores all entries into the StorageEngine, and emits Void.
 * Keys and values are serialized as UTF-8 strings (value uses Integer.toString()).
 */
public class MapStoreFilterVoid extends AbstractFilter {

    public static final InputPort<Map<String, Integer>> IN =
        new InputPort<>("entries");

    public static final OutputPort<Void> OUT =
        new OutputPort<>("done");

    private final StorageEngine storage;
    private final StringSerializer  stringSer  = new StringSerializer();
    private final IntegerSerializer intSer     = new IntegerSerializer();

    public MapStoreFilterVoid(PortBindings bindings, StorageEngine storage) {
        super(Arrays.asList(IN), Arrays.asList(OUT), bindings, new StreamPolicy());
        if (storage == null) throw new IllegalArgumentException("StorageEngine cannot be null");
        this.storage = storage;
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        final long start = System.nanoTime();
        Message<Map<String, Integer>> msg = in.pop(IN);

        if (msg.getKind() == Message.MessageKind.EOS) {
            logger.debug("MapStoreFilterVoid received EOS; forwarding.");
            out.emit(OUT, Message.eos());
            return;
        }

        Map<String, Integer> payload = msg.getPayload();
        int inputCount = (payload == null) ? 0 : payload.size();

        if (payload == null || payload.isEmpty()) {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            logger.info("MapStoreFilterVoid empty batch: input=0, inserted=0, durationMs={}", elapsed);
            out.emit(OUT, Message.data(null));
            return;
        }

        Map<byte[], byte[]> toPut = new HashMap<>(payload.size());
        for (Map.Entry<String, Integer> e : payload.entrySet()) {
            byte[] k = stringSer.serialize(e.getKey());
            byte[] v = intSer.serialize(e.getValue());
            toPut.put(k, v);
        }

        int inserted = 0;
        try {
            storage.putAll(toPut);
            inserted = toPut.size();
        } catch (StorageException ex) {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            logger.error("MapStoreFilterVoid storage putAll failed after {} ms (input={}): {}",
                    elapsed, inputCount, ex.getMessage(), ex);
            throw ex;
        }

        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        logger.info("MapStoreFilterVoid processed batch: input={}, inserted={}, durationMs={}",
                inputCount, inserted, elapsed);

        out.emit(OUT, Message.data(null));
    }
}
