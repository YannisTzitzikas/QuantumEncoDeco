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

    private final SortedSet<String> memoryAccumulator;
    private final int maxMemoryCapacity;
    private final WindowedChunkMerger chunkMerger;

    public BoundedMemorySortSink(WindowedChunkMerger chunkMerger, int maxMemoryCapacity) {
        this.chunkMerger = chunkMerger;
        this.maxMemoryCapacity = maxMemoryCapacity;
        this.memoryAccumulator = new TreeSet<>(); 
    }

    @Override
    public void accept(TripleComponent comp) {
        if (comp != null && comp.getValue() != null) {
            memoryAccumulator.add(comp.getValue());
            
            if (memoryAccumulator.size() >= maxMemoryCapacity) {
                flushBufferToDisk();
            }
        }
    }

    public void flushRemaining() {
        if (!memoryAccumulator.isEmpty()) {
            flushBufferToDisk();
        }
    }

    private void flushBufferToDisk() {
        try {
            Path chunkPath = chunkMerger.writeInitialSortedChunk(memoryAccumulator);
            memoryAccumulator.clear(); 
            chunkMerger.registerAndProgressMerge(chunkPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to flush streaming memory buffer", e);
        }
    }
}