package Ctransformers.encode;

import java.util.concurrent.atomic.AtomicInteger;

import Ctransformers.EncodingData;
import Ewritters.EWritter;

public class R1Encoder extends BaseEncoder<Integer> {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Integer encode(EncodingData data) {
        String canonical = getCanonicalURI(data.getValue());
        return mappings.computeIfAbsent(canonical, k -> counter.getAndIncrement());
    }

    @Override
    public void saveMappings(String filename) {
        EWritter writter = new EWritter(filename);
        mappings.forEach((key, value)-> writter.write(key + " " + value + "\n"));
        writter.close();
    }

    @Override
    public Integer getEncoded(String uri) {
        return mappings.get(uri);
    }
}