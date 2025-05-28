package Ctransformers.decoder;

import java.io.*;
import java.util.*;

public class R1Decoder implements IDecoder {
    private final Map<Integer, String> mapping = new HashMap<>();

    @Override
    public void loadMappings(String mappingFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(mappingFilePath))) {
            String firstLine = reader.readLine();
            if (firstLine == null || !firstLine.startsWith("R1")) {
                throw new IOException("Invalid mapping file format: Missing type information in the first line.");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2); // Ensure only two segments are extracted
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[1].trim()); // Trim spaces & ensure it's a number
                        mapping.put(id, parts[0]); // Store <ID, URI>
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
                int segmentLength = line.length() / 3; // Assuming equal length for Subject, Predicate, Object

                int s = Integer.parseInt(line.substring(0, segmentLength), 2); // Binary to Integer
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
