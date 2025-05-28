package Ctransformers.encode;

import Ctransformers.EncodingData;
import Ctransformers.TripleComponent;
import Ctransformers.URIFactory;

import java.util.Map;

public abstract class BaseEncoder<T> implements IEncoder<T> {
    protected abstract T createEncoding(TripleComponent type);

    protected String getCanonicalURI(String uri) {
        return URIFactory.getURI(uri);
    }

    @Override
    public T encode(EncodingData data) {
        String canonicalURI = getCanonicalURI(data.getValue());
        return getMappings(data.getType()).computeIfAbsent(canonicalURI, key -> createEncoding(data.getType()));
    }

    @Override
    public T getEncoded(String uri, TripleComponent component) {
        String canonicalURI = getCanonicalURI(uri);
        T value = getMappings(component).get(canonicalURI);
        if (value != null) {
            return value;
        }
        return null;
    }

    @Override
    public Map<String, T> getMappings(TripleComponent type) {
        return getMappings(type);
    }
}
