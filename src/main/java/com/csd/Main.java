package com.csd;

import com.csd.common.io.reader.IQEDReader;
import com.csd.common.io.reader.QEDReaderFactory;
import com.csd.config.GraphConfig;
import com.csd.config.GraphConfigMapper;
import com.csd.config.JobConfig;
import com.csd.config.JobConfigMapper;
import com.csd.core.split.SplitterRegistry;
import com.csd.core.stage.StageRegistry;
import com.csd.validation.GraphSanitizer;

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

        SplitterRegistry splitReg = new SplitterRegistry();
        StageRegistry    stageReg = new StageRegistry();

        stageReg.discoverViaSPI();
        splitReg.discoverViaSPI();

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

            GraphConfigMapper gmap = new GraphConfigMapper();
            GraphConfig   graphConf = gmap.map(intermediate); 

            GraphSanitizer validator = new GraphSanitizer(stageReg, splitReg);
            
            log.info("Validation Results are: {}" ,validator.validate(graphConf));
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
