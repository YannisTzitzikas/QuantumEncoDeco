package com.csd.core.service;

import com.csd.core.codex.encoder.EncoderFactory;
import com.csd.core.codex.encoder.IEncoder;
import com.csd.core.config.Config;
import com.csd.core.io.readers.URIReader;
import com.csd.core.io.readers.URIReaderFactory;
import com.csd.core.model.*;
import com.csd.core.storage.StorageEngine;
import com.csd.core.storage.StorageEngineFactory;
import com.csd.core.storage.StorageException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodingService {
    private static final Logger logger      = LoggerFactory.getLogger(EncodingService.class);

    private final Config config;
    private final StorageEngine storageEngine;
    private final IEncoder<?> encoder;

    public EncodingService(Config config) throws Exception {
        this.config = config;
        this.storageEngine = StorageEngineFactory.getStorageEngine(config.getStorageBackend(), config.getStoragePath().toString());
        this.encoder = EncoderFactory.getEncoder(config.getEncoding());
        initializeEncoder();
    }

    private void initializeEncoder() {
        EncodingContext context = new EncodingContext(
            storageEngine,
            config.getParameters()
        );
        encoder.setContext(context);
        logger.info("Initialized {} encoder", encoder.getInfo().getName());
    }

    public void execute() throws Exception {
        logger.info("Starting encoding process");
        EncodingContext context = encoder.getContext();
        
        try {
            if (encoder.getInfo().isTwoPass()) {
                logger.info("Starting first pass (two-pass encoding)");
                context.setStatus(EncodingContext.EncodingStatus.RUNNING);
                processBatches(true);
                context.setStatus(EncodingContext.EncodingStatus.DONE);
                logger.info("First pass completed");
            }

            logger.info("Starting final encoding pass");
            processBatches(false);
            logger.info("Final encoding pass completed");

            exportMappings();
        } finally {
            storageEngine.close();
        }
    }

    private void processBatches(boolean isFirstPass) throws Exception {
        URIReader reader = URIReaderFactory.getReader(config.getInputPath().toString());
        List<URITriple> batch = new ArrayList<>(config.getBatchSize());
        
        reader.stream(config.getInputPath().toString(), triple -> {
            batch.add(triple);
            if (batch.size() >= config.getBatchSize()) {
                processBatch(batch, isFirstPass);
                batch.clear();
            }
        });
        
        if (!batch.isEmpty()) {
            processBatch(batch, isFirstPass);
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

        System.out.println(subjectEncoding + predicateEncoding +objectEncoding );
    }

    private void exportMappings() throws StorageException {
        String mappingsPath = config.getMappingsPath().toString();
        logger.info("Exporting mappings to {}", mappingsPath);
    }

}