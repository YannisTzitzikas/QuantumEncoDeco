package com.csd.core.config;

import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * @author George Theodorakis (csd4881@csd.uoc.gr)
 *  Class for configuration. It can store configuration details.
 *  In this way, we can support several configutation files and file types.
 *  (instead of having to change the code to test an alternative configuration).
 */
public final class Config {

    // Default values as constants
    private static final String             DEFAULT_OUTPUT_PATH = "output.txt";
    private static final String             DEFAULT_INPUT_PATH  = "input.txt";
    private static final String             DEFAULT_MAP_PATH    = "mappings.bin";

    private static final String             DEFAULT_FILE_FILTER = "*";
    private static final String             DEFAULT_ENCODING    = "R1";

    private static final int                DEFAULT_BUFFER_SIZE = 0x400;
    private static final boolean            DEFAULT_MODE        = false;    // encode == false ; decode == true
    private static final boolean            DEFAULT_OVERWRITE   = false;
    private static final NamingStrategy     DEFAULT_NAME_STRAT  = NamingStrategy.SUFFIX_MODE;


    // Configuration variables
    private final Path                      inputPath;
    private final Path                      outputPath;
    private final Path                      mappingsPath;

    private final String                    encoding;
    private final String                    fileFilterPattern;

    private final boolean                   mode;
    private final boolean                   overwriteExisting;  

    private final int                       bufferSize;
    private final NamingStrategy            namingStrategy;
    private final Map<String, String>       parameters; 

    //----- Constructors ----- //
    private Config(Builder builder) {
        this.inputPath           = builder.inputPath;
        this.outputPath          = builder.outputPath;
        this.encoding            = builder.encoding;
        this.parameters          = builder.parameters;
        this.mode                = builder.mode;
        this.mappingsPath        = builder.mappingsPath;
        this.namingStrategy      = builder.namingStrategy;
        this.overwriteExisting   = builder.overwriteExisting;
        this.bufferSize          = builder.bufferSize;
        this.fileFilterPattern   = builder.fileFilterPattern;
    }

    public enum NamingStrategy {
        SUFFIX_MODE,                    // input.txt → input_encoded.txt
        FIXED_NAME,                     // output.bin
        PRESERVE_HIERARCHY              // maintain dir structure
    }

    //----- Builder Pattern ----- //
    public static class Builder {
        private Path    inputPath               = Paths.get(DEFAULT_INPUT_PATH);
        private Path    outputPath              = Paths.get(DEFAULT_OUTPUT_PATH);
        private Path    mappingsPath            = Paths.get(DEFAULT_MAP_PATH);

        private String fileFilterPattern        = DEFAULT_FILE_FILTER;
        private String  encoding                = DEFAULT_ENCODING;
        private boolean mode                    = DEFAULT_MODE;

        private NamingStrategy namingStrategy   = DEFAULT_NAME_STRAT;
        private boolean overwriteExisting       = DEFAULT_OVERWRITE;
        private int bufferSize                  = DEFAULT_BUFFER_SIZE;
        
        private Map<String, String> parameters = new HashMap<>();

        public Builder withInputFilePath(String path) {
            this.inputPath = (path != null) ? Paths.get(path) : inputPath;
            return this;
        }
    
        public Builder withOutputFilePath(String path) {
            this.outputPath = (path != null) ? Paths.get(path) : outputPath;
            return this;
        }
        
        public Builder withMappingFilePath(String path) {
            this.mappingsPath = (path != null) ? Paths.get(path) : mappingsPath;
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
            return new Config(this);
        }
    }

    //----- Getters ----- //
    public Path getInputPath() {
        return inputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public Path getMappingsPath() {
        return mappingsPath;
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
