package com.ics.io.readers;

import java.util.function.Consumer;

import com.ics.model.Record;

public interface RecordReader {
    public void readRecord(String filePath, Consumer<Record> processor);
}
