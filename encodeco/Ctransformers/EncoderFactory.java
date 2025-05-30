package Ctransformers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import Ctransformers.encode.IEncoder;
import Ctransformers.encode.R1Encoder;
import Ctransformers.encode.R2Encoder;

public class EncoderFactory {
    private final Map<String, Supplier<? extends IEncoder<?>>> registry = new HashMap<>();

    public EncoderFactory() {
        registerEncoder("R1", R1Encoder::new);
        registerEncoder("R2", R2Encoder::new);
    }

    public void registerEncoder(String type, Supplier<? extends IEncoder<?>> supplier) {
        registry.put(type, supplier);
    }

    public IEncoder<?> createEncoder(String type) {
        Supplier<? extends IEncoder<?>> supplier = registry.get(type);
        if (supplier == null) {
            throw new IllegalArgumentException("No encoder registered for type: " + type);
        }
        return supplier.get();
    }
}
