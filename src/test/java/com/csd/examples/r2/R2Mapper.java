package com.csd.examples.r2;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.api.StreamEnvironment; 
import com.csd.core.execution.VirtualThreadGraphExecutor;
import com.csd.examples.common.filters.operations.WindowedChunkMerger;
import com.csd.examples.common.filters.sinks.BoundedMemorySortSink;
import com.csd.examples.common.filters.sources.UriTripleBatchSourceSupplier;
import com.csd.examples.common.metrics.PipelineContext;
import com.csd.examples.common.metrics.PipelineMetricsReportWriter;
import com.csd.examples.common.model.TripleComponent;

public class R2Mapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(R2Mapper.class);

    @Test
    public void testMultiBranchPipelineWithIsolatedDiskMerges() throws Exception {
        Path tempWorkspace = Files.createTempDirectory("r2_workspace_");
        
        try {
            PipelineContext metricsContext = new PipelineContext();
            Path inputDataFile = Paths.get("src", "test", "resources", "data.xml").toAbsolutePath();
            Path finalEntitiesFile = Paths.get("results", "r2",  "global_entities_mappings.dat");
            Path finalPredicatesFile = Paths.get("results", "r2", "global_predicates_mappings.dat");
            Path entityWorkDir = Files.createTempDirectory(tempWorkspace, "entities_chunks");
            
            Files.createDirectories(finalEntitiesFile.getParent());

            // --- BRANCH A: ENTITIES WORKSPACE SETUP ---
            WindowedChunkMerger entityMerger = new WindowedChunkMerger(entityWorkDir, "ENTITY", metricsContext);
            BoundedMemorySortSink entitySink = new BoundedMemorySortSink(entityMerger, 200_000);

            // --- BRANCH B: PREDICATES WORKSPACE SETUP ---
            Path predicateWorkDir = Files.createTempDirectory(tempWorkspace, "predicates_chunks");
            WindowedChunkMerger predicateMerger = new WindowedChunkMerger(predicateWorkDir, "PREDICATE", metricsContext);
            BoundedMemorySortSink predicateSink = new BoundedMemorySortSink(predicateMerger, 100_000); // Predicates are normally smaller

            StreamEnvironment graph = new StreamEnvironment(); 
            VirtualThreadGraphExecutor executor = new VirtualThreadGraphExecutor();

            UriTripleBatchSourceSupplier sourceSupplier = new UriTripleBatchSourceSupplier(
                inputDataFile, "*.ttl", 25_000, false, metricsContext
            );

            // Establish the shared pipeline root source node
            var sharedSourceStream = graph.fromSource(sourceSupplier);

            // ==================== BRANCH 1: ENTITIES PIPELINE ====================
            sharedSourceStream
                .flatMap(triple -> {
                    List<TripleComponent> entities = new ArrayList<>(2);
                    if (triple.getSubject() != null) entities.add(triple.getSubject());
                    if (triple.getObject() != null && triple.getObject().getKind() == TripleComponent.Kind.IRI) {
                        entities.add(triple.getObject());
                    }
                    return entities;
                })
                .sink(entitySink);

            // ==================== BRANCH 2: PREDICATES PIPELINE ====================
            sharedSourceStream
                .flatMap(triple -> {
                    List<TripleComponent> predicates = new ArrayList<>(1);
                    if (triple.getPredicate() != null) predicates.add(triple.getPredicate());
                    return predicates;
                })
                .sink(predicateSink);

            // --- EXECUTION PHASE ---
            long pipelineStartTime = System.nanoTime();
            executor.execute(graph); // This runs all branches using Virtual Threads
            
            // Critical Flush Fix: Dump trailing in-memory buffer crumbs down to disk
            entitySink.flushRemaining();
            predicateSink.flushRemaining();
            
            long pipelineDuration = System.nanoTime() - pipelineStartTime;
            LOGGER.info("Streaming complete in {} ms. Initiating parallel K-Way merges...", pipelineDuration / 1_000_000.0);

            // --- MERGING & OUTPUT GENERATION ---
            // Execution on main lane sequentially or inside an async thread block if preferred
            LOGGER.info("Serializing final entities file to: {}", finalEntitiesFile.toAbsolutePath());
            entityMerger.executeFinalMergePass(finalEntitiesFile);

            LOGGER.info("Serializing final predicates file to: {}", finalPredicatesFile.toAbsolutePath());
            predicateMerger.executeFinalMergePass(finalPredicatesFile);

            // Telemetry Report Dump
            PipelineMetricsReportWriter reportWriter = new PipelineMetricsReportWriter();
            reportWriter.generateExecutionReports(metricsContext);
            
            LOGGER.info("Dual Branch R1 Configuration Completed Successfully.");
            
        } finally {
            // Workspace Clean Sweep
            Files.walk(tempWorkspace)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }
}