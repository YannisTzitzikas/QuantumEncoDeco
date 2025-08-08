package com.csd.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author George Theodorakis (giorgostheodoraki@hotmail.com)
 *  Class for creating a configuration parser dynamically based on the file extension
 * of the configuration file or based. This is not multi
 */
public class ConfigParserFactory {
    private static final Map<String, ConfigParser> parserRegistry = new HashMap<>();

    static {
        registerParser(".json", new JsonConfigParser());
    }

    public static void registerParser(String extension, ConfigParser parser) {
        parserRegistry.put(extension.toLowerCase(), parser);
    }

    public static ConfigParser createParser(File file) {
        String extension =  file.getName()
                                .toLowerCase()
                                .substring(file.getName().lastIndexOf('.'));

        return getParser(extension);
    }

    public static ConfigParser createParser(String filePath) {
        String extension =  filePath.toLowerCase()
                                    .substring(filePath.lastIndexOf('.'));

        return getParser(extension);
    }

    private static ConfigParser getParser(String extension) {
        ConfigParser parser = parserRegistry.get(extension);
        if (parser == null) {
            throw new UnsupportedOperationException("No parser for extension: " + extension);
        }
        return parser;
    }
}