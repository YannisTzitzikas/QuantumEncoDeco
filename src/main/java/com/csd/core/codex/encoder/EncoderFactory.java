package com.csd.core.codex.encoder;

public class EncoderFactory {

    private EncoderFactory() {
        // Prevent instantiation
    }

    /**
     * Returns a new encoder instance based on the name.
     * Defaults to R1Encoder if name is unknown.
     */
    public static IEncoder<?> getEncoder(String name) {
        switch (name.trim().toLowerCase()) {
            case "r1": return new R1Encoder();
            case "r2": return new R2Encoder();
            default:
                System.err.println("Unknown encoder name: " + name + ", using R1Encoder.");
                return new R1Encoder();
        }
    }
}
