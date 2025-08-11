package com.csd.validation;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.csd.common.io.reader.QEDJsonReader;
import com.csd.config.GraphConfig;
import com.csd.config.GraphConfigMapper;
import com.csd.stage.StageRegistry;

public class GraphValidationTest {

    static StageRegistry registry;
    static GraphValidator validator;

    static GraphConfig dagErrorConfig;
    static GraphConfig typeErrorConfig;
    static GraphConfig typeErrorConfig2;
    static GraphConfig validConfig;

    @BeforeClass
        public static void setUpGraphConfigs() {
        // Correct way to load resources statically
        ClassLoader cl = GraphValidationTest.class.getClassLoader();

        InputStream dagErrorStream    = cl.getResourceAsStream("DAGError.json");
        InputStream typeErrorStream   = cl.getResourceAsStream("InvalidType.json");
        InputStream typeErrorStream2  = cl.getResourceAsStream("InvalidTypeV2.json");
        InputStream validStream       = cl.getResourceAsStream("ValidConfig.json");

        if (dagErrorStream == null || typeErrorStream == null ||
            typeErrorStream2 == null || validStream == null) {
            throw new IllegalStateException("One or more config files were not found in resources");
        }

        QEDJsonReader reader = new QEDJsonReader();
        GraphConfigMapper mapper = new GraphConfigMapper();

        try {
            dagErrorConfig    = mapper.map(reader.read(() -> new InputStreamReader(dagErrorStream)));
            typeErrorConfig   = mapper.map(reader.read(() -> new InputStreamReader(typeErrorStream)));
            typeErrorConfig2  = mapper.map(reader.read(() -> new InputStreamReader(typeErrorStream2)));
            validConfig       = mapper.map(reader.read(() -> new InputStreamReader(validStream)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load and map graph configs", e);
        }
    }

    @BeforeClass
    public static void setUpRegistryAndValidator() {
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
    public void typeErrorTest2() {
        System.out.println(validator.validate(typeErrorConfig2));
    }

    @Test
    public void validConfigTest() {
        System.out.println(validator.validate(validConfig));
    }

    @AfterClass
    public static void cleanUp() {
        dagErrorConfig = null;
        typeErrorConfig = null;
        typeErrorConfig2 = null;
        validConfig = null;

        registry = null;
        validator = null;
    }

}
