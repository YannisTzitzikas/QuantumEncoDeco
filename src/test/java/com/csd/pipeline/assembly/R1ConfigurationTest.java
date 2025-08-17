package com.csd.pipeline.assembly;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.model.uri.URITriple;
import com.csd.core.pipeline.Pipe;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.storage.StorageEngine;
import com.csd.pipeline.filters.BasisEncoderFilter;
import com.csd.pipeline.filters.ComponentRemoverFilter;
import com.csd.pipeline.filters.MapStoreFilterVoid;
import com.csd.pipeline.filters.TripleComponentExtractorFilter;
import com.csd.pipeline.pumps.UriTripleBatchPump;
import com.csd.pipeline.sinks.StorageExportSink;
import com.csd.storage.StorageEngineFactory;

public class R1ConfigurationTest {
    static class InMemoryPipe<T> implements Pipe<T> {
        private final BlockingQueue<Message<T>> q;
        
        public InMemoryPipe(){q = new LinkedBlockingQueue<>();}
        public InMemoryPipe(int capacity){q = new LinkedBlockingQueue<>(capacity);}
        @Override public void send(Message<T> msg) throws InterruptedException { q.put(msg); }
        @Override public Message<T> receive() throws InterruptedException { return q.take(); }
    }

    @Test
    public void testR1Configuration() throws Exception {
        // Create test data directory and file
        Path tempDir = Files.createTempDirectory("r1test");
        Path testFile  = Paths.get("C:\\Users\\User\\Downloads\\CIDOC_CRM_v7.1.1.rdfs.xml");

        // Setup pipes
        InMemoryPipe<List<URITriple>> pipe1 = new InMemoryPipe<>();
        InMemoryPipe<Set<TripleComponent>> pipe2 = new InMemoryPipe<>();
        InMemoryPipe<Set<TripleComponent>> pipe3 = new InMemoryPipe<>();
        InMemoryPipe<Map<String, Integer>> pipe4 = new InMemoryPipe<>();
        InMemoryPipe<Void> pipe5 = new InMemoryPipe<>();
        
        // Create storage and pre-seed with "existing"
        StorageEngine storage = StorageEngineFactory.rocks(tempDir.toString());
        
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
        storeBindings.bindOutput(MapStoreFilterVoid.OUT, pipe5);

        PortBindings sinkBindings = new PortBindings();
        sinkBindings.bindInput(StorageExportSink.IN, pipe5);
        
        // Create components
        UriTripleBatchPump pump = new UriTripleBatchPump(
            testFile, 
            "*.ttl", 
            1000, 
            pumpBindings
        );
        
        TripleComponentExtractorFilter extractor = 
            new TripleComponentExtractorFilter(extractorBindings);
        
        ComponentRemoverFilter remover = 
            new ComponentRemoverFilter(removerBindings, storage);
        
        BasisEncoderFilter basis = 
            new BasisEncoderFilter(basisBindings);
        
        MapStoreFilterVoid store = 
            new MapStoreFilterVoid(storeBindings, storage);

        StorageExportSink sink = 
            new StorageExportSink(sinkBindings,storage);
        
        // Run pipeline components in threads
        Thread pumpThread = new Thread(pump);
        Thread extractorThread = new Thread(extractor);
        Thread removerThread = new Thread(remover);
        Thread basisThread = new Thread(basis);
        Thread storeThread = new Thread(store);
        Thread sinkThread = new Thread(sink);
        
        pumpThread.start();
        extractorThread.start();
        removerThread.start();
        basisThread.start();
        storeThread.start();
        
        // Wait for EOS to propagate
        
        pumpThread.join();
        extractorThread.join();
        removerThread.join();
        basisThread.join();
        storeThread.join();
        sinkThread.join();
    }
}