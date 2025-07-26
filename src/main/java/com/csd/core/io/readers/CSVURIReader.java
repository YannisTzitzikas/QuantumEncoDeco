package com.csd.core.io.readers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

import com.csd.core.model.URITriple;

public class CSVURIReader implements URIReader {
    public void read(String filePath, Consumer<URITriple> processor) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.lines()
            .map(line -> line.split("\\s*,\\s*"))
            .filter(parts -> parts.length == 3)
            .map(parts -> new URITriple(parts[0], parts[1], parts[2]))
            .forEach(processor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}