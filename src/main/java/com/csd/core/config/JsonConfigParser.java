package com.csd.core.config;

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
 * @author George Theodorakis (csd4881@csd.uoc.gr)
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

        String inputPath         = configObj.containsKey("input")       ? (String) configObj.get("input") : null;
        String outputPath        = configObj.containsKey("output")      ? (String) configObj.get("output") : null;
        String mappingsPath      = configObj.containsKey("mapping")     ? (String) configObj.get("mapping") : null;
        String encoding          = configObj.containsKey("encoding")    ? (String) configObj.get("encoding") : null;
        String fileFilterPattern = configObj.containsKey("format")      ? (String) configObj.get("format") : null;
        String mode              = configObj.containsKey("mode")        ? (String) configObj.get("mode") : null;
        String namingStrategy    = configObj.containsKey("namingStrat") ? (String) configObj.get("namingStrat") : null;
        String parameters        = configObj.containsKey("params")      ? (String) configObj.get("params") : null; 

        Integer bufferSize        = configObj.containsKey("bufferSize") ? (Integer) configObj.get("bufferSize") : null ;
        boolean overwriteExisting = configObj.containsKey("overwrite")  ? (boolean) configObj.get("overwrite") : false ;

        // TODO(gtheo): Add the option to pass various encoding-specific parameters 
        return new Config.Builder()
            .withInputPath(inputPath)
            .withOutputPath(outputPath)
            .withMappingPath(mappingsPath)
            .withEncoding(encoding)
            .withMode(mode)
            .withBufferSize(bufferSize)
            .withNamingStrat(namingStrategy)
            .withNamingStrat(fileFilterPattern)
            .withOverwrite(overwriteExisting)
            .build();
    }
}
