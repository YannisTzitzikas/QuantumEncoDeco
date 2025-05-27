package Ewritters;

import java.util.HashMap;
import java.util.Map;

public class EncoderWriterFactory {
    private static final Map<String, IWriter> writerMap = new HashMap<>();

    static {
        writerMap.put("R1", new R1Writer());
        writerMap.put("R2", new R2Writer());
    }

    public static IWriter getWriter(String type) {
        IWriter writer = writerMap.get(type);

        if (writer == null) {
            throw new IllegalArgumentException("Unsupported encoder type: " + type);
        }

        return writer;
    }
}