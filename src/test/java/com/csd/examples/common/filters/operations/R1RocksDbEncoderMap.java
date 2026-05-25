package com.csd.examples.common.filters.operations;

import java.util.function.Function;

import org.rocksdb.RocksDB;

import com.csd.examples.common.model.TripleComponent;
import com.csd.examples.common.model.URITriple;

public class R1RocksDbEncoderMap implements Function<URITriple, String> {
    private final RocksDB forwardDb;
    private final int bitWidthN;

    /**
     * @param forwardDb RocksDB instance configured with URI -> ID mappings
     * @param bitWidthN The exact bit-length required for one component (e.g., 26 for 40M)
     */
    public R1RocksDbEncoderMap(RocksDB forwardDb, int bitWidthN) {
        this.forwardDb = forwardDb;
        this.bitWidthN = bitWidthN;
    }

    private String extractUri(TripleComponent comp) {
        if (comp != null && comp.getKind() == TripleComponent.Kind.IRI) {
            return comp.getValue();
        }
        return null;
    }

    private String padLeftWithZeros(String binaryStr, int targetLength) {
        if (binaryStr.length() >= targetLength) {
            return binaryStr;
        }
        StringBuilder sb = new StringBuilder(targetLength);
        for (int i = 0; i < targetLength - binaryStr.length(); i++) {
            sb.append('0');
        }
        sb.append(binaryStr);
        return sb.toString();
    }

    @Override
    public String apply(URITriple triple) {
        String encoded = null;
        try {
            // Extract strings securely
            String sUri = extractUri(triple.getSubject());
            String pUri = extractUri(triple.getPredicate());
            String oUri = extractUri(triple.getObject());

            if (sUri == null || pUri == null || oUri == null) return null;

            // Query native off-heap RocksDB
            byte[] sIdBytes = forwardDb.get(sUri.getBytes());
            byte[] pIdBytes = forwardDb.get(pUri.getBytes());
            byte[] oIdBytes = forwardDb.get(oUri.getBytes());

            // If all three components exist in the dictionary, encode them
            if (sIdBytes != null && pIdBytes != null && oIdBytes != null) {
                long sId = Long.parseLong(new String(sIdBytes));
                long pId = Long.parseLong(new String(pIdBytes));
                long oId = Long.parseLong(new String(oIdBytes));

                // Convert to binary and pad to N bits
                String sBits = padLeftWithZeros(Long.toBinaryString(sId), bitWidthN);
                String pBits = padLeftWithZeros(Long.toBinaryString(pId), bitWidthN);
                String oBits = padLeftWithZeros(Long.toBinaryString(oId), bitWidthN);

                encoded = (sBits + pBits + oBits);
            }

        } catch (Exception e) {
            throw new RuntimeException("RocksDB encoding lookup exception", e);
        }

        return encoded;
    }
}