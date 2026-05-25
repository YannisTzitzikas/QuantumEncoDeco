package com.csd.examples.common.filters.operations;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.csd.examples.common.metrics.PipelineContext;

/**
 * Windowed Chunk Merger managing intermediate 10-file stream validations and
 * final sorting writes.
 */
public class WindowedChunkMerger {
    private final Path workDir;
    private final String roleLabel;
    private final PipelineContext context;

    private final List<Path> activeChunkFiles = new ArrayList<>();
    private final AtomicInteger fileSequence = new AtomicInteger(0);
    private final AtomicInteger generationSequence = new AtomicInteger(0);

    private static final int WINDOW_THRESHOLD = 10;

    public WindowedChunkMerger(Path workDir, String roleLabel, PipelineContext context) {
        this.workDir = workDir;
        this.roleLabel = roleLabel;
        this.context = context;
    }

    public Path writeInitialSortedChunk(Collection<String> sortedData) throws IOException {
        Path chunk = workDir.resolve("chunk_" + fileSequence.getAndIncrement() + ".dat");
        try (BufferedWriter writer = Files.newBufferedWriter(chunk)) {
            for (String s : sortedData) {
                writer.write(s);
                writer.newLine();
            }
        }
        return chunk;
    }

    public synchronized void registerAndProgressMerge(Path path) throws IOException {
        activeChunkFiles.add(path);

        if (activeChunkFiles.size() >= WINDOW_THRESHOLD) {
            Path consolidatedGenFile = workDir.resolve("gen_" + generationSequence.getAndIncrement() + ".dat");

            try (BufferedWriter writer = Files.newBufferedWriter(consolidatedGenFile)) {
                executeKWayMerge(activeChunkFiles, writer, false);
            }

            for (Path p : activeChunkFiles) {
                Files.deleteIfExists(p);
            }
            activeChunkFiles.clear();
            activeChunkFiles.add(consolidatedGenFile);
        }
    }

    public synchronized void executeFinalMergePass(Path finalOutFile) throws IOException {
        if (activeChunkFiles.isEmpty()) {
            try (BufferedWriter writer = Files.newBufferedWriter(finalOutFile)) {
                writer.write("0\n"); // Empty data guard
            }
            return;
        }

        // Step A: We must know total size before writing, merge into a temporary
        // scratch file first
        Path tempFinalScratch = workDir.resolve("final_scratch.tmp");
        long totalUniqueElements = 0;

        try (BufferedWriter scratchWriter = Files.newBufferedWriter(tempFinalScratch)) {
            totalUniqueElements = executeKWayMerge(activeChunkFiles, scratchWriter, true);
        }

        // Step B: Write output file putting the total size count at the very top line
        try (BufferedReader reader = Files.newBufferedReader(tempFinalScratch);
                BufferedWriter finalWriter = Files.newBufferedWriter(finalOutFile)) {

            // Write total item counts at the top of the file
            finalWriter.write(totalUniqueElements + "\n");

            String line;
            while ((line = reader.readLine()) != null) {
                finalWriter.write(line);
                finalWriter.newLine();
            }
        }

        // Clean up files inside the scratch directory
        for (Path p : activeChunkFiles) {
            Files.deleteIfExists(p);
        }
        Files.deleteIfExists(tempFinalScratch);
    }

    /**
     * Core Priority-Queue Backed External Sort K-Way Merge routine.
     */
    private long executeKWayMerge(List<Path> targets, BufferedWriter output, boolean assigningGlobalIds)
            throws IOException {
        PriorityQueue<MergeCursor> heap = new PriorityQueue<>(Comparator.comparing(c -> c.currentLine));
        List<BufferedReader> activeReaders = new ArrayList<>();

        try {
            for (Path p : targets) {
                BufferedReader br = Files.newBufferedReader(p);
                activeReaders.add(br);
                MergeCursor cursor = new MergeCursor(br);
                if (cursor.advance()) {
                    heap.add(cursor);
                }
            }

            long sequentialId = 0;
            String lastValue = null;

            while (!heap.isEmpty()) {
                MergeCursor top = heap.poll();
                String currentValue = top.currentLine;

                // Inter-file Deduplication Pass
                if (lastValue == null || !lastValue.equals(currentValue)) {
                    if (assigningGlobalIds) {
                        // Format requirements: element space ordered/numbered on second column
                        output.write(currentValue + " " + sequentialId + " [" + roleLabel + "]");
                        output.newLine();
                    } else {
                        output.write(currentValue);
                        output.newLine();
                    }
                    sequentialId++;
                    lastValue = currentValue;
                }

                if (top.advance()) {
                    heap.add(top);
                } else {
                    top.close();
                }
            }

            if (assigningGlobalIds) {
                if ("ENTITY".equals(roleLabel))
                    context.addUniqueEntities(sequentialId);
                else
                    context.addUniquePredicates(sequentialId);
            }

            return sequentialId;
        } finally {
            for (BufferedReader br : activeReaders) {
                try {
                    br.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static class MergeCursor {
        final BufferedReader reader;
        String currentLine;

        MergeCursor(BufferedReader reader) {
            this.reader = reader;
        }

        boolean advance() throws IOException {
            return (currentLine = reader.readLine()) != null;
        }

        void close() throws IOException {
            reader.close();
        }
    }
}