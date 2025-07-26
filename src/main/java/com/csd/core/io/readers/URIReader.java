package com.csd.core.io.readers;

import java.util.function.Consumer;

import com.csd.core.model.URITriple;

public interface URIReader {
    public void read(String filePath, Consumer<URITriple> processor);
}
