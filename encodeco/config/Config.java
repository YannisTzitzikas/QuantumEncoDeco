package config;

import java.util.Objects;

import encode.EncodingType;

/**
 * 
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *  Class for configuration. It can store configuration details.
 *  In this way, we can have several configutation file 
 *  (instead of having to change the code to test an alternative configuration).
 */
public final class Config {

    // Default values as constants
    private static final String         DEFAULT_INPUT_PATH  = "input.txt";
    private static final String         DEFAULT_OUTPUT_PATH = "output.txt";
    private static final EncodingType   DEFAULT_ENCODING    = EncodingType.R1;

    private final String                inputPath;
    private final String                outputPath;
    private final EncodingType          encoding;

    //----- Constructors ----- //
    private Config(String inputFilePath, String outputFilePath, EncodingType encoding) {
        this.inputPath = inputFilePath;
        this.outputPath = outputFilePath;
        this.encoding = encoding;
    }

    //----- Builder Pattern ----- //
    public static class Builder {
        private String inputFilePath = DEFAULT_INPUT_PATH;
        private String outputFilePath = DEFAULT_OUTPUT_PATH;
        private EncodingType encoding = DEFAULT_ENCODING;

        public Builder withInputFilePath(String path) {
            this.inputFilePath = Objects.requireNonNull(path, "Input path cannot be null");
            return this;
        }

        public Builder withOutputFilePath(String path) {
            this.outputFilePath = Objects.requireNonNull(path, "Output path cannot be null");
            return this;
        }

        public Builder withEncoding(EncodingType encoding) {
            this.encoding = Objects.requireNonNull(encoding, "Encoding cannot be null");
            return this;
        }

        public Config build() {
            return new Config(inputFilePath, outputFilePath, encoding);
        }
    }

    //----- Getters ----- //
    public String getInputPath() {
        return inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public EncodingType getEncoding() {
        return encoding;
    }

    //----- toString ----- //
    @Override
    public String toString() {
        String i = "\tInput file: "  + inputPath;
        String o = "\tOutput file: " + outputPath;
        String e = "\tEncoding: "    + encoding;
        return i + "\n" + o + "\n" + e;
    }
}