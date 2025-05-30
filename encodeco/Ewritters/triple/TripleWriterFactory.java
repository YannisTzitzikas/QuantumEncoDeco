package Ewritters.triple;

import java.io.OutputStream;

public class TripleWriterFactory {
    public static ITripleWriter getWriter(String type, OutputStream out) {

        if ("csv".equalsIgnoreCase(type)) {
            return new CSVTripleWriter(out);
        } else if ("ttl".equalsIgnoreCase(type)) {
            return new TTLTripleWriter(out);
        } else if ("rdf".equalsIgnoreCase(type)) {
            return new RDFXMLTripleWriter(out);
        } else {
            throw new IllegalArgumentException("Unsupported encoder type: " + type);
        }
    }
}