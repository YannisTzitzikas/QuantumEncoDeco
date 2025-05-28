package Ctransformers.decoder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class R2Decoder implements IDecoder {
    private Map<Integer, String> predicateMappings;
    private Map<Integer, String> entityMappings;
    private int predicateBitCount;
    private int entityBitCount;

    @Override
    public void loadMappings(String mappingFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(mappingFilePath), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();

            // Handle UTF-8 BOM if present
            if (firstLine != null && firstLine.startsWith("\uFEFF")) {
                firstLine = firstLine.substring(1);
            }
            if (firstLine == null || !firstLine.startsWith("R2")) {
                throw new IOException("Invalid mapping file format: Missing type information in the first line.");
            }

            // Extract counts
            String[] headerParts = firstLine.split(", ");
            if (headerParts.length != 3) {
                throw new IOException("Invalid mapping header format.");
            }

            int predicateCount = Integer.parseInt(headerParts[1].trim());
            int entityCount = Integer.parseInt(headerParts[2].trim());

            // Preallocate HashMaps
            predicateMappings = new HashMap<>(predicateCount);
            entityMappings = new HashMap<>(entityCount);

            // Compute bit counts
            predicateBitCount = bitCount(predicateCount);
            entityBitCount = bitCount(entityCount);

            StringBuilder currentEntry = new StringBuilder();
            int predicatesLoaded = 0;
            String line;

            while (predicatesLoaded < predicateCount && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                if (currentEntry.length() > 0) {
                    currentEntry.append("\n");
                }
                currentEntry.append(line);

                // Check if the current buffer ends with a numeric ID
                int lastSpace = currentEntry.lastIndexOf(" ");
                if (lastSpace != -1) {
                    try {
                        String idPart = currentEntry.substring(lastSpace + 1).trim();
                        int id = Integer.parseInt(idPart);
                        
                        // Found complete entry
                        String value = currentEntry.substring(0, lastSpace).trim();
                        predicateMappings.put(id, value);
                        predicatesLoaded++;
                        
                        currentEntry.setLength(0);
                    } catch (NumberFormatException e) {
                        // Not an ID - continue accumulating lines
                    }
                }
            }

            if (predicatesLoaded != predicateCount) {
                throw new IOException("Entity count mismatch. Expected: " + predicateCount + 
                                      ", Loaded: " + predicatesLoaded);
            }

            // Read entities (can be multi-line)
            StringBuilder currentEntity = new StringBuilder();
            int entitiesLoaded = 0;

            while (entitiesLoaded < entityCount && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                if (currentEntry.length() > 0) {
                    currentEntry.append("\n");
                }
                currentEntry.append(line);

                // Check if the current buffer ends with a numeric ID
                int lastSpace = currentEntry.lastIndexOf(" ");
                if (lastSpace != -1) {
                    try {
                        String idPart = currentEntry.substring(lastSpace + 1).trim();
                        int id = Integer.parseInt(idPart);
                        
                        // Found complete entry
                        String value = currentEntry.substring(0, lastSpace).trim();
                        entityMappings.put(id, value);
                        entitiesLoaded++;
                        
                        currentEntry.setLength(0);
                    } catch (NumberFormatException e) {
                    }
                }
            }

            if (currentEntity.length() > 0) {
                int lastSpace = currentEntity.lastIndexOf(" ");
                if (lastSpace != -1) {
                    try {
                        String idPart = currentEntity.substring(lastSpace + 1).trim();
                        int id = Integer.parseInt(idPart);
                        entityMappings.put(id, currentEntity.substring(0, lastSpace).trim());
                        entitiesLoaded++;
                    } catch (NumberFormatException e) {
                        System.err.println(currentEntity.toString());
                    }
                }
            }

            if (entitiesLoaded != entityCount) {
                throw new IOException("Entity count mismatch. Expected: " + entityCount + 
                                      ", Loaded: " + entitiesLoaded);
            }
        }
    }

    @Override
    public void decodeFile(String encodedFilePath, String outputCsvPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(encodedFilePath), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                 new FileOutputStream(outputCsvPath), StandardCharsets.UTF_8))) {

            writer.write("Subject,Predicate,Object\n");

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                int totalBits = entityBitCount + predicateBitCount + entityBitCount;
                if (line.length() != totalBits) {
                    throw new IOException("Invalid line length: " + line.length() + 
                                         ". Expected: " + totalBits);
                }

                // Parse subject
                int s = 0;
                if (entityBitCount > 0) {
                    s = Integer.parseInt(line.substring(0, entityBitCount), 2);
                }

                // Parse predicate
                int p = 0;
                if (predicateBitCount > 0) {
                    p = Integer.parseInt(line.substring(
                        entityBitCount, entityBitCount + predicateBitCount), 2);
                }

                // Parse object
                int o = 0;
                if (entityBitCount > 0) {
                    o = Integer.parseInt(line.substring(
                        entityBitCount + predicateBitCount), 2);
                }

                // Lookup mappings
                String subject = entityMappings.getOrDefault(s, "UNKNOWN");
                String predicate = predicateMappings.getOrDefault(p, "UNKNOWN");
                String object = entityMappings.getOrDefault(o, "UNKNOWN");

                // Write to CSV
                writer.write(subject + "," + predicate + "," + object + "\n");
            }
        }
    }

    private int bitCount(int count) {
        if (count <= 1) {
            return count; // 0 or 1 bit
        }
        return 32 - Integer.numberOfLeadingZeros(count - 1);
    }
}