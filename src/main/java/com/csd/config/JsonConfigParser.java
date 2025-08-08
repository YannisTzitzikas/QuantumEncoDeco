package com.csd.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import java.util.List;
import java.util.ArrayList;

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
        return parseConfig(filePath.toFile());
    }

    @Override
    public List<Config> parseConfig(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        return parseConfig(new File(filePath));
    }

    private List<Config> parseJson(File file) {
        List<Config> configList = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (root.isJsonArray()) {
                JsonArray array = root.getAsJsonArray();
                for (JsonElement element : array) {
                    configList.add(parseConfigObject(element.getAsJsonObject()));
                }
            } else if (root.isJsonObject()) {
                configList.add(parseConfigObject(root.getAsJsonObject()));
            } else {
                throw new RuntimeException("Unexpected JSON format. Expected JsonObject or JsonArray.");
            }

        } catch (IOException e) {
            throw new RuntimeException("Error parsing config file: " + e.getMessage(), e);
        }

        return configList;
    }

    private Config parseConfigObject(JsonObject obj) {
        String inputPath         = obj.has("input")       ? obj.get("input").getAsString()       : null;
        String outputPath        = obj.has("output")      ? obj.get("output").getAsString()      : null;
        String storagePath       = obj.has("storage")     ? obj.get("storage").getAsString()     : null;
        String mappingsPath      = obj.has("mapping")     ? obj.get("mapping").getAsString()     : null;
        String encoding          = obj.has("encoding")    ? obj.get("encoding").getAsString()    : null;
        String fileFilterPattern = obj.has("format")      ? obj.get("format").getAsString()      : null;
        String storageBackend    = obj.has("engine")      ? obj.get("engine").getAsString()      : null;
        String mode              = obj.has("mode")        ? obj.get("mode").getAsString()        : null;
        String namingStrategy    = obj.has("namingStrat") ? obj.get("namingStrat").getAsString() : null;

        Integer batchSize        = obj.has("batchSize")  ? obj.get("batchSize").getAsInt()    : null;

        return new Config.Builder()
            .withInputPath(inputPath)
            .withOutputPath(outputPath)
            .withOutputPath(storagePath)
            .withMappingPath(mappingsPath)
            .withStorageBackend(storageBackend)
            .withEncoding(encoding)
            .withMode(mode)
            .withBatchSize(batchSize)
            .withNamingStrat(namingStrategy)
            .withFileType(fileFilterPattern)
            .build();
    }

    private boolean validateFile(File file) {
        return file != null && file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(".json");
    }
}
