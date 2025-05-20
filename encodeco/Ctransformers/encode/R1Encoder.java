package Ctransformers.encode;

import java.util.TreeMap;

import Ewritters.EWritter;

public class R1Encoder implements IEncoder {

    private TreeMap<String, Integer> uriToId = new TreeMap<>();
    private int currentId = 0;

    @Override
    public int encode(String uri) {
        return uriToId.computeIfAbsent(uri, k -> currentId++);
    }

    @Override
    public void saveMappings(String filename) {
        EWritter writer = new EWritter(filename);
        int bitsNeeded  = bitsNeeded(currentId);
        
        uriToId.forEach((uri, id) -> writer.write(uri + "," + id + "\n"));
        writer.close();
    }

    private static int bitsNeeded(int n) {
        if (n == 0) {
            return 1; // 0 requires 1 bit
        }
        return 32 - Integer.numberOfLeadingZeros(n);
    }
}
