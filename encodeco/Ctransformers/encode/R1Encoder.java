package Ctransformers.encode;

import Ewritters.EWritter;

public class R1Encoder extends BaseEncoder<Integer> {
    private int counter = 0;

    @Override
    public Integer encode(String uri) {
        String canonical = getCanonicalURI(uri);
        return mappings.computeIfAbsent(canonical, k -> counter++);
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