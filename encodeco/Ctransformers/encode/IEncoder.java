package Ctransformers.encode;

import java.util.Map;

public interface IEncoder<T> {
    T encode(String uri);
    T getEncoded(String uri);
    Map<String, T> getMappings();
    void saveMappings(String filename);
}
