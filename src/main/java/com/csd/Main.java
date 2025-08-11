package com.csd;

import com.csd.common.io.reader.IQEDReader;
import com.csd.common.io.reader.QEDReaderFactory;
import com.csd.config.GraphConfig;
import com.csd.config.GraphConfigMapper;
import com.csd.config.JobConfig;
import com.csd.config.JobConfigMapper;
import com.csd.stage.StageRegistry;
import com.csd.validation.GraphValidator;
import com.csd.validation.ValidationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            log.error("Usage: java -jar app.jar <path-to-config.{json|yaml|...}>");
            System.exit(1);
        }

        Path configPath = Paths.get(args[0]);
        if (!Files.exists(configPath) || !Files.isRegularFile(configPath)) {
            log.error("Configuration file not found: {}", configPath);
            System.exit(2);
        }

        try {
            Map<String, Object> intermediate = null;

            IQEDReader reader   = QEDReaderFactory.getForFile(configPath.toString());
            intermediate        = reader.read(configPath); 

            log.info("Loaded JobConfig: {}", intermediate);
            JobConfig jobConfig = JobConfigMapper.map(intermediate);

            log.info("Loaded JobConfig: {}", jobConfig);

            // Load graph configuration
            Path graphConfigPath  = jobConfig.getGraphPath();
            reader                = QEDReaderFactory.getForFile(graphConfigPath.toString());
            intermediate          = reader.read(graphConfigPath);

            GraphConfig   graphConf = GraphConfigMapper.map(intermediate); 
            StageRegistry registry  = new StageRegistry();
            registry.discoverViaSPI();

            GraphValidator validator = new GraphValidator(registry);
            ValidationResult result  = validator.validate(graphConf);

            log.info("{}",result);
            log.info("Loaded GraphConfig: {}", graphConf);

        } catch (IllegalArgumentException e) {
            log.error("Configuration error: {}", e.getMessage());
            System.exit(3);
        } catch (IOException e) {
            log.error("I/O error reading configuration: {}", e.getMessage());
            System.exit(4);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(4);
        }
    }
}
