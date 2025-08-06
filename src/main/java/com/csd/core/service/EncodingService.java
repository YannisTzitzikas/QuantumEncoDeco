package com.csd.core.service;

import com.csd.core.codex.encoder.EncoderFactory;
import com.csd.core.codex.encoder.IEncoder;
import com.csd.core.config.Config;
import com.csd.core.io.readers.URIReader;
import com.csd.core.io.readers.URIReaderFactory;
import com.csd.core.model.*;
import com.csd.core.model.JobContext.JobType;
import com.csd.core.stats.StatisticsCollector;
import com.csd.core.storage.StorageEngine;
import com.csd.core.storage.StorageEngineFactory;
import com.csd.core.storage.StorageException;
import com.csd.core.utils.fs.FileIterator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodingService {
    private static final Logger logger      = LoggerFactory.getLogger(EncodingService.class);

    private final Config config;
    private final StorageEngine storageEngine;
    private final StatisticsCollector statCollector;
    private final BufferedWriter outputWriter;
    private final IEncoder<?> encoder;

    public EncodingService(Config config) throws Exception {
        this.config = config;
        this.storageEngine = StorageEngineFactory.getStorageEngine(config.getStorageBackend(), config.getStoragePath().toString());
        this.encoder = EncoderFactory.getEncoder(config.getEncoding());
        this.statCollector = new StatisticsCollector();
        this.outputWriter = Files.newBufferedWriter(config.getOutputPath(),  
                                                    StandardOpenOption.CREATE,
                                                    StandardOpenOption.TRUNCATE_EXISTING,
                                                    StandardOpenOption.WRITE);

        initializeEncoder();
    }

    private void initializeEncoder() {
        JobContext context = new JobContext(
            JobType.ENCODING,
            storageEngine,
            config.getParameters(),
            statCollector
        );

        encoder.setContext(context);
        logger.info("Initialized {} encoder", encoder.getInfo().getName());
    }

    public void execute() throws Exception {
        logger.info("Starting encoding process");
        JobContext context = encoder.getContext();
        
        long startTime = System.nanoTime();


        try {
            if (encoder.getInfo().isTwoPass()) {
                logger.info("Starting first pass (two-pass encoding)");
                context.setStatus(JobContext.JobStatus.RUNNING);
                processBatches(true);
                context.setStatus(JobContext.JobStatus.DONE);
                logger.info("First pass completed");
            }

            long endTime = System.nanoTime();
            long durationInNs = endTime - startTime;
            System.out.println("First Pass took " + durationInNs / 1_000_000 + " ms");

            logger.info("Starting final encoding pass");
            startTime = System.nanoTime();
            processBatches(false);
            endTime = System.nanoTime();
            durationInNs = endTime - startTime;
            System.out.println("Second Pass took " + durationInNs / 1_000_000 + " ms");

            logger.info("Final encoding pass completed");

            exportMappings();
        } finally {
            storageEngine.close();
        }
    }

    private void processBatches(boolean isFirstPass) throws Exception {
        FileIterator fileIterator = new FileIterator(config.getInputPath(), config.getFileFilterPattern());
        List<URITriple> batch = new ArrayList<>(config.getBatchSize());
    
        while (fileIterator.hasNext()) {
            Path file = fileIterator.next();
            URIReader reader = URIReaderFactory.getReader(file.toString());
    
            long startTime = System.nanoTime();
    
            reader.stream(file.toString(), triple -> {
                batch.add(triple);
                statCollector.recordTriple(); // Count each triple
                if (batch.size() >= config.getBatchSize()) {
                    processBatch(batch, isFirstPass);
                    statCollector.recordBatch(); // Count each batch
                    logger.info("Batch no {} finished\n", statCollector.getBatchCount());
                    batch.clear();
                }
            });
    
            if (!batch.isEmpty()) {
                processBatch(batch, isFirstPass);
                statCollector.recordBatch();
                batch.clear();
            }
    
            long endTime = System.nanoTime();
            statCollector.recordFile(file.toString());  // File stats
            statCollector.addTime(endTime - startTime); // Time per file

            logger.info("File no {}, named {}, finished at {} ms", statCollector.getFileCount(), file.toString(), (endTime - startTime)/1_000_000.0);
        }
    }


    private void processBatch(List<URITriple> batch, boolean isFirstPass) {
        for (URITriple triple : batch) {
            if (isFirstPass) {
                processTripleFirstPass(triple);
            } else {
                processTripleFinalEncoding(triple);
            }
        }
    }

    private void processTripleFirstPass(URITriple triple) {
        encoder.encode(new EncodingData(triple.getSubject(), TripleComponent.SUBJECT));
        encoder.encode(new EncodingData(triple.getPredicate(), TripleComponent.PREDICATE));
        encoder.encode(new EncodingData(triple.getObject(), TripleComponent.OBJECT));
    }

    private void processTripleFinalEncoding(URITriple triple) {
        String subjectEncoding   = encoder.getFinalEncoding(new EncodingData(triple.getSubject(), TripleComponent.SUBJECT));
        String predicateEncoding = encoder.getFinalEncoding(new EncodingData(triple.getPredicate(), TripleComponent.PREDICATE));
        String objectEncoding    = encoder.getFinalEncoding(new EncodingData(triple.getObject(), TripleComponent.OBJECT));

        try {
            outputWriter.write(subjectEncoding + predicateEncoding + objectEncoding);
            outputWriter.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write encoded triple to output file", e);
        }
    }


    private void exportMappings() throws StorageException {
        String mappingsPath = config.getMappingsPath().toString();
        logger.info("Exporting mappings to {}", mappingsPath);
    }

}