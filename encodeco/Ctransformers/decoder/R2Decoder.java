package Ctransformers.decoder;

import java.io.*;
import java.util.*;

public class R2Decoder implements IDecoder {
    private Map<Integer, String> predicateMappings;
    private Map<Integer, String> entityMappings;
    private int predicateBitCount;
    private int entityBitCount;

    @Override
    public void loadMappings(String mappingFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(mappingFilePath))) {
            String firstLine = reader.readLine();
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
            predicateBitCount = (int) Math.ceil(Math.log(predicateCount) / Math.log(2));
            entityBitCount = (int) Math.ceil(Math.log(entityCount) / Math.log(2));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[1].trim()); // Ensure clean number parsing
                        if (predicateMappings.size() < predicateCount) {
                            predicateMappings.put(id, parts[0].trim()); // Trim URI
                        } else {
                            entityMappings.put(id, parts[0].trim());
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid mapping line: " + line);
                    }
                }
            }
        }
    }


    @Override
    public void decodeFile(String encodedFilePath, String outputCsvPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(encodedFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsvPath))) {

            writer.write("Subject,Predicate,Object\n"); // CSV Header

            String line;
            while ((line = reader.readLine()) != null) {
                int totalBits = predicateBitCount + (2 * entityBitCount); // Assuming format: [Entity][Predicate][Entity]
                
                if (line.length() != totalBits) {
                    throw new IOException("Invalid encoded file format. Bit lengths do not match expected values.");
                }

                int s = Integer.parseInt(line.substring(0, entityBitCount), 2); // Binary to Integer
                int p = Integer.parseInt(line.substring(entityBitCount, entityBitCount + predicateBitCount), 2);
                int o = Integer.parseInt(line.substring(entityBitCount + predicateBitCount), 2);

                // Lookup mappings
                String subject = entityMappings.getOrDefault(s, "UNKNOWN");
                String predicate = predicateMappings.getOrDefault(p, "UNKNOWN");
                String object = entityMappings.getOrDefault(o, "UNKNOWN");

                // Write to CSV
                writer.write(subject + "," + predicate + "," + object + "\n");
            }
        }
    }
}
