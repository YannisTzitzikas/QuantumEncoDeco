package com.csd.core.codec.encoding;

import java.util.TreeMap;

// FIXME(gtheo): This will not work yet as intended.
//               If the file is too large and we flush
//               the first batch, then there is the possiblity
//               that a repeated term gets a different id

public class R1Encoder implements Encoder {
    private final TreeMap<String, Integer> dictionary = new TreeMap<>();
    private Integer nextId = 0;

    public String encode(String element) {
        return dictionary.computeIfAbsent(element, key -> nextId++).toString();
    }

    public TreeMap<String, Integer> getDictionary() {
        return dictionary;
    }
}
