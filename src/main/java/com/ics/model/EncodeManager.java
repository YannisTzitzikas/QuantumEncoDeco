package com.ics.model;

import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedHashMap;

import com.ics.config.Config;
import com.ics.codec.encoding.Encoder;
import com.ics.codec.encoding.EncoderFactory;
import com.ics.services.BatchSizeMonitor;

public class EncodeManager {

    private final Map<Record, String> encodeMap;  // Stores full encoded records
    private final Map<String, String> termEncodeMap; // Stores individual term encoding
    private final Encoder encoder;
    private final BatchSizeMonitor batchMonitor;

    private int currentBatchSize;

    public EncodeManager(Config conf) {
        this.encodeMap = new LinkedHashMap<>();
        this.termEncodeMap = new TreeMap<>();
        this.encoder = EncoderFactory.getEncoder(conf.getEncoding());
        this.batchMonitor = new BatchSizeMonitor();
        this.currentBatchSize = 0;
    }

    public void process(Record record) {
        StringBuilder encodedRecord = new StringBuilder();

        // Encode each element individually
        for (String element : record) {
            String encodedValue = termEncodeMap.computeIfAbsent(element, encoder::encode);

            // Append encoded value with a delimiter
            if (encodedRecord.length() > 0) {
                encodedRecord.append(",");
            }
            encodedRecord.append(encodedValue);
        }

        // Store fully encoded record in the map
        encodeMap.put(record, encodedRecord.toString());
        currentBatchSize++;

        // Check batch size for flushing
        if (batchMonitor.isBatchLimitExceeded(currentBatchSize)) {
            flush();
        }
    }

    private void write() {
        System.out.println("Writing batch...");
        encodeMap.forEach((record, encoded) -> System.out.println(record + " -> " + encoded));
    }

    public void flush() {
        write();
        encodeMap.clear();
        termEncodeMap.clear();
        currentBatchSize = 0;
        System.out.println("Flush complete.");
    }
}
