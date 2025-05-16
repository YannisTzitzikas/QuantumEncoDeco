package com.ics;

import com.ics.config.Config;
import com.ics.config.ConfigParserFactory;
import com.ics.io.readers.ReaderFactory;
import com.ics.io.readers.RecordReader;
import com.ics.io.utils.FileManager;
import com.ics.model.EncodeManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Let the user choose a configuration file
        File configFile = FileManager.showFileChooserForFile();
        if (configFile == null) {
            System.out.println("No file selected. Exiting.");
            return;
        }

        try {
            List<Config> confList = ConfigParserFactory.createParser(configFile).parseConfig(configFile);

            confList.forEach(conf -> {
                try {
                    String inputPath = conf.getInputPath();
                    RecordReader recordReader = ReaderFactory.getReader(inputPath);

                    // Initialize EncodeManager with current configuration
                    EncodeManager encodeManager = new EncodeManager(conf);

                    // Process each record
                    recordReader.readRecord(inputPath, record -> {
                        encodeManager.process(record);
                        System.out.println("Encoded: " + record);
                    });

                    // Ensure flushing happens after processing
                    encodeManager.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (UnsupportedOperationException e) {
            System.err.println("Error: Unsupported file type. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
