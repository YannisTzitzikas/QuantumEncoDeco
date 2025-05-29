package Ewritters.triple;

import org.apache.jena.rdf.model.*;
import java.io.IOException;
import java.io.OutputStream;

public class RDFXMLTripleWriter implements ITripleWriter {
    private final Model model;
    private final OutputStream out;

    public RDFXMLTripleWriter(OutputStream out) {
        this.model = ModelFactory.createDefaultModel();
        this.out = out;
    }

    @Override
    public void write(String subject, String predicate, String object) throws IOException {
        Resource subjectRes = model.createResource(subject);
        Property predicateProp = model.createProperty(predicate);
        RDFNode objectNode = createNode(model, object);
        model.add(subjectRes, predicateProp, objectNode);
    }

    private RDFNode createNode(Model model, String value) {
        // Same as TTLWriter
        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("urn:")) {
            return model.createResource(value);
        } else if (value.contains("^^")) {
            String[] parts = value.split("\\^\\^", 2);
            return model.createTypedLiteral(parts[0], parts[1]);
        } else if (value.contains("@")) {
            int idx = value.lastIndexOf('@');
            return model.createLiteral(value.substring(0, idx), value.substring(idx + 1));
        } else {
            return model.createLiteral(value);
        }
    }

    @Override
    public void close() throws IOException {
        model.write(out, "RDF/XML");
        out.close();
    }
}