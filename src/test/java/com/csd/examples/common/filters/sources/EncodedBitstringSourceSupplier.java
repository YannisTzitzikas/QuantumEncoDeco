package com.csd.examples.common.filters.sources;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.github.jsonldjava.shaded.com.google.common.base.Supplier;

public class EncodedBitstringSourceSupplier implements Supplier<List<String>> {
    private final int batchSize;
    private BufferedReader reader;

    public EncodedBitstringSourceSupplier(Path encodedFile, int batchSize) throws IOException {
        this.batchSize = batchSize;
        this.reader = Files.newBufferedReader(encodedFile);
    }

    @Override
    public List<String> get() {
        if (reader == null) return null;
        
        List<String> batch = new ArrayList<>(batchSize);
        String line;
        try {
            while (batch.size() < batchSize && (line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    batch.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading encoded bitstrings", e);
        }

        return batch.isEmpty() ? null : batch;
    }
}