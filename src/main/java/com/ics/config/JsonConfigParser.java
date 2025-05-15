package com.ics.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import java.util.Map;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *  Class for configuration. It can read a configuration in JSON format.
 */
public class JsonConfigParser implements ConfigParser {

    @Override
    public Config parseConfig(File file) {
        if (!validateFile(file)) {
            throw new IllegalArgumentException("Invalid file provided: " + file.getPath());
        }
        return parseJson(file);
    }

    @Override
    public Config parseConfig(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }
        return parseConfig(filePath.toFile()); // Convert Path to File
    }

    @Override
    public Config parseConfig(String filePath) {
        if (filePath == null || filePath == "") {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        return parseConfig(new File(filePath)); // Convert String to File
    }

    private Config parseJson(File file) {
        try (FileReader reader = new FileReader(file)) {
            JSONParser jsonParser = new JSONParser();
            JSONObject configObj = (JSONObject) jsonParser.parse(reader);

            // Extract base config fields
            String inputFile  = (String) configObj.get("inputFile");
            String outputFile = (String) configObj.get("outputFile");
            String encoding   = (String) configObj.get("encoding");

            // Extract additional parameters
            Map<String, String> parameters = new HashMap<>();
            
            JSONObject paramsObj = (JSONObject) configObj.get("parameters");
            
            if (paramsObj != null) {
                for (Object key : paramsObj.keySet()) { 
                    String paramKey   = (String) key; 
                    String paramValue = (String) paramsObj.get(paramKey);
                    parameters.put(paramKey, paramValue);
                }
            }

            return new Config.Builder()
                .withInputFilePath(inputFile)
                .withOutputFilePath(outputFile)
                .withEncoding(encoding)
                .withParameters(parameters) 
                .build();

        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error parsing config file: " + e.getMessage(), e);
        }
    }

    // ----- Utilities ----- //
    private boolean validateFile(File file) {
        return file != null && file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(".json");
    }

}
