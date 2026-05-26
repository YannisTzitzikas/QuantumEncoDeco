package com.csd.examples.common.filters.sinks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import com.csd.examples.common.filters.operations.WindowedChunkMerger;
import com.csd.examples.common.model.TripleComponent;

public class BoundedMemorySortSink implements Consumer<TripleComponent> {

    private SortedSet<String> memoryAccumulator;
    private final int maxMemoryCapacity;
    private final WindowedChunkMerger chunkMerger;
    
    // Dedicated lock object for micro-synchronization
    private final Object lock = new Object();

    public BoundedMemorySortSink(WindowedChunkMerger chunkMerger, int maxMemoryCapacity) {
        this.chunkMerger = chunkMerger;
        this.maxMemoryCapacity = maxMemoryCapacity;
        this.memoryAccumulator = new TreeSet<>(); 
    }

    @Override
    public void accept(TripleComponent comp) {
        if (comp == null || comp.getValue() == null) {
            return;
        }

        SortedSet<String> readyToFlush = null;

        // 1. FAST LOCK: Only lock while actively inserting into the TreeSet
        synchronized (lock) {
            memoryAccumulator.add(comp.getValue());
            
            if (memoryAccumulator.size() >= maxMemoryCapacity) {
                // Detach the full buffer so it can be written safely
                readyToFlush = memoryAccumulator;
                // Instantly replace it with a fresh buffer for other threads
                memoryAccumulator = new TreeSet<>();
            }
        }

        // 2. HEAVY I/O: Write to disk OUTSIDE the lock so we don't block the stream
        if (readyToFlush != null) {
            writeAndRegister(readyToFlush);
        }
    }

    public void flushRemaining() {
        SortedSet<String> readyToFlush = null;
        
        synchronized (lock) {
            if (!memoryAccumulator.isEmpty()) {
                readyToFlush = memoryAccumulator;
                memoryAccumulator = new TreeSet<>(); 
            }
        }
        
        if (readyToFlush != null) {
            writeAndRegister(readyToFlush);
        }
    }

    private void writeAndRegister(SortedSet<String> dataChunk) {
        try {
            // The chunk is now isolated; no other threads can modify it.
            Path chunkPath = chunkMerger.writeInitialSortedChunk(dataChunk);
            chunkMerger.registerAndProgressMerge(chunkPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to flush streaming memory buffer", e);
        }
    }
}