package com.csd.common.io.reader;

import java.io.File;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

public interface IReader {
    Map<String, Object> read(File file)                       throws Exception;
    Map<String, Object> read(Path filePath)                   throws Exception;
    Map<String, Object> read(String fileStr)                  throws Exception;
    Map<String, Object> read(Supplier<Reader> readerSupplier) throws Exception;
}
