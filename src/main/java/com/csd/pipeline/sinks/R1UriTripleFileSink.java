package com.csd.pipeline.sinks;

import com.csd.common.io.FileIterator;
import com.csd.common.utils.serializer.IntegerSerializer;
import com.csd.common.utils.serializer.StringSerializer;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.model.uri.URITriple;
import com.csd.core.pipeline.AbstractSink;
import com.csd.core.pipeline.BarrierPolicy;
import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.storage.StorageEngine;
import com.csd.core.storage.StorageException;
import com.csd.events.UniqueEntityEvent;
import com.csd.events.UniquePredicateEvent;
import com.csd.io.URIStreamerFactory;
import com.csd.core.event.EventBus;
import com.csd.core.io.URIStreamer;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Terminal sink that encodes triples into fixed-width bitstrings and
 * writes them as one concatenated bitstring per line to an output file.
 */
public final class R1UriTripleFileSink extends AbstractSink implements Closeable {

    // Trigger input; payload unused (Void)
    public static final InputPort<Void> IN = new InputPort<>("start");

    private final FileIterator fileIterator;
    private final StorageEngine storage;
    private final BufferedWriter writer;
    private final StringSerializer strSer = new StringSerializer();
    private final IntegerSerializer intSer = new IntegerSerializer();
    private       int bitsPerComponent;
    private final AtomicInteger uniqueComponents = new AtomicInteger(0);

    public R1UriTripleFileSink(Path inputDir,
                             String globPattern,
                             PortBindings bindings,
                             StorageEngine storage,
                             Path outFile,
                             EventBus bus) throws IOException {
        super(Arrays.asList(IN), bindings, new BarrierPolicy(), bus);
        if (storage == null) throw new IllegalArgumentException("StorageEngine cannot be null");
        if (outFile == null) throw new IllegalArgumentException("Output file cannot be null");

        this.fileIterator = new FileIterator(inputDir, (globPattern == null || globPattern.isEmpty()) ? "*" : globPattern);
        this.storage = storage;
        this.writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(outFile), StandardCharsets.UTF_8));
        
        // Calculate bits per component
        logger.info("UriTripleFileSink initialized: inputDir={}, pattern={}, outFile={}",
                inputDir, globPattern, outFile);

        eventBus.get().subscribe(UniqueEntityEvent.class, e -> {this.uniqueComponents.incrementAndGet(); } );
        eventBus.get().subscribe(UniquePredicateEvent.class, e -> {this.uniqueComponents.incrementAndGet();} );
    }

    @Override
    protected void drain(Batch in, Emitter out) throws Exception {
        bitsPerComponent = calculateBitWidth(uniqueComponents.get());
        logger.info("Bits/Component are: {}", bitsPerComponent);

        // Process all files similarly to the pump
        while (fileIterator.hasNext()) {
            Path file = fileIterator.next();
            long t0 = System.nanoTime();
            logger.info("Processing file: {}", file);

            try (URIStreamer streamer = URIStreamerFactory.getReader(file.toString())) {
                streamer.stream(file.toString(), this::processTriple);
            }

            long t1 = System.nanoTime();
            logger.info("Completed file: {} ({} ms)", file, (t1 - t0) / 1_000_000);
        }
    }

    /**
     * Encodes a triple's components as a fixed-width bitstring and writes one line per triple.
     */
    private void processTriple(URITriple triple) {
        try {
            String sBits = getComponentBits(triple.getSubject());
            String pBits = getComponentBits(triple.getPredicate());
            String oBits = getComponentBits(triple.getObject());

            writer.write(sBits);
            writer.write(pBits);
            writer.write(oBits);
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write encoded triple", e);
        } catch (StorageException e) {
            throw new RuntimeException("Storage error during encoding", e);
        }
    }

    private String getComponentBits(TripleComponent component) throws StorageException {
        String value = component.getValue();
        byte[] keyBytes = strSer.serialize(value);
        byte[] idBytes = storage.get(keyBytes);
        
        if (idBytes == null) {
            throw new IllegalStateException("Component not found in storage: " + value);
        }
        
        int id = intSer.deserialize(idBytes);
        return toFixedWidthBits(id, bitsPerComponent);
    }

    private static int calculateBitWidth(int uniqueComponents) {
        if (uniqueComponents <= 1) return 1;
        return (int) Math.ceil(Math.log(uniqueComponents) / Math.log(2));
    }

    private static String toFixedWidthBits(int value, int width) {
        // Build binary string representation
        StringBuilder bits = new StringBuilder(width);
        for (int i = width - 1; i >= 0; i--) {
            int bit = (value >> i) & 1;
            bits.append(bit);
        }
        return bits.toString();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}