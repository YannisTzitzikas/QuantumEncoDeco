package com.csd.core.io.writers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DiskWriter {
    private final String filePath;

    public DiskWriter(String filePath) {
        this.filePath = filePath;
    }

    public void saveBatch(List<String> encodedTriples) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (String encodedTriple : encodedTriples) {
                writer.write(encodedTriple);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
