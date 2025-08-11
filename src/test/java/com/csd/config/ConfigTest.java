package com.csd.config;

import com.csd.common.io.reader.QEDJsonReader;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;


public class ConfigTest {

    @Test
    public void testGraphConfigParsing() throws Exception {
        // Load file from resources
        InputStream in = getClass().getClassLoader().getResourceAsStream("ConfigTest.json");
        if (in == null) {
            throw new IllegalStateException("ConfigTest.json not found in resources");
        }

        // Parse JSON into Map
        QEDJsonReader reader = new QEDJsonReader();
        Map<String, Object> raw = reader.read(() -> new InputStreamReader(in));

        // Map to GraphConfig
        GraphConfig config = new GraphConfigMapper().map(raw);

        // Print full config
        System.out.println("Parsed GraphConfig:");
        System.out.println(config);

        // Validate params
        System.out.println("\nValidating node parameters:");
        for (NodeConfig node : config.getNodes()) {
            System.out.println("Node: " + node.getName());
            Map<String, Object> params = node.getStageConf().getParams();

            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                boolean isValidType = value instanceof String || value instanceof Number || value instanceof Boolean;

                assertTrue("Invalid type for param '" + key + "' in node '" + node.getName() + "': " + value.getClass().getName(), isValidType);
                System.out.println("  " + (isValidType ? "✔" : "✖") + " " + key + " = " + value + " (" + value.getClass().getSimpleName() + ")");
            }
        }
    }
}
