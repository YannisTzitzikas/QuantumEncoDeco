package com.csd.common.io.readers;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;

import com.csd.core.model.uri.URITriple;

import java.util.function.Consumer;

public class OntologyURIReader implements URIReader {

    @Override
    public void stream(String filePath, Consumer<URITriple> processor) {
        StreamRDF stream = new StreamRDFBase() {
            @Override
            public void triple(Triple triple) {
                URITriple uriTriple = createTriple(triple);
                processor.accept(uriTriple);
            }
        };

        RDFParser.source(filePath)
                 .lang(RDFLanguages.filenameToLang(filePath))
                 .parse(stream);
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