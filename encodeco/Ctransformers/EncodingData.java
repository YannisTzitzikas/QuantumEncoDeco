package Ctransformers;

public class EncodingData {
    private final String value;
    private final TripleComponent type;

    public EncodingData(String value, TripleComponent type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public TripleComponent getType() {
        return type;
    }
}
