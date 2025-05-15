package com.ics.io.readers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVTripleReader {
    public List<Triple> readTriples(String filePath) throws IOException {
        List<Triple> triples = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length == 3) {
                    triples.add(new Triple(parts[0], parts[1], parts[2]));
                }
            }
        }
        return triples;
    }

    public static class Triple {
        private final String subject;
        private final String predicate;
        private final String object;

        public Triple(String subject, String predicate, String object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }

        @Override
        public String toString() {
            return String.format("(%s, %s, %s)", subject, predicate, object);
        }
    }
}