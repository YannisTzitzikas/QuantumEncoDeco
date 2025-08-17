package com.csd.pipeline.pumps;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.common.io.FileIterator;
import com.csd.io.URIStreamerFactory;
import com.csd.core.io.URIStreamer;
import com.csd.core.pipeline.AbstractPump;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.model.Message;
import com.csd.core.model.uri.URITriple;

public final class UriTripleBatchPump extends AbstractPump {

    private static final Logger log = LoggerFactory.getLogger(UriTripleBatchPump.class);

    public static final OutputPort<List<URITriple>> OUT =
            new OutputPort<>("triples");

    private final FileIterator fileIterator;
    private final int batchSize;
    private final List<URITriple> batch;
    private final boolean flushOnFileBoundary;

    private long batchCounter = 0;
    private long lastEmitTime;

    public UriTripleBatchPump(Path path,
                              String globPattern,
                              int batchSize,
                              PortBindings bindings) throws IOException {
        this(path, globPattern, batchSize, false, bindings);
    }

    public UriTripleBatchPump(Path path,
                              String globPattern,
                              int batchSize,
                              boolean flushOnFileBoundary,
                              PortBindings bindings) throws IOException {
        super(Arrays.asList(OUT), bindings, 0);
        if (batchSize <= 0) throw new IllegalArgumentException("batchSize must be > 0");
        this.fileIterator = new FileIterator(path, globPattern == null ? "*" : globPattern);
        this.batchSize = batchSize;
        this.batch = new ArrayList<>(batchSize);
        this.flushOnFileBoundary = flushOnFileBoundary;
        this.lastEmitTime = System.nanoTime();

        log.info("UriTripleBatchPump initialized with path={}, globPattern={}, batchSize={}, flushOnFileBoundary={}",
                path, globPattern, batchSize, flushOnFileBoundary);
    }

    @Override
    protected boolean step(Emitter out) throws Exception {
        boolean progressed = false;

        while (fileIterator.hasNext()) {
            Path file = fileIterator.next();
            progressed = true;
            long fileStart = System.nanoTime();

            log.info("Processing file: {}", file);

            try (URIStreamer streamer = URIStreamerFactory.getReader(file.toString())) {
                Consumer<URITriple> sink = triple -> acceptTriple(triple, out);
                streamer.stream(file.toString(), sink); 
            }

            long fileEnd = System.nanoTime();
            log.info("Completed file: {} ({} ms)", file, (fileEnd - fileStart) / 1_000_000);

            if (flushOnFileBoundary && !batch.isEmpty()) {
                emitBatch(out);
                return true;
            }
        }

        if (!batch.isEmpty()) {
            emitBatch(out);
            return true;
        }

        log.info("No more files to process. Pump stopping after {} batches.", batchCounter);
        stop();
        return progressed;
    }

    private void acceptTriple(URITriple triple, Emitter out) {
        batch.add(triple);
        if (batch.size() >= batchSize) {
            emitBatch(out);
        }
    }

    private void emitBatch(Emitter out) {
        try {
            List<URITriple> snapshot = new ArrayList<>(batch);
            out.emit(OUT, Message.data(snapshot));

            batchCounter++;
            long now = System.nanoTime();
            long elapsedSinceLast = (now - lastEmitTime) / 1_000_000;
            lastEmitTime = now;

            log.info("Emitted batch #{} with {} triples ({} ms since last batch)",
                    batchCounter, snapshot.size(), elapsedSinceLast);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while emitting batch #{}", batchCounter + 1, ie);
            throw new RuntimeException("Interrupted while emitting triple batch", ie);
        } finally {
            batch.clear();
        }
    }
}
