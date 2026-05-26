package com.csd.examples.common.filters.sources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.api.functions.AdaptiveBatchSupplier;
import com.csd.examples.common.metrics.PipelineContext;
import com.csd.examples.common.model.TripleComponent;
import com.csd.examples.common.model.URITriple;

public class UriTripleBatchSourceSupplier implements AdaptiveBatchSupplier<URITriple> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UriTripleBatchSourceSupplier.class);

    // Jena cannot go faster
    private static final int PARSER_PIPE_BUFFER_SIZE = 2_000_000;

    private final FileIterator fileIterator;
    private final boolean flushOnFileBoundary;
    private final PipelineContext metricsContext;

    private Path currentFile = null;
    private long currentFileStartTime = 0;
    private boolean exhausted = false;
    private PipedRDFIterator<Triple> currentTripleIterator = null;

    public UriTripleBatchSourceSupplier(Path path,
                                         String globPattern,
                                         boolean flushOnFileBoundary,
                                         PipelineContext metricsContext) throws IOException {
        this.fileIterator = new FileIterator(path, globPattern == null ? "*" : globPattern);
        this.flushOnFileBoundary = flushOnFileBoundary;
        this.metricsContext = metricsContext;
    }

    @Override
    public List<URITriple> getBatch(int targetSize) {
        if (exhausted) {
            return null; 
        }

        List<URITriple> batch = new ArrayList<>(targetSize);

        try {
            while (batch.size() < targetSize) {
                if (currentTripleIterator == null || !currentTripleIterator.hasNext()) {
                    
                    if (currentFile != null) {
                        long duration = System.nanoTime() - currentFileStartTime;
                        LOGGER.info("File {} processed in {} ns", currentFile.getFileName(), duration);
                        if (metricsContext != null) {
                            metricsContext.recordFileCompletion(currentFile.toString(), duration);
                        }
                        currentFile = null;

                        if (flushOnFileBoundary && !batch.isEmpty()) {
                            return recordAndDispatch(batch);
                        }
                    }

                    if (!fileIterator.hasNext()) {
                        exhausted = true;
                        break; 
                    }

                    currentFile = fileIterator.next();
                    currentFileStartTime = System.nanoTime();
                    LOGGER.info("Processing file: {}", currentFile);
                    startParserThread(currentFile);
                }

                if (currentTripleIterator.hasNext()) {
                    batch.add(convertTriple(currentTripleIterator.next()));
                }
            }

        } catch (Exception e) {
            LOGGER.error("[ERROR] Failure processing adaptive parsing sequence at: " + currentFile, e);
            exhausted = true;
            return batch.isEmpty() ? null : recordAndDispatch(batch);
        }

        return batch.isEmpty() ? null : recordAndDispatch(batch);
    }

    private void startParserThread(Path file) {
        // Initializing the Jena pipe iterator using a dedicated look-ahead buffer capacity.
        // The parser thread will cleanly fill this buffer up to 100,000 triples ahead,
        // blocking only if the DAG engine falls behind.
        currentTripleIterator = new PipedRDFIterator<>(PARSER_PIPE_BUFFER_SIZE);
        PipedRDFStream<Triple> inputStream = new PipedTriplesStream(currentTripleIterator);

        Thread parserThread = new Thread(() -> {
            try {
                String filePathStr = file.toString();
                RDFParser.source(filePathStr)
                         .lang(RDFLanguages.filenameToLang(filePathStr))
                         .parse(inputStream);
            } catch (Exception e) {
                LOGGER.error("Parsing failed for file: " + file, e);
            } finally {
                inputStream.finish();
            }
        });

        parserThread.setName("RDF-Parser-" + file.getFileName());
        parserThread.setDaemon(true);
        parserThread.start();
    }

    private List<URITriple> recordAndDispatch(List<URITriple> batch) {
        if (metricsContext != null && !batch.isEmpty()) {
            metricsContext.incrementBatches();
            metricsContext.addTriples(batch.size());
        }
        return batch;
    }

    private URITriple convertTriple(Triple triple) {
        return new URITriple(
            nodeToComponent(triple.getSubject(), TripleComponent.Role.SUBJECT),
            nodeToComponent(triple.getPredicate(), TripleComponent.Role.PREDICATE),
            nodeToComponent(triple.getObject(), TripleComponent.Role.OBJECT)
        );
    }

    private TripleComponent nodeToComponent(Node node, TripleComponent.Role role) {
        String value;
        TripleComponent.Kind kind;
        if (node.isURI()) {
            kind = TripleComponent.Kind.IRI;
            return new TripleComponent(node.getURI(), kind, role);
        } else if (node.isBlank()) {
            kind = TripleComponent.Kind.BLANK_NODE;
            return new TripleComponent("_:b" + node.getBlankNodeId().getLabelString(), kind, role);
        } else if (node.isLiteral()) {
            kind = TripleComponent.Kind.LITERAL;
            value = node.getLiteralLexicalForm();
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