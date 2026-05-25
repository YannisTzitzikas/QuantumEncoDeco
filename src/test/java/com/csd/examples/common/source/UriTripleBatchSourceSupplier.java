package com.csd.examples.common.source;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.examples.common.metrics.PipelineContext;
import com.csd.examples.common.model.TripleComponent;
import com.csd.examples.common.model.URITriple;

/**
 * Self-contained Test/Example Source Supplier.
 * Integrates the Apache Jena RDF parsing routine directly with file-system iterations
 * and inline lock-free metrics tracking, chunking records on demand for the DAG core engine.
 */
public class UriTripleBatchSourceSupplier implements Supplier<List<URITriple>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UriTripleBatchSourceSupplier.class);

    private final FileIterator fileIterator;
    private final int batchSize;
    private final boolean flushOnFileBoundary;
    private final PipelineContext metricsContext; // Replaced EventBus

    // Local accumulation buffer retained across engine pull boundaries
    private final List<URITriple> internalBuffer;
    
    private Path currentFile = null;
    private long currentFileStartTime = 0;
    private boolean exhausted = false;
    private final StreamRDF jenaStreamHook;

    public UriTripleBatchSourceSupplier(Path path,
                                         String globPattern,
                                         int batchSize,
                                         boolean flushOnFileBoundary,
                                         PipelineContext metricsContext) throws IOException {
        if (batchSize <= 0) throw new IllegalArgumentException("batchSize must be > 0");
        this.fileIterator = new FileIterator(path, globPattern == null ? "*" : globPattern);
        this.batchSize = batchSize;
        this.flushOnFileBoundary = flushOnFileBoundary;
        this.metricsContext = metricsContext;
        this.internalBuffer = new ArrayList<>(batchSize);
        
        // Pass parsed Jena triples straight into our microbatch processing buffer
        this.jenaStreamHook = new StreamRDFBase() {
            @Override
            public void triple(Triple jenaTriple) {
                internalBuffer.add(convertTriple(jenaTriple));
            }
        };
    }

    @Override
    public List<URITriple> get() {
        if (exhausted) {
            return null; // Signals terminal EOS to the virtual thread executor
        }

        try {
            while (internalBuffer.size() < batchSize) {
                if (currentFile == null) {
                    if (!fileIterator.hasNext()) {
                        if (!internalBuffer.isEmpty()) {
                            return drainAndRecordBuffer();
                        }
                        exhausted = true;
                        return null; 
                    }

                    currentFile = fileIterator.next();
                    currentFileStartTime = System.nanoTime();
                    LOGGER.info("Processing file: {}", currentFile);
                }

                String filePathStr = currentFile.toString();
                
                // Synchronously parse file records into internalBuffer
                RDFParser.source(filePathStr)
                         .lang(RDFLanguages.filenameToLang(filePathStr))
                         .parse(jenaStreamHook);

                long duration = System.nanoTime() - currentFileStartTime;
                LOGGER.info("File processed in {} ns", duration);
                
                // Direct Inline Recording replacing old async EventBus publications
                if (metricsContext != null) {
                    metricsContext.recordFileCompletion(filePathStr, duration);
                }

                currentFile = null;

                if (flushOnFileBoundary && !internalBuffer.isEmpty()) {
                    return drainAndRecordBuffer();
                }
            }

            return drainAndRecordBuffer();

        } catch (Exception e) {
            LOGGER.error("[ERROR] Failure processing parsing sequence at file: " + currentFile, e);
            exhausted = true;
            return null;
        }
    }

    /**
     * Drains the accumulator buffer while safely adding counts to the pipeline telemetry trackers
     */
    private List<URITriple> drainAndRecordBuffer() {
        List<URITriple> dispatchSnapshot = new ArrayList<>(internalBuffer);
        
        if (metricsContext != null && !dispatchSnapshot.isEmpty()) {
            metricsContext.incrementBatches();
            metricsContext.addTriples(dispatchSnapshot.size());
        }
        
        internalBuffer.clear();
        return dispatchSnapshot;
    }

    private URITriple convertTriple(Triple triple) {
        return new URITriple(
            nodeToComponent(triple.getSubject(), TripleComponent.Role.SUBJECT),
            nodeToComponent(triple.getPredicate(), TripleComponent.Role.PREDICATE),
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
}