package com.csd.config;

import java.util.Map;
import java.util.HashMap;

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
    private static final boolean            DEFAULT_MODE        = false;

    private final String                    inputPath;
    private final String                    outputPath;
    private final String                    encoding;
    private final boolean                   mode;
    private final Map<String, String>       parameters; 

    //----- Constructors ----- //
    private Config(String inputPath, String outputPath, String encoding, boolean mode, Map<String, String> parameters) {
        this.inputPath  = inputPath;
        this.outputPath = outputPath;
        this.encoding   = encoding;
        this.parameters = parameters;
        this.mode       = mode;
    }

    //----- Builder Pattern ----- //
    public static class Builder {
        private String  inputFilePath  = DEFAULT_INPUT_PATH;
        private String  outputFilePath = DEFAULT_OUTPUT_PATH;
        private String  encoding       = DEFAULT_ENCODING;
        private boolean mode           = DEFAULT_MODE;
        private Map<String, String> parameters = new HashMap<>();

        public Builder withInputFilePath(String path) {
            this.inputFilePath = (path != null) ? path : DEFAULT_INPUT_PATH;
            return this;
        }
    
        public Builder withOutputFilePath(String path) {
            this.outputFilePath = (path != null) ? path : DEFAULT_OUTPUT_PATH;
            return this;
        }
    
        public Builder withEncoding(String encoding) {
            this.encoding = (encoding != null) ? encoding : DEFAULT_ENCODING;
            return this;
        }
    
        public Builder withParameters(Map<String, String> parameters) {
            this.parameters = (parameters != null) ? parameters : new HashMap<>();
            return this;
        }
    
        public Builder withMode(String mode) {
            this.mode = "decode".equalsIgnoreCase(mode);
            return this;
        }
    
        public Config build() {
            return new Config(inputFilePath, outputFilePath, encoding, mode, parameters);
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

   public String getMode() {
        return mode == false ? "encode" : "decode" ;
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
               .append("\n\tMode: ").append(getMode())
               .append("\n\tParameters: ").append(parameters);
        return builder.toString();
    }
}
