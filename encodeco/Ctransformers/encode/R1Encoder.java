package Ctransformers.encode;

import Ctransformers.TripleComponent;
import Ewritters.EWritter;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class R1Encoder extends BaseEncoder<Integer> {
    private final Map<String, Integer> mappings = new LinkedHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Map<String, Integer> getMappings(TripleComponent type) {
        return mappings;
    }

    @Override
    protected Integer createEncoding(TripleComponent type) {
        return counter.getAndIncrement();
    }

    @Override
    public void saveMappings(String filename) {
        EWritter writter = new EWritter(filename);
        writter.write("R1, " + counter.get() + "\n");

        mappings.forEach((key, value)-> writter.write(key + " " + value + "\n"));
        writter.close();
    }

}
