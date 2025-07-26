package com.csd.core.io.readers;

import java.util.function.Consumer;

import com.csd.core.model.Record;

public interface RecordReader {
    public void readRecord(String filePath, Consumer<Record> processor);
}
