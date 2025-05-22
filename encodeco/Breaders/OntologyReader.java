package Breaders;

import Ctransformers.URIFactory;
import Ctransformers.URITriple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import java.util.ArrayList;
import java.util.List;

public class OntologyReader {
    
    /**
     * Reads an RDF file and returns a list of URITriples
     */
    public List<URITriple> readTriplesFromPath(String filename) {
        List<URITriple> triples = new ArrayList<>();
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, filename);
        
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            triples.add(createTriple(stmt));
        }
        return triples;
    }

    private URITriple createTriple(Statement stmt) {
        String subject = URIFactory.getURI(stmt.getSubject().getURI());
        String predicate = URIFactory.getURI(stmt.getPredicate().getURI());
        
        RDFNode objectNode = stmt.getObject();
        String object = objectNode.isResource() 
            ? URIFactory.getURI(objectNode.asResource().getURI())
            : objectNode.asLiteral().getLexicalForm();

        return new URITriple(subject, predicate, object);
    }

}