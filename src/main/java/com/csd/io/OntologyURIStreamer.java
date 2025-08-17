package com.csd.io;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.io.URIStreamer;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.model.uri.URITriple;

import java.util.function.Consumer;

public class OntologyURIStreamer implements URIStreamer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyURIStreamer.class);

    @Override
    public void stream(String filePath, Consumer<URITriple> processor) {
        StreamRDF stream = new StreamRDFBase() {
            @Override
            public void triple(Triple triple) {
                URITriple uriTriple = createTriple(triple);
                processor.accept(uriTriple);
            }
        };

        try {
            RDFParser.source(filePath)
                     .lang(RDFLanguages.filenameToLang(filePath))
                     .parse(stream);
        } catch (Exception e) {
            LOGGER.error("[ERROR] Failed to parse RDF file: " + filePath);
            e.printStackTrace(System.err);
        }
    }

    private URITriple createTriple(Triple triple) {
        return new URITriple(
            nodeToComponent(triple.getSubject(), TripleComponent.Role.SUBJECT),
            nodeToComponent(triple.getPredicate(), TripleComponent.Role.PREDICATE ),
            nodeToComponent(triple.getObject(), TripleComponent.Role.OBJECT)
        );
    }

    private TripleComponent nodeToComponent(Node node, TripleComponent.Role role) {
        TripleComponent.Kind kind;

        if (node.isURI()) {
            kind = TripleComponent.Kind.IRI;
            return new TripleComponent(node.getURI(), kind, role);
        } else if (node.isBlank()) {
            kind = TripleComponent.Kind.BLANK_NODE;
            return new TripleComponent("_:b" + node.getBlankNodeId().getLabelString(), kind, role);
        } else if (node.isLiteral()) {
            kind = TripleComponent.Kind.LITERAL;
            String value = node.getLiteralLexicalForm();

            if (node.getLiteralLanguage() != null && !node.getLiteralLanguage().isEmpty()) {
                value = "\"" + escape(value) + "\"" + "@" + node.getLiteralLanguage();
            } else if (node.getLiteralDatatypeURI() != null) {
                value = "\"" + escape(value) + "\"" + "^^" + node.getLiteralDatatypeURI();
            } else {
                value = "\"" + escape(value) + "\"";
            }

            return new TripleComponent(value, kind, role);
        }

        kind = TripleComponent.Kind.UNKNOWN;
        return new TripleComponent(node.toString(), kind, role);
    }
    
    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    @Override
    public void close() throws Exception {
        
    }
}