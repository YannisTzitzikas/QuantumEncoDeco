package com.csd.core.io.readers;

import java.io.IOException;

public class URIReaderFactory {
    public static URIReader getReader(String filePath) throws IOException {
        String extension = filePath.toLowerCase().substring(filePath.lastIndexOf('.'));
        
        if (extension.equals(".csv")) {
            return new CSVURIReader();
        } else if (extension.equals(".rdf") || extension.equals(".xml") || extension.equals(".ttl")) {
            return new OntologyURIReader();
        } else {
            throw new IllegalArgumentException("Unsupported format: " + filePath);
        }
    }
}
