package com.csd.core.io;

import java.util.function.Consumer;

import com.csd.core.model.uri.URITriple;

public interface URIStreamer extends AutoCloseable {
    public void stream(String filePath, Consumer<URITriple> processor);
}
