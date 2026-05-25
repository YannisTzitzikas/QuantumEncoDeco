package com.csd.examples.common.filters.sinks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class TripleOutputSink implements Consumer<String>, AutoCloseable {
    private final BufferedWriter writer;

    public TripleOutputSink(Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent());
        this.writer = Files.newBufferedWriter(outputFile);
    }

    @Override
    public synchronized void accept(String decodedTriple) {
        try {
            writer.write(decodedTriple);
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed writing decoded triples to output path", e);
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