package com.csd.core;

import java.util.List;

import com.csd.core.config.Config;
import com.csd.core.config.ConfigParser;
import com.csd.core.config.ConfigParserFactory;
import com.csd.core.service.EncodingService;

public class Main {

    public static void main(String[] args) {
        
        ConfigParser parser = ConfigParserFactory.createParser("D:\\Projects\\QuantumEncoDeco\\src\\test\\resources\\aaa.json");

        List<Config> confList = parser.parseConfig("D:\\Projects\\QuantumEncoDeco\\src\\test\\resources\\aaa.json");

        for (Config config : confList) {
            try {
                EncodingService test = new EncodingService(config);
                test.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
