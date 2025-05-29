/**
 * 
 */
package Aconfig;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author Yannis Tzitzikas (yannistzitzik@gmail.com)
 *         Class for configuration. It can store/read a configuration in JSON
 *         format.
 *         In this way, we can have several configutation file
 *         (instead of having to change the code to test an alternative
 *         configuration).
 */
public class AConfig {
    private String inputfilepath;
    private String outputfilepath;
    private String mappingFile;

    private String outputFormat = "csv"; 
    private String encoding = "R1";
    private String mode = "encode"; // Default to "encode"

    // Getters and Setters
    public String getMappingFile() {
        return mappingFile;
    }

    public String getEncoding() {
        return encoding;
    }
    
    public String getOutputFormat() {
        return outputFormat;
    }

    public void setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
    }

    /** * @return the inputfilepath */
    public String getInputfilepath() {
        return inputfilepath;
    }

    /** * @param inputfilepath the inputfilepath to set */
    public void setInputfilepath(String inputfilepath) {
        this.inputfilepath = inputfilepath;
    }

    /** * @return the outputfilepath */
    public String getOutputfilepath() {
        return outputfilepath;
    }

    /** * @param outputfilepath the outputfilepath to set */
    public void setOutputfilepath(String outputfilepath) {
        this.outputfilepath = outputfilepath;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        if (mode.equalsIgnoreCase("encode") || mode.equalsIgnoreCase("decode")) {
            this.mode = mode;
        } else {
            this.mode = "encode"; // Default value if input is invalid
        }
    }

    public void writeConfigFile(String filepath) {
        JSONObject files = new JSONObject();
        files.put("inputFile", inputfilepath);
        files.put("outputFile", outputfilepath);
        files.put("encoding", encoding);
        files.put("mappingFile", mappingFile);
        files.put("mode", mode);
        files.put("format", outputFormat);

        JSONArray configArray = new JSONArray();
        configArray.add(files);

        // Write JSON file
        try (FileWriter file = new FileWriter(filepath)) {
            file.write(configArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AConfig() {
    }

    public AConfig(String filepath) {
        readConfigFile(filepath);
    }

    private void readConfigFile(String filepath) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(filepath)) {
            Object obj = jsonParser.parse(reader);
            JSONArray configArray = (JSONArray) obj;

            // Reading JSON properties
            JSONObject oj = (JSONObject) configArray.get(0);
            inputfilepath = (String) oj.get("inputFile");
            outputfilepath = (String) oj.get("outputFile");
            outputFormat = (String) oj.get("format");
            encoding = (String) oj.get("encoding");

            if (outputFormat == null) outputFormat = "csv";

            // Read new parameters
            mappingFile = (String) oj.get("mappingFile");
            String modeStr = (String) oj.get("mode");

            if(mappingFile == null) mappingFile = inputfilepath + ".map";

            // Validate mode
            if (modeStr != null && (modeStr.equalsIgnoreCase("encode") || modeStr.equalsIgnoreCase("decode"))) {
                mode = modeStr;
            } else {
                mode = "encode"; // Default value if mode is not valid
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "\tInput file: " + inputfilepath + "\n" +
                "\tMapping file: " + mappingFile + "\n" +
                "\tOutput file: " + outputfilepath + "\n" +
                "\tEncoding: " + encoding + "\n" +
                "\tFormat: " + outputFormat + "\n" +
                "\tMode: " + mode;
    }
}
