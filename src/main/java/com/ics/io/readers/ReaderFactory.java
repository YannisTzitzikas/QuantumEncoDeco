package com.ics.io.readers;

import java.io.IOException;

public class ReaderFactory {
    
    public static RecordReader getReader(String filePath) throws IOException {
        
        String extension =  filePath
                            .toLowerCase()
                            .substring(filePath.lastIndexOf('.'));

        if (extension == ".csv") {
            return new CSVRecordReader();
        } else if (extension == ".rdf" || extension == ".xml") {
            return new RDFXMLRecordReader();
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }
}
