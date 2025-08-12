package com.csd.stage;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.csd.core.stage.StageRegistry;

public class StageRegistryTest {

    @Test
    public void testSPIDiscovery() {
        StageRegistry registry = new StageRegistry();
        registry.discoverViaSPI();

        assertTrue("R1 provider should be discovered", registry.get("R1").isPresent());
    }

}
