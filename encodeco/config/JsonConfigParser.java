package config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import encode.EncodingType;

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
            JSONArray configArray = (JSONArray) jsonParser.parse(reader);
            JSONObject configObj = (JSONObject) configArray.get(0);

            return new Config.Builder()
                .withInputFilePath((String) configObj.get("inputFile"))
                .withOutputFilePath((String) configObj.get("outputFile"))
                .withEncoding(parseEncoding((String) configObj.get("encoding")))
                .build();
            
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error parsing config file: " + e.getMessage(), e);
        }
    }

    // ----- Utilities ----- //
    private boolean validateFile(File file) {
        return file != null && file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(".json");
    }

    private EncodingType parseEncoding(String encoding) {
        if (encoding == null) {
            return EncodingType.R1;  // Adjust based on your EncodingType enum
        }

        try {
            return EncodingType.valueOf(encoding.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Unknown encoding '" + encoding + "'. Using default.");
            return EncodingType.R1;
        }
    }
}
