package config;

import java.io.File;
import java.nio.file.Path;

public interface ConfigParser {
    // Parse the Configuration
    public Config     parseConfig(File  file);
    public Config     parseConfig(Path filePath);
    public Config     parseConfig(String filePath);
}
