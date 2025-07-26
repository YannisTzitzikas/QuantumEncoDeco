package com.csd.io.readers;

import java.util.function.Consumer;

import com.csd.model.Record;

public interface RecordReader {
    public void readRecord(String filePath, Consumer<Record> processor);
}
