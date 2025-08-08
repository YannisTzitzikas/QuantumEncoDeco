package com.csd.common.io.readers;

import java.util.function.Consumer;

import com.csd.core.model.URITriple;

public interface URIReader {
    public void stream(String filePath, Consumer<URITriple> processor);
}
