package com.csd.core.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.csd.core.execution.operations.BatchMapOp;
import com.csd.core.execution.operations.FilterOp;
import com.csd.core.execution.operations.FlatMapOp;
import com.csd.core.execution.operations.MapOp;
import com.csd.core.execution.operations.SinkOp;
import com.csd.core.schema.StreamVertex;

public class StreamBuilder<T> {
    private final StreamEnvironment graph;
    private final List<StreamVertex<?, T>> tailNodes;

    StreamBuilder(StreamEnvironment graph, List<StreamVertex<?, T>> nodes) {
        this.graph = graph;
        this.tailNodes = new ArrayList<>(nodes);
    }

    public <R> StreamBuilder<R> map(Function<T, R> mapper) {
        StreamVertex<T, R> nextNode = new StreamVertex<>(new MapOp<>(mapper));
        for (StreamVertex<?, T> tail : tailNodes) {
            tail.connectTo(nextNode);
        }
        graph.registerNode(nextNode);
        return new StreamBuilder<>(graph, List.of(nextNode));
    }

    public <R> StreamBuilder<R> batchMap(Function<List<T>, List<R>> mapper) {
        StreamVertex<T, R> nextNode = new StreamVertex<>(new BatchMapOp<>(mapper));
        for (StreamVertex<?, T> tail : tailNodes) {
            tail.connectTo(nextNode);
        }
        graph.registerNode(nextNode);
        return new StreamBuilder<>(graph, List.of(nextNode));
    }

    public <R> StreamBuilder<R> flatMap(Function<T, List<R>> flatMapper) {
        StreamVertex<T, R> nextNode = new StreamVertex<>(new FlatMapOp<>(flatMapper));
        for (StreamVertex<?, T> tail : tailNodes) {
            tail.connectTo(nextNode);
        }
        graph.registerNode(nextNode);
        return new StreamBuilder<>(graph, List.of(nextNode));
    }

    public StreamBuilder<T> filter(Predicate<T> predicate) {
        StreamVertex<T, T> nextNode = new StreamVertex<>(new FilterOp<>(predicate));
        for (StreamVertex<?, T> tail : tailNodes) {
            tail.connectTo(nextNode);
        }
        graph.registerNode(nextNode);
        return new StreamBuilder<>(graph, List.of(nextNode));
    }

    @SafeVarargs
    public final StreamBuilder<T> merge(StreamBuilder<T>... branches) {
        List<StreamVertex<?, T>> combinedTails = new ArrayList<>(this.tailNodes);
        for (StreamBuilder<T> branch : branches) {
            combinedTails.addAll(branch.tailNodes);
        }
        return new StreamBuilder<>(graph, combinedTails);
    }

    // ==========================================
    // SINK OVERLOADS
    // ==========================================

    /**
     * Base Sink: Consumes elements using a custom user-provided function.
     */
    public void sink(Consumer<T> consumer) {
        StreamVertex<T, Void> sinkNode = new StreamVertex<>(new SinkOp<>(consumer));
        for (StreamVertex<?, T> tail : tailNodes) {
            tail.connectTo(sinkNode);
        }
        graph.registerNode(sinkNode);
    }

    /**
     * Fallback Sink: Prints elements directly to standard output (STDOUT).
     * Useful for debugging or terminal-based pipelines.
     */
    public void sink() {
        sink(item -> System.out.println(item == null ? "null" : item.toString()));
    }

    /**
     * String File Sink: Converts the string path to a Path object and delegates.
     */
    public void sink(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Target file path cannot be null or empty.");
        }
        sink(Paths.get(filePath));
    }

    public void sink(Path filePath) {
        try {
            sink(new FileWriterConsumer<>(filePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize sink output file at: " + filePath.toAbsolutePath(), e);
        }
    }

    /**
     * A thread-safe, auto-closeable consumer that handles file writing.
     * By implementing AutoCloseable, the pipeline executor can safely flush
     * and close the file when the execution graph terminates.
     */
    private static class FileWriterConsumer<T> implements Consumer<T>, AutoCloseable {
        private final BufferedWriter writer;
        private final Path path;

        public FileWriterConsumer(Path path) throws IOException {
            this.path = path;
            
            // Validate and create parent directories if they don't exist
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            this.writer = Files.newBufferedWriter(path);
        }

        @Override
        public synchronized void accept(T item) {
            if (item == null) return;
            try {
                writer.write(item.toString());
                writer.newLine();
            } catch (IOException e) {
                throw new UncheckedIOException("Pipeline write failure at file: " + path.getFileName(), e);
            }
        }

        @Override
        public void close() throws IOException {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }
}