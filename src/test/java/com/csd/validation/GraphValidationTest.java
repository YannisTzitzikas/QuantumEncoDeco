package com.csd.validation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.csd.common.io.reader.QEDJsonReader;
import com.csd.config.GraphConfig;
import com.csd.config.GraphConfigMapper;
import com.csd.stage.StageRegistry;


public class GraphValidationTest {

    StageRegistry registry;
    GraphValidator validator;

    GraphConfig dagErrorConfig;
    GraphConfig typeErrorConfig;
    GraphConfig validConfig;

    @Before
    public void setUpGraphConfigs() {
        // Load file from resources
        InputStream dagErrorStream = getClass().getClassLoader().getResourceAsStream("DAGError.json");
        InputStream typeErrorStream = getClass().getClassLoader().getResourceAsStream("InvalidType.json");
        InputStream validStream = getClass().getClassLoader().getResourceAsStream("ValidConfig.json");

        if (dagErrorStream == null || typeErrorStream == null ||  validStream == null) {
            throw new IllegalStateException("At least one of the files was not found in resources");
        }

        // Parse JSON into Map
        QEDJsonReader reader = new QEDJsonReader();
        Map<String, Object> raw;
        try {
            raw = reader.read(() -> new InputStreamReader(dagErrorStream));
            dagErrorConfig = new GraphConfigMapper().map(raw);

            raw = reader.read(() -> new InputStreamReader(typeErrorStream));
            typeErrorConfig = new GraphConfigMapper().map(raw);

            raw = reader.read(() -> new InputStreamReader(validStream));
            validConfig = new GraphConfigMapper().map(raw);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUpRegistryAndValidator() {
        registry = new StageRegistry();
        registry.discoverViaSPI();

        validator = new GraphValidator(registry);
    }

    @Test
    public void dagErrorTest() {
        System.out.println(validator.validate(dagErrorConfig));
    }

    @Test
    public void typeErrorTest() {
        System.out.println(validator.validate(typeErrorConfig));
    }

    @Test
    public void validConfigTest() {
        System.out.println(validator.validate(validConfig));
    }


}
