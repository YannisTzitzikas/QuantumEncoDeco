package Ctransformers.decoder;

public class DecoderFactory {
    public static IDecoder createDecoder(String encodingType) {
        if ("R1".equalsIgnoreCase(encodingType)) {
            return new R1Decoder();
        } else if ("R2".equalsIgnoreCase(encodingType)) {
            return new R2Decoder();
        } else {
            throw new IllegalArgumentException("Unsupported decoder type: " + encodingType);
        }
    }
}
