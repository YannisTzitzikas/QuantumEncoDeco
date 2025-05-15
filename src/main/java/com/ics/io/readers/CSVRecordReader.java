package com.ics.io.readers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

import com.ics.model.Record;

public class CSVRecordReader {
    public void readRecord(String filePath, Consumer<Record> processor) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.lines()
            .map(line -> line.split("\\s*,\\s*"))
            .filter(parts -> parts.length == 3)
            .map(parts -> new Record(parts[0], parts[1], parts[2]))
            .forEach(processor);
        }
    }
}