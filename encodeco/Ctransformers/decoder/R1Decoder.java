package Ctransformers.decoder;

import java.io.*;
import java.util.*;

public class R1Decoder {

    // HashMap to store mappings
    private Map<Integer, String> mapping = new HashMap<>();

    public void loadMappings(String mappingFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(mappingFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2); // Split into ID and URI
                if (parts.length == 2) {
                    mapping.put(Integer.parseInt(parts[1]), parts[0]); // Store <ID, URI>
                }
            }
        }
    }

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