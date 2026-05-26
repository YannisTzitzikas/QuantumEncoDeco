package com.csd.examples.common.filters.sources;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.csd.core.api.functions.AdaptiveBatchSupplier;

public class EncodedBitstringSourceSupplier implements AdaptiveBatchSupplier<String> {
    private final int maxBatchSize;
    private final BufferedReader reader;

    public EncodedBitstringSourceSupplier(Path encodedFile, int maxBatchSize) throws IOException {
        this.maxBatchSize = maxBatchSize;
        this.reader = Files.newBufferedReader(encodedFile);
    }

    @Override
    public List<String> getBatch(int targetSize) {
        if (reader == null) return null;
        
        // Ensure we never read more than what the system can handle right now, capped at max baseline
        int dynamicLimit = Math.min(targetSize, maxBatchSize);
        List<String> batch = new ArrayList<>(dynamicLimit);
        String line;
        
        try {
            while (batch.size() < dynamicLimit && (line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    batch.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading encoded bitstrings adaptive batch", e);
        }

        return batch.isEmpty() ? null : batch;
    }
}