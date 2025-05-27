package Ctransformers.encode;

import Ctransformers.EncodingData;
import Ctransformers.TripleComponent;
import Ewritters.EWritter;

import java.util.concurrent.atomic.AtomicInteger;

public class R2Encoder extends BaseEncoder<Integer> {
    private final AtomicInteger subjectObjectCounter = new AtomicInteger(0);
    private final AtomicInteger predicateCounter = new AtomicInteger(0);

    @Override
    public Integer encode(EncodingData data) {
        String canonicalURI = getCanonicalURI(data.getValue());
        TripleComponent type = data.getType();

        return mappings.computeIfAbsent(canonicalURI, key -> {
            if (type == TripleComponent.PREDICATE) {
                return predicateCounter.getAndIncrement();
            } else {
                return subjectObjectCounter.getAndIncrement();
            }
        });
    }

    @Override
    public Integer getEncoded(String uri) {
        String canonicalURI = getCanonicalURI(uri);
        return mappings.getOrDefault(canonicalURI, null);
    }

    @Override
    public void saveMappings(String filename) {
        EWritter writer = new EWritter(filename);
        mappings.forEach((key, value) -> writer.write(key + " " + value + "\n"));
        writer.close();
    }
}
