package com.csd.examples.common.filters.operations;

import java.util.function.Function;

import org.rocksdb.RocksDB;

public class R1RocksDbDecoderMap implements Function<String, String> {
    private final RocksDB reverseDb;
    private final int bitWidthN;

    /**
     * @param reverseDb Open instance of RocksDB loaded with ID -> URI mappings
     * @param bitWidthN Calculated length of bits representing one component
     */
    public R1RocksDbDecoderMap(RocksDB reverseDb, int bitWidthN) {
        this.reverseDb = reverseDb;
        this.bitWidthN = bitWidthN;
    }

    @Override
    public String apply(String line) {
       
        String triple = null;

        if (line.length() < 3 * bitWidthN) return null;

        // 1. Slice bitstring into component segments
        String sBits = line.substring(0, bitWidthN);
        String pBits = line.substring(bitWidthN, 2 * bitWidthN);
        String oBits = line.substring(2 * bitWidthN, 3 * bitWidthN);

        // 2. Convert base-2 bitstrings directly to String IDs
        String sId = String.valueOf(Long.parseLong(sBits, 2));
        String pId = String.valueOf(Long.parseLong(pBits, 2));
        String oId = String.valueOf(Long.parseLong(oBits, 2));

        try {
            // 3. Query native off-heap RocksDB Block Cache 
            byte[] sValue = reverseDb.get(sId.getBytes());
            byte[] pValue = reverseDb.get(pId.getBytes());
            byte[] oValue = reverseDb.get(oId.getBytes());

            if (sValue != null && pValue != null && oValue != null) {
                // Reconstruct standard N-Triple serialization text format
                triple = "<" + new String(sValue) + "> <" 
                             + new String(pValue) + "> <" 
                             + new String(oValue) + "> .";
            }

        } catch (Exception e) {
            throw new RuntimeException("RocksDB decoding lookup exception", e);
        }
        return triple;
    }
}