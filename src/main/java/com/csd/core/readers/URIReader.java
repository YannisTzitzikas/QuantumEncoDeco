package com.csd.core.readers;

import java.util.function.Consumer;

import com.csd.core.model.uri.URITriple;

public interface URIReader {
    public void stream(String filePath, Consumer<URITriple> processor);
}
