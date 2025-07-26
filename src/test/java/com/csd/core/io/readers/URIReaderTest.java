package com.csd.core.io.readers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import org.junit.Test;

import com.csd.core.model.*;

public class URIReaderTest {
    @Test
    public void testConfigParsingFromJson() {
        try {
            URIReader reader = URIReaderFactory.getReader("URIreaderTest.xml");

            assertEquals(reader.getClass(), OntologyURIReader.class);

            List<URITriple> triples = new ArrayList<>();

            reader.read("src/test/resources/URIreaderTest.xml", triple -> {
                triples.add(triple);
            });

            assertEquals(triples.size(), 8);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
