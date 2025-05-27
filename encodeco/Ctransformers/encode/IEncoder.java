package Ctransformers.encode;

import Ctransformers.EncodingData;

import java.util.Map;

public interface IEncoder<T> {
    T encode(EncodingData data);
    T getEncoded(String uri);
    Map<String, T> getMappings();
    void saveMappings(String filename);
}
