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
    private static final String             DEFAULT_STORAGE_PATH = "temp";
    private static final String             DEFAULT_OUTPUT_PATH  = "output.txt";
    private static final String             DEFAULT_INPUT_PATH   = "input.txt";
    private static final String             DEFAULT_MAP_PATH     = "mappings.bin";

    private static final String             DEFAULT_FILE_FILTER  = "*";
    private static final String             DEFAULT_ENCODING     = "R1";
    private static final String             DEFAULT_STORAGE      = "hashmap";
 
    private static final int                DEFAULT_BATCH_SIZE   = 5_000_000;
    private static final boolean            DEFAULT_MODE         = false;    // encode == false ; decode == true
    private static final NamingStrategy     DEFAULT_NAME_STRAT   = NamingStrategy.SUFFIX_MODE;

    // Configuration variables
    private final Path                      inputPath;
    private final Path                      outputPath;
    private final Path                      storagePath;
    private final Path                      mappingsPath;

    private final String                    encoding;
    private final String                    fileFilterPattern;
    private final String                    storageBackend;

    private final boolean                   mode;

    private final int                       batchSize;
    private final NamingStrategy            namingStrategy;
    private final Map<String, Object>       parameters; 

    //----- Constructors ----- //
    private Config(Builder builder) {
        this.storagePath         = builder.storagePath;
        this.outputPath          = builder.outputPath;
        this.inputPath           = builder.inputPath;
        this.encoding            = builder.encoding;
        this.parameters          = builder.parameters;
        this.mode                = builder.mode;
        this.mappingsPath        = builder.mappingsPath;
        this.namingStrategy      = builder.namingStrategy;
        this.batchSize           = builder.batchSize;
        this.fileFilterPattern   = builder.fileFilterPattern;
        this.storageBackend      = builder.storageBackend;
    }

    public enum NamingStrategy {
        SUFFIX_MODE,                    // input.txt → input_encoded.txt
        FIXED_NAME,                     // output.bin
        PRESERVE_HIERARCHY              // maintain dir structure
    }

    //----- Builder Pattern ----- //
    public static class Builder {
        private Path    storagePath              = Paths.get(DEFAULT_STORAGE_PATH);
        private Path    outputPath               = Paths.get(DEFAULT_OUTPUT_PATH);
        private Path    inputPath                = Paths.get(DEFAULT_INPUT_PATH);
        private Path    mappingsPath             = Paths.get(DEFAULT_MAP_PATH);

        private String  fileFilterPattern        = DEFAULT_FILE_FILTER;
        private String  encoding                 = DEFAULT_ENCODING;
        private String  storageBackend           = DEFAULT_STORAGE;
        private boolean mode                     = DEFAULT_MODE;

        private NamingStrategy namingStrategy    = DEFAULT_NAME_STRAT;
        private int            batchSize         = DEFAULT_BATCH_SIZE;
        
        private Map<String, Object> parameters   = new HashMap<>();

        public Builder withInputPath(String path) {
            if(path != null) this.inputPath = Paths.get(path);
            return this;
        }
    
        public Builder withOutputPath(String path) {
            if(path != null) this.outputPath = Paths.get(path);
            return this;
        }

        public Builder withStoragePath(String path) {
            if(path != null) this.storagePath = Paths.get(path);
            return this;
        }
        
        public Builder withStorageBackend(String storage) {
            if(storage != null) this.storageBackend = storage;
            return this;
        }
        
        public Builder withMappingPath(String path) {
            if(path != null) this.mappingsPath = Paths.get(path);
            return this;
        }
    
        public Builder withEncoding(String encoding) {
            if (encoding != null) this.encoding = encoding;
            return this;
        }
         
        public Builder withMode(String mode) {
            if (mode != null) this.mode = "decode".equalsIgnoreCase(mode);
            return this;
        }

        public Builder withFileType(String fileFilterPattern) {
            if(fileFilterPattern != null) this.fileFilterPattern = fileFilterPattern;
            return this;
        }

        public Builder withBatchSize(Integer batchSize)
        {
            if(batchSize != null) this.batchSize = batchSize;
            return this;
        }
        
        public Builder withNamingStrat(String namingStrategy)
        {
            if (namingStrategy == null) {
                return this;
            }

            switch (namingStrategy.toUpperCase()) {
                case "SUFFIX_MODE": this.namingStrategy = NamingStrategy.SUFFIX_MODE; break;
                case "FIXED_NAME": this.namingStrategy = NamingStrategy.FIXED_NAME; break;
                case "PRESERVE_HIERARCHY": this.namingStrategy = NamingStrategy.PRESERVE_HIERARCHY; break;
                default: throw new IllegalArgumentException("Unknown naming strategy: " + namingStrategy);
            }

            return this;
        }

        public Builder withParameters(Map<String, Object> parameters) {
            if (parameters != null) this.parameters = parameters;
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

    public Path getStoragePath() {
        return storagePath;
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

    public String getFileFilterPattern() {
        return fileFilterPattern;
    }

    public String getStorageBackend() {
        return storageBackend;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public NamingStrategy getNamingStrategy() {
        return namingStrategy;
    }
 
    public Map<String, Object> getParameters() {
        return parameters;
    }

    // Retrieve specific parameter by key
    public Object getParameter(String key) {
        return parameters.getOrDefault(key, "Not specified");
    }

    //----- toString ----- //
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\tInput file: ").append(inputPath)
               .append("\n\tOutput file: ").append(outputPath)
               .append("\n\tEncoding: ").append(encoding)
               .append("\n\tMode: ").append(mode ? "DECODE" : "ENCODE")
               .append("\n\tFile Filter Pattern: ").append(fileFilterPattern)
               .append("\n\tBuffer Size: ").append(batchSize)
               .append("\n\tNaming Strategy: ").append(namingStrategy)
               .append("\n\tParameters: ").append(parameters);
        return builder.toString();
    }
}
