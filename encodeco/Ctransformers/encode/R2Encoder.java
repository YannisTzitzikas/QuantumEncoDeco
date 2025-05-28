package Ctransformers.encode;

import Ctransformers.TripleComponent;
import Ewritters.EWritter;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class R2Encoder extends BaseEncoder<Integer> {
    private final Map<String, Integer> predicateMappings = new LinkedHashMap<>();
    private final Map<String, Integer> entityMappings = new LinkedHashMap<>();

    private final AtomicInteger subjectObjectCounter = new AtomicInteger(0);
    private final AtomicInteger predicateCounter = new AtomicInteger(0);

    @Override
    public Map<String, Integer> getMappings(TripleComponent type) {
        return (type == TripleComponent.PREDICATE) ? predicateMappings : entityMappings;
    }

    @Override
    protected Integer createEncoding(TripleComponent type) {
        return (type == TripleComponent.PREDICATE) ? predicateCounter.getAndIncrement() : subjectObjectCounter.getAndIncrement();
    }

    @Override
    public void saveMappings(String filename) {
        EWritter writer = new EWritter(filename);

        writer.write("R2, " + predicateCounter.get() + ", " + subjectObjectCounter.get() + "\n");
        predicateMappings.forEach((key, value) -> writer.write(key + " " + value + "\n"));
        entityMappings.forEach((key, value) -> writer.write(key + " " + value + "\n"));
    }
}
