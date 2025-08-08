package com.csd.core.config;

import org.junit.Test;

import com.csd.config.Config;
import com.csd.config.ConfigParser;
import com.csd.config.ConfigParserFactory;
import com.csd.config.JsonConfigParser;

import java.util.List;
import static org.junit.Assert.*;

public class ConfigReaderTest {

    @Test
    public void testConfigParsingFromJson() {
        ConfigParser parser = ConfigParserFactory.createParser("src/test/resources/ConfingReaderTest.json");
        assertEquals(parser.getClass(), JsonConfigParser.class);

        List<Config> configs = parser.parseConfig("src/test/resources/ConfingReaderTest.json");

        assertNotNull(configs);
        assertEquals(3, configs.size());

        Config first = configs.get(0);
        assertEquals("data\\input\\job1.txt", first.getInputPath().toString());
        assertEquals("data\\output\\job1_encoded.txt", first.getOutputPath().toString());
        assertEquals("data\\mappings\\job1.map", first.getMappingsPath().toString());
        assertEquals("customCodecA", first.getEncoding());
        assertEquals("*.txt", first.getFileFilterPattern());
        assertEquals("encode", first.getMode());
        assertEquals(Config.NamingStrategy.SUFFIX_MODE, first.getNamingStrategy());
        assertEquals(4096, first.getBatchSize());
    }
}
