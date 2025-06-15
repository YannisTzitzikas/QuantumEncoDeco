package Breaders;

import Ctransformers.StatisticsCollector;
import Ctransformers.TripleComponent;
import Ctransformers.URIFactory;
import Ctransformers.URITriple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import java.util.ArrayList;
import java.util.List;

public class OntologyReader {
    private final StatisticsCollector stats;
    
    public OntologyReader(StatisticsCollector stats) {
        this.stats = stats;
    }

    public List<URITriple> readTriplesFromPath(String filename) {
        List<URITriple> triples = new ArrayList<>();
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, filename);
        
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            URITriple triple = createTriple(stmt);
            triples.add(triple);
            
            // Update statistics using canonical URIs
            stats.recordTriple();
            stats.recordComponent(triple.getSubject(), TripleComponent.SUBJECT);
            stats.recordComponent(triple.getPredicate(), TripleComponent.PREDICATE);
            stats.recordComponent(triple.getObject(), TripleComponent.OBJECT);
        }
        return triples;
    }

    private URITriple createTriple(Statement stmt) {
        Resource subjectRes = stmt.getSubject();
        Property predicateProp = stmt.getPredicate();
        RDFNode objectNode = stmt.getObject();
        
        // Use URIFactory for canonical URIs
        String subject = URIFactory.getURI(subjectRes.getURI());
        String predicate = URIFactory.getURI(predicateProp.getURI());
        
        String object;
        if (objectNode.isResource()) {
            object = URIFactory.getURI(objectNode.asResource().getURI());
        } else {
            Literal literal = objectNode.asLiteral();
            object = literal.getLexicalForm();
            
            // Handle typed literals efficiently
            if (literal.getDatatype() != null) {
                object = object + "^^" + URIFactory.getURI(literal.getDatatypeURI());
            } else if (literal.getLanguage() != null && !literal.getLanguage().isEmpty()) {
                object = object + "@" + literal.getLanguage();
            }
        }
        
        // checking if all components are not null
        if ((subject==null) || (predicate==null) || (object==null)) {
        	new IllegalArgumentException("Cannot create triple because one of s,p,o is null ");
        }

        return new URITriple(subject, predicate, object);
    }
}