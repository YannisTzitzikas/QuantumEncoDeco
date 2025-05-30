package Ewritters.encoding;

import java.util.HashMap;
import java.util.Map;

public class EncoderWriterFactory {
    private static final Map<String, IEncodeWriter> writerMap = new HashMap<>();

    static {
        writerMap.put("R1", new R1EncodeWriter());
        writerMap.put("R2", new R2EncodeWriter());
    }

    public static IEncodeWriter getWriter(String type) {
        IEncodeWriter writer = writerMap.get(type);

        if (writer == null) {
            throw new IllegalArgumentException("Unsupported encoder type: " + type);
        }

        return writer;
    }
}