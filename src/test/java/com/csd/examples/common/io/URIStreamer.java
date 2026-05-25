package com.csd.examples.common.io;

import java.util.function.Consumer;

import com.csd.examples.common.model.URITriple;

public interface URIStreamer extends AutoCloseable {
    public void stream(String filePath, Consumer<URITriple> processor);
}
