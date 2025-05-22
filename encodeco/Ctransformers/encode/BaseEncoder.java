package Ctransformers.encode;

import java.util.Map;
import java.util.LinkedHashMap;

import Ctransformers.URIFactory;

public abstract class BaseEncoder<T> implements IEncoder<T> {
    protected final Map<String, T> mappings = new LinkedHashMap<>();
    
    protected String getCanonicalURI(String uri) {
        return URIFactory.getURI(uri);
    }

    @Override
    public Map<String, T> getMappings() {
        return mappings;
    }
}