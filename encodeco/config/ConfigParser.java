package config;

import java.io.File;

public interface ConfigParser {
    // Parse the Configuration
    public Config     parseConfig(File  file);
    public Config     parseConfig(Path filePath);
    public Config     parseConfig(String filePath);
}
