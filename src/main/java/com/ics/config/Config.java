package com.ics.config;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *  Class for configuration. It can store configuration details.
 *  In this way, we can have several configutation file 
 *  (instead of having to change the code to test an alternative configuration).
 */
public final class Config {

    // Default values as constants
    private static final String             DEFAULT_INPUT_PATH  = "input.txt";
    private static final String             DEFAULT_OUTPUT_PATH = "output.txt";
    private static final String             DEFAULT_ENCODING    = "R1";

    private final String                    inputPath;
    private final String                    outputPath;
    private final String                    encoding;
    private final Map<String, String>       parameters; 

    //----- Constructors ----- //
    private Config(String inputPath, String outputPath, String encoding, Map<String, String> parameters) {
        this.inputPath  = inputPath;
        this.outputPath = outputPath;
        this.encoding   = encoding;
        this.parameters = parameters;
    }

    //----- Builder Pattern ----- //
    public static class Builder {
        private String inputFilePath  = DEFAULT_INPUT_PATH;
        private String outputFilePath = DEFAULT_OUTPUT_PATH;
        private String encoding       = DEFAULT_ENCODING;
        private Map<String, String> parameters = new HashMap<>();

        public Builder withInputFilePath(String path) {
            this.inputFilePath = Objects.requireNonNull(path, "Input path cannot be null");
            return this;
        }

        public Builder withOutputFilePath(String path) {
            this.outputFilePath = Objects.requireNonNull(path, "Output path cannot be null");
            return this;
        }

        public Builder withEncoding(String encoding) {
            this.encoding = Objects.requireNonNull(encoding, "Encoding cannot be null");
            return this;
        }

        public Builder withParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Config build() {
            return new Config(inputFilePath, outputFilePath, encoding, parameters);
        }
    }

    //----- Getters ----- //
    public String getInputPath() {
        return inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getEncoding() {
        return encoding;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    // Retrieve specific parameter by key
    public String getParameter(String key) {
        return parameters.getOrDefault(key, "Not specified");
    }

    //----- toString ----- //
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\tInput file: ").append(inputPath)
               .append("\n\tOutput file: ").append(outputPath)
               .append("\n\tEncoding: ").append(encoding)
               .append("\n\tParameters: ").append(parameters);
        return builder.toString();
    }
}
