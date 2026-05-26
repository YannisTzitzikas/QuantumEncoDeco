package com.csd.examples.r1;

import java.nio.file.*;
import java.util.*;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.api.StreamEnvironment; // Matches your StreamEnvironment/TaskGraph API
import com.csd.examples.common.filters.operations.WindowedChunkMerger;
import com.csd.examples.common.filters.sinks.BoundedMemorySortSink;
import com.csd.examples.common.filters.sources.UriTripleBatchSourceSupplier;
import com.csd.examples.common.metrics.PipelineContext;
import com.csd.examples.common.metrics.PipelineMetricsReportWriter;
import com.csd.examples.common.model.TripleComponent;

public class R1Mapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(R1Mapper.class);

    @Test
    public void testR1KWayMergeMap() throws Exception {

        // Telemetry
        PipelineContext metricsContext = new PipelineContext();

        // Directories
        Path tempWorkspace = Files.createTempDirectory("r1_test_workspace_");
        Path inputDataFile = Paths.get("src", "test", "resources", "data.xml").toAbsolutePath();
        Path finalMappingFile = Paths.get("results/r1/global_mappings.dat");
        Path entityWorkDir = Files.createTempDirectory(tempWorkspace, "streaming_entities");

        // Ensure standard outputs path is ready
        Files.createDirectories(finalMappingFile.getParent());

        WindowedChunkMerger entityMerger = new WindowedChunkMerger(entityWorkDir, "ENTITY", metricsContext);
        BoundedMemorySortSink memorySink = new BoundedMemorySortSink(entityMerger, 200_000);
        UriTripleBatchSourceSupplier sourceSupplier = new UriTripleBatchSourceSupplier(
            inputDataFile, "*.ttl",  true, metricsContext
        );
        
        StreamEnvironment graph = new StreamEnvironment(); 
        graph.fromSource(sourceSupplier)
            .flatMap(triple -> {
                List<TripleComponent> components = new ArrayList<>(3);
                if (triple.getSubject() != null) components.add(triple.getSubject());
                if (triple.getPredicate() != null) components.add(triple.getPredicate());
                if (triple.getObject() != null && triple.getObject().getKind() == TripleComponent.Kind.IRI) {
                    components.add(triple.getObject());
                }
                return components;
            })
            .sink(memorySink);

        long pipelineStartTime = System.nanoTime();
        graph.execute();
        long pipelineDuration = System.nanoTime() - pipelineStartTime;

        memorySink.flushRemaining();

        // Terminal K-Way Merge Pass + Numbering on second column
        LOGGER.info("Data streaming completed. Executing terminal K-Way merge pass...");
        entityMerger.executeFinalMergePass(finalMappingFile);

        // Print Execution Summary and Historical Metrics
        PipelineMetricsReportWriter reportWriter = new PipelineMetricsReportWriter();
        reportWriter.generateExecutionReports(metricsContext);

        tempWorkspace.toFile().delete();
        
        LOGGER.info("R1 Pipeline Execution Complete. Pipeline duration: {} ms", pipelineDuration / 1_000_000.0);
    }
}