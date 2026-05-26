package com.csd.examples.common.filters.operations;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.rocksdb.RocksDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.examples.common.model.TripleComponent;
import com.csd.examples.common.model.URITriple;

public class R1RocksDbEncoderBatchOp
        implements Function<List<URITriple>, List<String>> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(R1RocksDbEncoderBatchOp.class);

    private final RocksDB forwardDb;
    private final int bitWidthN;

    public R1RocksDbEncoderBatchOp(
            RocksDB forwardDb,
            int bitWidthN) {

        this.forwardDb = forwardDb;
        this.bitWidthN = bitWidthN;
    }

    @Override
    public List<String> apply(List<URITriple> triples) {

        if (triples == null || triples.isEmpty()) {
            return null;
        }

        try {

            // =========================
            // PHASE 1: Collect lookup keys
            // =========================

            List<byte[]> lookupKeys =
                    new ArrayList<>(triples.size() * 3);

            // compact storage for reconstruction
            String[] uris =
                    new String[triples.size() * 3];

            int uriIndex = 0;

            for (URITriple triple : triples) {

                String sUri = extractUri(triple.getSubject());
                String pUri = extractUri(triple.getPredicate());
                String oUri = extractUri(triple.getObject());

                if (sUri == null ||
                    pUri == null ||
                    oUri == null) {
                    continue;
                }

                uris[uriIndex++] = sUri;
                uris[uriIndex++] = pUri;
                uris[uriIndex++] = oUri;

                lookupKeys.add(
                        sUri.getBytes(StandardCharsets.UTF_8));

                lookupKeys.add(
                        pUri.getBytes(StandardCharsets.UTF_8));

                lookupKeys.add(
                        oUri.getBytes(StandardCharsets.UTF_8));
            }

            // =========================
            // PHASE 2: Bulk lookup
            // =========================

            List<byte[]> values =
                    forwardDb.multiGetAsList(lookupKeys);

            // =========================
            // PHASE 3: Encode
            // =========================

            int tripleCount = uriIndex / 3;

            List<String> output =
                    new ArrayList<>(tripleCount);

            int valueIndex = 0;

            for (int i = 0; i < tripleCount; i++) {

                byte[] sIdBytes = values.get(valueIndex++);
                byte[] pIdBytes = values.get(valueIndex++);
                byte[] oIdBytes = values.get(valueIndex++);

                if (sIdBytes == null ||
                    pIdBytes == null ||
                    oIdBytes == null) {
                    continue;
                }

                long sId =
                        parseAsciiLong(sIdBytes);

                long pId =
                        parseAsciiLong(pIdBytes);

                long oId =
                        parseAsciiLong(oIdBytes);

                StringBuilder builder =
                        new StringBuilder(bitWidthN * 3);

                appendPaddedBits(builder, sId, bitWidthN);
                appendPaddedBits(builder, pId, bitWidthN);
                appendPaddedBits(builder, oId, bitWidthN);

                output.add(builder.toString());
            }

            LOGGER.info(
                    "Encoded {} triples from {} input triples",
                    output.size(),
                    triples.size());

            return output;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Batch RocksDB encoding lookup exception",
                    e);
        }
    }

    private String extractUri(TripleComponent comp) {

        if (comp != null &&
            comp.getKind() == TripleComponent.Kind.IRI) {

            return comp.getValue();
        }

        return null;
    }

    /**
     * Faster than:
     * Long.parseLong(new String(bytes))
     */
    private long parseAsciiLong(byte[] bytes) {

        long value = 0L;

        for (byte b : bytes) {
            value = (value * 10L) + (b - '0');
        }

        return value;
    }

    /**
     * Appends fixed-width binary representation directly
     * without intermediate strings.
     */
    private void appendPaddedBits(
            StringBuilder builder,
            long value,
            int width) {

        for (int i = width - 1; i >= 0; i--) {

            builder.append(
                    ((value >>> i) & 1L) == 1L
                            ? '1'
                            : '0');
        }
    }
}