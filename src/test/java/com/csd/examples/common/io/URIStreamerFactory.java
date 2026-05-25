package com.csd.examples.common.io;

import java.io.IOException;

public class URIStreamerFactory {
    public static URIStreamer getReader(String filePath) throws IOException {
        String extension = filePath.toLowerCase().substring(filePath.lastIndexOf('.'));
        
        if (extension.equals(".rdf") || extension.equals(".xml") || extension.equals(".ttl")) {
            return new OntologyURIStreamer();
        } else {
            throw new IllegalArgumentException("Unsupported format: " + filePath);
        }
    }
}
