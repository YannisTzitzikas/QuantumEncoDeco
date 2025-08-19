package com.csd.pipeline.assembly;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.event.AsyncEventBus;
import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.model.uri.URITriple;
import com.csd.core.pipeline.Pipe;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.storage.StorageEngine;
import com.csd.metrics.FileMetrics;
import com.csd.metrics.PipelineMetrics;
import com.csd.metrics.UriTripleMetricsWithCSV;
import com.csd.metrics.writers.FileMetricsWriter;
import com.csd.metrics.writers.PipelineMetricsWriter;
import com.csd.metrics.writers.UriTripleMetricsWriter;
import com.csd.pipeline.filters.BasisEncoderFilter;
import com.csd.pipeline.filters.ComponentRemoverFilter;
import com.csd.pipeline.filters.MapStoreFilterVoid;
import com.csd.pipeline.filters.TripleComponentExtractorFilter;
import com.csd.pipeline.pumps.UriTripleBatchPump;
import com.csd.pipeline.sinks.StorageExportSink;
import com.csd.pipeline.sinks.R1UriTripleFileSink;
import com.csd.storage.StorageEngineFactory;

public class R1ConfigurationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(R1ConfigurationTest.class);

    static class InMemoryPipe<T> implements Pipe<T> {
        private final BlockingQueue<Message<T>> q;

        public InMemoryPipe() {
            q = new LinkedBlockingQueue<>();
        }

        public InMemoryPipe(int capacity) {
            q = new LinkedBlockingQueue<>(capacity);
        }

        @Override
        public void send(Message<T> msg) throws InterruptedException {
            q.put(msg);
        }

        @Override
        public Message<T> receive() throws InterruptedException {
            return q.take();
        }
    }
    
    @Test
    public void testR1ConfigurationSmall() throws Exception {
        testR1Configuration(Paths.get("C:\\Users\\User\\Desktop\\dataset\\bigTest\\test\\test2"), Paths.get("resluts\\small_test.r1"), "resluts\\small_test.r1.map");
    }

    public void testR1ConfigurationMid() throws Exception {
        testR1Configuration(Paths.get("C:\\Users\\User\\Desktop\\dataset\\bigTest\\newtest"), Paths.get("resluts\\mid_test.r1"), "resluts\\mid_test.r1.map");
    }

    public void testR1ConfigurationBig() throws Exception {
        testR1Configuration(Paths.get("C:\\Users\\User\\Desktop\\dataset\\bigTest"), Paths.get("resluts\\big_test.r1"), "resluts\\big_test.r1.map");
    }


    private void testR1Configuration(Path testPath, Path output, String mapFileName) throws Exception {
        // Create test data directory and file
        // Create executor for event bus and pipeline components
        ExecutorService componentExecutor = Executors.newFixedThreadPool(10); 
        ExecutorService eventExecutor = Executors.newCachedThreadPool();

        AsyncEventBus bus = new AsyncEventBus(eventExecutor);
        
        // Setup pipes
        InMemoryPipe<List<URITriple>> pipe1 = new InMemoryPipe<>();
        InMemoryPipe<Set<TripleComponent>> pipe2 = new InMemoryPipe<>();
        InMemoryPipe<Set<TripleComponent>> pipe3 = new InMemoryPipe<>();
        InMemoryPipe<Map<String, Integer>> pipe4 = new InMemoryPipe<>();
        InMemoryPipe<Void> pipe5 = new InMemoryPipe<>();
        InMemoryPipe<Void> pipe6 = new InMemoryPipe<>();

        PipelineMetrics metrics = new PipelineMetrics(bus);
        UriTripleMetricsWithCSV csvMetrics = new UriTripleMetricsWithCSV(bus, mapFileName + "uri.csv");
        FileMetrics fileMetrics = new FileMetrics(bus);

        // Create storage and pre-seed with "existing"
        StorageEngine storage = StorageEngineFactory.inMemory();

        // Bind ports for all components
        PortBindings pumpBindings = new PortBindings();
        pumpBindings.bindOutput(UriTripleBatchPump.OUT, pipe1);

        PortBindings extractorBindings = new PortBindings();
        extractorBindings.bindInput(TripleComponentExtractorFilter.IN, pipe1);
        extractorBindings.bindOutput(TripleComponentExtractorFilter.OUT, pipe2);

        PortBindings removerBindings = new PortBindings();
        removerBindings.bindInput(ComponentRemoverFilter.IN, pipe2);
        removerBindings.bindOutput(ComponentRemoverFilter.OUT, pipe3);

        PortBindings basisBindings = new PortBindings();
        basisBindings.bindInput(BasisEncoderFilter.IN, pipe3);
        basisBindings.bindOutput(BasisEncoderFilter.OUT, pipe4);

        PortBindings storeBindings = new PortBindings();
        storeBindings.bindInput(MapStoreFilterVoid.IN, pipe4);
        storeBindings.bindOutput(MapStoreFilterVoid.OUT_MAP, pipe5);
        storeBindings.bindOutput(MapStoreFilterVoid.OUT_ENCODE, pipe6);

        PortBindings sinkBindings = new PortBindings();
        sinkBindings.bindInput(StorageExportSink.IN, pipe5);

        PortBindings encodeBindings = new PortBindings();
        encodeBindings.bindInput(R1UriTripleFileSink.IN, pipe6);

        // Create components
        UriTripleBatchPump pump = new UriTripleBatchPump(
                testPath,
                "*.ttl",
                25_000,
                pumpBindings,
                bus);

        TripleComponentExtractorFilter extractor = new TripleComponentExtractorFilter(extractorBindings, bus);
        ComponentRemoverFilter remover = new ComponentRemoverFilter(removerBindings, storage, bus);
        BasisEncoderFilter basis = new BasisEncoderFilter(basisBindings, bus);
        MapStoreFilterVoid store = new MapStoreFilterVoid(storeBindings, storage, bus);
        StorageExportSink sink = new StorageExportSink(sinkBindings, storage, mapFileName ,bus);
        R1UriTripleFileSink sink2 = new R1UriTripleFileSink( testPath,"*.ttl",encodeBindings,storage,output, bus);

        // Run pipeline components in threads
        componentExecutor.submit(pump);
        componentExecutor.submit(extractor);
        componentExecutor.submit(remover);
        componentExecutor.submit(basis);
        componentExecutor.submit(store);
        componentExecutor.submit(sink);
        componentExecutor.submit(sink2);

        // Shutdown executor and wait for completion
        componentExecutor.shutdown();
        boolean finished = componentExecutor.awaitTermination(1, TimeUnit.HOURS);
        
        if (!finished) {
            LOGGER.error("Pipeline did not complete within timeout");
            componentExecutor.shutdownNow();
        }

        // Print the metrics one finale time.
        PipelineMetricsWriter writer = new PipelineMetricsWriter();
        writer.writeAllMetrics(metrics);

        FileMetricsWriter wf = new FileMetricsWriter();
        wf.writeAllMetrics(fileMetrics);

        UriTripleMetricsWriter wt = new UriTripleMetricsWriter();
        wt.writeMetrics(csvMetrics);
    }
}