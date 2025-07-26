package com.csd.core.io.readers;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

import com.csd.core.model.URITriple;

import java.util.function.Consumer;

public class OntologyURIReader implements URIReader {

    @Override
    public void read(String filePath, Consumer<URITriple> processor) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, filePath);
        
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            URITriple triple = createTriple(stmt.asTriple());
            processor.accept(triple);
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