package com.csd.examples.common.filters.operations;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.rocksdb.RocksDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R1RocksDbDecoderBatchOp
        implements Function<List<String>, List<String>> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(R1RocksDbDecoderBatchOp.class);

    private final RocksDB reverseDb;
    private final int bitWidthN;

    public R1RocksDbDecoderBatchOp(
            RocksDB reverseDb,
            int bitWidthN) {

        this.reverseDb = reverseDb;
        this.bitWidthN = bitWidthN;
    }

    @Override
    public List<String> apply(List<String> lines) {

        if (lines == null || lines.isEmpty()) {
            return null;
        }

        try {

            // 3 keys per triple
            List<byte[]> lookupKeys =
                    new ArrayList<>(lines.size() * 3);

            // compact primitive storage
            long[] parsed =
                    new long[lines.size() * 3];

            int parsedIndex = 0;

            // =========================
            // PHASE 1: Parse bitstrings
            // =========================

            for (String line : lines) {

                if (line == null ||
                    line.length() < 3 * bitWidthN) {
                    continue;
                }

                long s = parseBits(line, 0, bitWidthN);

                long p = parseBits(
                        line,
                        bitWidthN,
                        bitWidthN);

                long o = parseBits(
                        line,
                        2 * bitWidthN,
                        bitWidthN);

                parsed[parsedIndex++] = s;
                parsed[parsedIndex++] = p;
                parsed[parsedIndex++] = o;

                // IMPORTANT:
                // DB currently stores STRING KEYS
                lookupKeys.add(
                        Long.toString(s)
                                .getBytes(StandardCharsets.UTF_8));

                lookupKeys.add(
                        Long.toString(p)
                                .getBytes(StandardCharsets.UTF_8));

                lookupKeys.add(
                        Long.toString(o)
                                .getBytes(StandardCharsets.UTF_8));
            }

            // =========================
            // PHASE 2: Bulk lookup
            // =========================

            List<byte[]> values =
                    reverseDb.multiGetAsList(lookupKeys);

            // =========================
            // PHASE 3: Reconstruct
            // =========================

            int tripleCount = parsedIndex / 3;

            List<String> output =
                    new ArrayList<>(tripleCount);

            int valueIndex = 0;

            for (int i = 0; i < tripleCount; i++) {

                byte[] sVal = values.get(valueIndex++);
                byte[] pVal = values.get(valueIndex++);
                byte[] oVal = values.get(valueIndex++);

                if (sVal == null ||
                    pVal == null ||
                    oVal == null) {
                    continue;
                }

                StringBuilder builder =
                        new StringBuilder(256);

                builder.append('<')
                       .append(new String(
                               sVal,
                               StandardCharsets.UTF_8))
                       .append("> <")
                       .append(new String(
                               pVal,
                               StandardCharsets.UTF_8))
                       .append("> <")
                       .append(new String(
                               oVal,
                               StandardCharsets.UTF_8))
                       .append("> .");

                output.add(builder.toString());
            }

            LOGGER.info(
                    "Decoded {} triples from {} lines",
                    output.size(),
                    lines.size());

            return output;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Batch RocksDB decode failure",
                    e);
        }
    }

    /**
     * Fast manual binary parser.
     * Avoids substring allocation + parseLong overhead.
     */
    private long parseBits(
            String line,
            int start,
            int length) {

        long value = 0L;

        for (int i = 0; i < length; i++) {

            value <<= 1;

            if (line.charAt(start + i) == '1') {
                value |= 1L;
            }
        }

        return value;
    }
}