package com.csd.examples.common.filters.sources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.examples.common.metrics.PipelineContext;
import com.csd.examples.common.model.TripleComponent;
import com.csd.examples.common.model.URITriple;

/**
 * Integrates Apache Jena RDF parsing with a PipedRDFIterator to bridge
 * Jena's push-based parsing with the pipeline's pull-based batching.
 */
public class UriTripleBatchSourceSupplier implements Supplier<List<URITriple>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UriTripleBatchSourceSupplier.class);

    private final FileIterator fileIterator;
    private final int batchSize;
    private final boolean flushOnFileBoundary;
    private final PipelineContext metricsContext;

    private Path currentFile = null;
    private long currentFileStartTime = 0;
    private boolean exhausted = false;

    // Jena Piped stream components for thread-safe pull parsing
    private PipedRDFIterator<Triple> currentTripleIterator = null;

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
    }

    @Override
    public List<URITriple> get() {
        if (exhausted) {
            return null; // Signals terminal EOS
        }

        List<URITriple> batch = new ArrayList<>(batchSize);

        try {
            while (batch.size() < batchSize) {
                // If we don't have an active iterator, or the current one is empty, load the next file
                if (currentTripleIterator == null || !currentTripleIterator.hasNext()) {
                    
                    // 1. Finalize the previous file if it existed
                    if (currentFile != null) {
                        long duration = System.nanoTime() - currentFileStartTime;
                        LOGGER.info("File {} processed in {} ns", currentFile.getFileName(), duration);
                        if (metricsContext != null) {
                            metricsContext.recordFileCompletion(currentFile.toString(), duration);
                        }
                        currentFile = null;

                        // Flush partial batch if boundary flag is set
                        if (flushOnFileBoundary && !batch.isEmpty()) {
                            return recordAndDispatch(batch);
                        }
                    }

                    // 2. Check for next file
                    if (!fileIterator.hasNext()) {
                        exhausted = true;
                        break; // Break loop to return whatever is left in the batch
                    }

                    // 3. Setup next file
                    currentFile = fileIterator.next();
                    currentFileStartTime = System.nanoTime();
                    LOGGER.info("Processing file: {}", currentFile);
                    startParserThread(currentFile);
                }

                // Safely pull the next triple from the background parsing thread
                if (currentTripleIterator.hasNext()) {
                    batch.add(convertTriple(currentTripleIterator.next()));
                }
            }

        } catch (Exception e) {
            LOGGER.error("[ERROR] Failure processing parsing sequence at file: " + currentFile, e);
            exhausted = true;
            return batch.isEmpty() ? null : recordAndDispatch(batch);
        }

        return batch.isEmpty() ? null : recordAndDispatch(batch);
    }

    private void startParserThread(Path file) {
        // Buffer size for the pipe. batchSize * 2 ensures smooth thread handoffs
        currentTripleIterator = new PipedRDFIterator<>(batchSize * 2);
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