package com.csd.core.io.readers;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import com.csd.core.model.URITriple;

import java.io.InputStream;
import java.util.function.Consumer;

public class OntologyURIReader implements URIReader {

    @Override
    public void read(String filePath, Consumer<URITriple> processor) {
        // Use Jena's streaming API for memory efficiency
        StreamRDF stream = new StreamRDF() {
            @Override
            public void start() {}
            
            @Override
            public void triple(Triple triple) {
                processor.accept(createTriple(triple));
            }
            
            @Override
            public void quad(Quad quad) {
                // Handle named graphs if needed
            }
            
            @Override
            public void base(String base) {}
            
            @Override
            public void prefix(String prefix, String iri) {}
            
            @Override
            public void finish() {}
        };
        
        // Auto-closing stream with proper error handling
        try (InputStream in = RDFDataMgr.open(filePath)) {
            RDFDataMgr.parse(stream, in, null);
        } catch (Exception e) {
            throw new RuntimeException("Error reading RDF file: " + filePath, e);
        }
    }

    private URITriple createTriple(Triple triple) {
        return new URITriple(
            nodeToString(triple.getSubject()),
            nodeToString(triple.getPredicate()),
            nodeToString(triple.getObject())
        );
    }

    private String nodeToString(Node node) {
        if (node.isURI()) {
            return node.getURI();
        } else if (node.isBlank()) {
            return "_:b" + node.getBlankNodeId().getLabelString();
        } else if (node.isLiteral()) {
            return literalToString(node.getLiteral());
        }
        throw new IllegalArgumentException("Unsupported node type: " + node);
    }

    private String literalToString(Object literal) {
        if (literal instanceof Literal) {
            Literal lit = (Literal) literal;
            StringBuilder sb = new StringBuilder("\"")
                .append(lit.getLexicalForm().replace("\"", "\\\""))
                .append("\"");
            
            if (lit.getDatatype() != null) {
                sb.append("^^").append(lit.getDatatypeURI());
            } else if (lit.getLanguage() != null && !lit.getLanguage().isEmpty()) {
                sb.append("@").append(lit.getLanguage());
            }
            return sb.toString();
        }
        return literal.toString();
    }
}