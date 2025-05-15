package com.ics;

import com.ics.config.ConfigParserFactory;
import com.ics.io.utils.FileManager;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        // Let the user choose a configuration file
        File configFile = FileManager.showFileChooserForFile();
        if (configFile == null) {
            System.out.println("No file selected. Exiting.");
            return;
        }

        try {
            System.out.println(ConfigParserFactory.createParser(configFile).parseConfig(configFile)); 
        } catch (UnsupportedOperationException e) {
            System.err.println("Error: Unsupported file type. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}