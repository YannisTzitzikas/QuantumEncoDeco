package com.csd.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author George Theodorakis (csd4881@csd.uoc.gr)
 *  Class for configuration. It can store configuration details.
 *  In this way, we can support several configutation files and file types.
 *  (instead of having to change the code to test an alternative configuration).
 */
public final class JobConfig {

    // Default values as constants
    private static final String DEFAULT_STORAGE_SETTINGS_PATH = "__PLACE_HOLDER__";
    private static final String DEFAULT_OUTPUT_PATH           = "output.txt";
    private static final String DEFAULT_INPUT_PATH            = "input.txt";

    // Configuration variables
    private final Path          inputPath;
    private final Path          outputPath;
    private final Path          storageSettingsPath;

    //----- Constructors ----- //
    private JobConfig(Builder builder) {
        this.outputPath          = builder.outputPath;
        this.inputPath           = builder.inputPath;
        this.storageSettingsPath = builder.storageSettingsPath;
    }

    //----- Builder Pattern ----- //
    public static class Builder {
        private Path    inputPath           = Paths.get(DEFAULT_INPUT_PATH);
        private Path    outputPath          = Paths.get(DEFAULT_OUTPUT_PATH);
        private Path    storageSettingsPath = Paths.get(DEFAULT_STORAGE_SETTINGS_PATH);
        
        public Builder withInputPath(String path) {
            if(path != null) this.inputPath = Paths.get(path);
            return this;
        }
    
        public Builder withOutputPath(String path) {
            if(path != null) this.outputPath = Paths.get(path);
            return this;
        }

        public Builder withStorageSettingsPath(String path) {
            if(path != null) this.storageSettingsPath = Paths.get(path);
            return this;
        }

        public JobConfig build() {
            return new JobConfig(this);
        }
    }

    //----- Getters ----- //
    public Path getInputPath() {
        return inputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public Path getStorageSettingsPath() {
        return storageSettingsPath;
    }

    //----- toString ----- //
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\tInput file: ").append(inputPath)
               .append("\n\tOutput file: ").append(outputPath)
               .append("\n\tStorage Config file: ").append(storageSettingsPath);

        return builder.toString();
    }
}
