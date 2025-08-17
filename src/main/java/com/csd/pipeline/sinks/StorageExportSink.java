package com.csd.pipeline.sinks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.csd.common.utils.serializer.IntegerSerializer;
import com.csd.common.utils.serializer.StringSerializer;
import com.csd.core.pipeline.AbstractSink;
import com.csd.core.pipeline.BarrierPolicy;

import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.storage.StorageEngine;

public final class StorageExportSink extends AbstractSink {
    
    public static final InputPort<Void> IN =
        new InputPort<>("void");

    private final StorageEngine     storage;
    private final StringSerializer  stringSer  = new StringSerializer();
    private final IntegerSerializer intSer     = new IntegerSerializer();

    public StorageExportSink(PortBindings bindings, StorageEngine storage) {
        super(Arrays.asList(IN), bindings, new BarrierPolicy());
        if (storage == null) throw new IllegalArgumentException("StorageEngine cannot be null");
        this.storage = storage;
    }


  @Override
    protected void drain(Batch in, Emitter out) throws Exception {
        in.pop(IN);
        try (// Write to stdout; flush but do NOT close System.out.
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("map.test"), StandardCharsets.UTF_8))) {
            // Stream through the storage, decode, and write "String Number"
            storage.entries().forEach(entry  -> {
                final String key = stringSer.deserialize(entry.getKey());
                final Integer val = intSer.deserialize(entry.getValue());
                try {
                    writer.write(key);
                    writer.write(' ');
                    writer.write(Integer.toString(val));
                    writer.newLine();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            writer.flush();
        }
    }
}
