package Ctransformers.decoder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class R1Decoder implements IDecoder {
    private Map<Integer, String> mapping;

    @Override
    public void loadMappings(String mappingFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(mappingFilePath), StandardCharsets.UTF_8))) {
            
            String firstLine = reader.readLine();
            if (firstLine == null || !firstLine.startsWith("R1")) {
                throw new IOException("Invalid mapping file format: Missing R1 header");
            }

            // Extract the total expected count
            String[] headerParts = firstLine.split(", ");
            if (headerParts.length != 2) {
                throw new IOException("Invalid mapping header format. Expected 'R1, <count>'");
            }

            int entryCount = Integer.parseInt(headerParts[1].trim());
            mapping = new HashMap<>(entryCount);

            StringBuilder currentEntry = new StringBuilder();
            int entriesLoaded = 0;
            String line;

            while (entriesLoaded < entryCount && (line = reader.readLine()) != null) {
                // Skip empty lines
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
                        mapping.put(id, value);
                        entriesLoaded++;
                        
                        // Reset for next entry
                        currentEntry.setLength(0);
                    } catch (NumberFormatException e) {
                        // Not an ID - continue accumulating lines
                    }
                }
            }

            // Handle last entry if buffer isn't empty
            if (currentEntry.length() > 0) {
                int lastSpace = currentEntry.lastIndexOf(" ");
                if (lastSpace != -1) {
                    try {
                        String idPart = currentEntry.substring(lastSpace + 1).trim();
                        int id = Integer.parseInt(idPart);
                        mapping.put(id, currentEntry.substring(0, lastSpace).trim());
                        entriesLoaded++;
                    } catch (NumberFormatException e) {
                        throw new IOException("Last entry missing valid ID: " + currentEntry.toString());
                    }
                }
            }

            if (entriesLoaded != entryCount) {
                throw new IOException("Entry count mismatch. Expected: " + entryCount + 
                                      ", Loaded: " + entriesLoaded);
            }
        }
    }

    @Override
    public void decodeFile(String encodedFilePath, String outputCsvPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(encodedFilePath), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                 new OutputStreamWriter(new FileOutputStream(outputCsvPath), StandardCharsets.UTF_8))) {
            
            writer.write("Subject,Predicate,Object\n");
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // Validate line length is divisible by 3
                if (line.length() % 3 != 0) {
                    throw new IOException("Invalid line length: " + line.length() + 
                                         ". Must be divisible by 3");
                }
                
                int segmentLength = line.length() / 3;
                int s = Integer.parseInt(line.substring(0, segmentLength), 2);
                int p = Integer.parseInt(line.substring(segmentLength, 2 * segmentLength), 2);
                int o = Integer.parseInt(line.substring(2 * segmentLength), 2);

                // Lookup mappings
                String subject = mapping.getOrDefault(s, "UNKNOWN");
                String predicate = mapping.getOrDefault(p, "UNKNOWN");
                String object = mapping.getOrDefault(o, "UNKNOWN");

                // Write to CSV
                writer.write(subject + "," + predicate + "," + object + "\n");
            }
        }
    }
}