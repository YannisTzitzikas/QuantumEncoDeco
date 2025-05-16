package com.ics.io.readers;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import com.ics.model.Record;

import java.io.InputStream;
import java.util.function.Consumer;

public class RDFXMLRecordReader implements RecordReader {

    public void readRecord(String filePath, Consumer<Record> processor) {
        Model model = ModelFactory.createDefaultModel();
    
        try (InputStream in = RDFDataMgr.open(filePath)) {
            model.read(in, null);
    
            // Process statements in parallel
            model.listStatements().toList().stream()
                .map(stmt -> new Record(
                    stmt.getSubject().toString(),
                    stmt.getPredicate().toString(),
                    stmt.getObject().toString()))
                .forEach(processor);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
