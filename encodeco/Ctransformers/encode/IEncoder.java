package Ctransformers.encode;

import Ctransformers.EncodingData;
import Ctransformers.TripleComponent;

import java.util.Map;

public interface IEncoder<T> {
    T encode(EncodingData data);
    T getEncoded(String uri, TripleComponent component);
    Map<String, T> getMappings(TripleComponent component);
    void saveMappings(String filename);
}
