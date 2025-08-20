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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Terminal sink that encodes triples into fixed-width bitstrings and
 * writes them as one concatenated bitstring per line to an output file.
 */
public final class R1UriTripleFileSink extends AbstractSink implements Closeable {

    public static final InputPort<Void> IN = new InputPort<>("start");

    private final FileIterator fileIterator;
    private final StorageEngine storage;
    private final BufferedWriter writer;
    private final StringSerializer strSer = new StringSerializer();
    private final IntegerSerializer intSer = new IntegerSerializer();
    private       int bitsPerComponent;
    private final AtomicInteger uniqueComponents = new AtomicInteger(0);

    // New fields for batching
    private final int batchSize;
    private final List<TripleComponent> componentBatch = new ArrayList<>();
    private final List<URITriple> tripleBatch = new ArrayList<>();

    public R1UriTripleFileSink(Path inputDir,
                               String globPattern,
                               PortBindings bindings,
                               StorageEngine storage,
                               Path outFile,
                               EventBus bus,
                               int batchSize) throws IOException {
        super(Arrays.asList(IN), bindings, new BarrierPolicy(), bus);
        if (storage == null) throw new IllegalArgumentException("StorageEngine cannot be null");
        if (outFile == null) throw new IllegalArgumentException("Output file cannot be null");

        this.fileIterator = new FileIterator(inputDir,
            (globPattern == null || globPattern.isEmpty()) ? "*" : globPattern);
        this.storage = storage;
        this.writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(outFile), StandardCharsets.UTF_8));

        this.batchSize = batchSize;

        logger.info("UriTripleFileSink initialized: inputDir={}, pattern={}, outFile={}, batchSize={}",
                inputDir, globPattern, outFile, batchSize);

        eventBus.get().subscribe(UniqueEntityEvent.class,
                e -> this.uniqueComponents.incrementAndGet());
        eventBus.get().subscribe(UniquePredicateEvent.class,
                e -> this.uniqueComponents.incrementAndGet());
    }

    @Override
    protected void drain(Batch in, Emitter out) throws Exception {
        bitsPerComponent = calculateBitWidth(uniqueComponents.get());
        logger.info("Bits/Component are: {}", bitsPerComponent);

        while (fileIterator.hasNext()) {
            Path file = fileIterator.next();
            long t0 = System.nanoTime();
            logger.info("Processing file: {}", file);

            try (URIStreamer streamer = URIStreamerFactory.getReader(file.toString())) {
                streamer.stream(file.toString(), this::apply);
                flushBatch(); // process any leftovers
            }

            long t1 = System.nanoTime();
            logger.info("Completed file: {} ({} ms)", file, (t1 - t0) / 1_000_000);
        }
    }

    // New apply method for batching
    private void apply(URITriple triple) {
        tripleBatch.add(triple);
        componentBatch.add(triple.getSubject());
        componentBatch.add(triple.getPredicate());
        componentBatch.add(triple.getObject());

        if (tripleBatch.size() >= batchSize) {
            processBatch();
        }
    }

    private void flushBatch() {
        if (!tripleBatch.isEmpty()) {
            processBatch();
        }
    }

    private void processBatch() {
        try {
            // Serialize all components and get IDs in bulk
            List<byte[]> keys = componentBatch.stream()
                    .map(tc -> strSer.serialize(tc.getValue()))
                    .collect(Collectors.toList());

            List<byte[]> ids = storage.getAll(keys); // Bulk retrieval

            if (ids.size() != keys.size()) {
                throw new IllegalStateException("Mismatch in component IDs returned");
            }

            int idIndex = 0;
            for (URITriple triple : tripleBatch) {
                String sBits = toFixedWidthBits(intSer.deserialize(ids.get(idIndex++)), bitsPerComponent);
                String pBits = toFixedWidthBits(intSer.deserialize(ids.get(idIndex++)), bitsPerComponent);
                String oBits = toFixedWidthBits(intSer.deserialize(ids.get(idIndex++)), bitsPerComponent);

                writer.write(sBits);
                writer.write(pBits);
                writer.write(oBits);
                writer.newLine();
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write encoded triple", e);
        } catch (StorageException e) {
            throw new RuntimeException("Storage error during batch encoding", e);
        } finally {
            tripleBatch.clear();
            componentBatch.clear();
        }
    }

    // unchanged helper methods
    private static int calculateBitWidth(int uniqueComponents) {
        if (uniqueComponents <= 1) return 1;
        return (int) Math.ceil(Math.log(uniqueComponents) / Math.log(2));
    }

    private static String toFixedWidthBits(int value, int width) {
        StringBuilder bits = new StringBuilder(width);
        for (int i = width - 1; i >= 0; i--) {
            bits.append((value >> i) & 1);
        }
        return bits.toString();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
