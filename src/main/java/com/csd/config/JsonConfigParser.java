package com.csd.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import java.util.List;
import java.util.ArrayList;

import org.json.simple.JSONArray;
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
    public List<Config> parseConfig(File file) {
        if (!validateFile(file)) {
            throw new IllegalArgumentException("Invalid file provided: " + file.getPath());
        }
        return parseJson(file);
    }

    @Override
    public List<Config> parseConfig(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }
        return parseConfig(filePath.toFile()); // Convert Path to File
    }

    @Override
    public List<Config> parseConfig(String filePath) {
        if (filePath == null || filePath == "") {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        return parseConfig(new File(filePath)); // Convert String to File
    }

        private List<Config> parseJson(File file) {
        List<Config> configList = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            JSONParser jsonParser = new JSONParser();
            Object parsedObj = jsonParser.parse(reader);

            if (parsedObj instanceof JSONArray) {
                JSONArray configArray = (JSONArray) parsedObj;
                for (Object obj : configArray) {
                    if (obj instanceof JSONObject) {
                        configList.add(parseConfigObject((JSONObject) obj));
                    }
                }
            } else if (parsedObj instanceof JSONObject) {
                configList.add(parseConfigObject((JSONObject) parsedObj));
            } else {
                throw new RuntimeException("Unexpected JSON format. Expected JSONObject or JSONArray.");
            }

        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error parsing config file: " + e.getMessage(), e);
        }

        return configList;
    }


    // ----- Utilities ----- //
    private boolean validateFile(File file) {
        return file != null && file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(".json");
    }

    private Config parseConfigObject(JSONObject configObj) {
        String inputFile  = (String) configObj.get("inputFile");
        String outputFile = (String) configObj.get("outputFile");
        String encoding   = (String) configObj.get("encoding");
        String mode       = (String) configObj.get("mode");

        return new Config.Builder()
            .withInputFilePath(inputFile)
            .withOutputFilePath(outputFile)
            .withEncoding(encoding)
            .withMode(mode)
            .build();
    }
}
