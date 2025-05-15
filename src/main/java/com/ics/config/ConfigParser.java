package com.ics.config;

import java.io.File;
import java.nio.file.Path;

import java.util.List;

public interface ConfigParser {
    // Parse the Configuration
    public List<Config>     parseConfig(File  file);
    public List<Config>     parseConfig(Path filePath);
    public List<Config>     parseConfig(String filePath);
}
