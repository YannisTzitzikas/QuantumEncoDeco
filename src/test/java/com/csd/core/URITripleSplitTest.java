package com.csd.core;

import com.csd.core.model.uri.TripleComponent;
import com.csd.core.model.uri.URITriple;
import com.csd.core.split.BeanFieldsSplitStrategy;
import com.csd.core.split.SplitStrategy;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class URITripleSplitTest {

    @Test
    public void testSplitURITripleFields() {
        TripleComponent subject = new TripleComponent(
                "http://example.org/Alice",
                TripleComponent.Kind.IRI,
                TripleComponent.Role.SUBJECT);
        TripleComponent predicate = new TripleComponent(
                "http://xmlns.com/foaf/0.1/knows",
                TripleComponent.Kind.IRI,
                TripleComponent.Role.PREDICATE);
        TripleComponent object = new TripleComponent(
                "http://example.org/Bob",
                TripleComponent.Kind.IRI,
                TripleComponent.Role.OBJECT);
        URITriple triple = new URITriple(subject, predicate, object);

        Map<String, String> portToPath = new LinkedHashMap<>();
        portToPath.put("subjectOut", "subject");
        portToPath.put("predicateOut", "predicate");
        portToPath.put("objectOut", "object");
        portToPath.put("subjectValue", "subject.value");
        portToPath.put("predicateKind", "predicate.kind");
        portToPath.put("objectRole", "object.role");

        SplitStrategy splitter = new BeanFieldsSplitStrategy(portToPath);
        List<SplitStrategy.SplitPart> parts = splitter.split(triple);

        assertEquals(6, parts.size());

        for (SplitStrategy.SplitPart part : parts) {
            String port = part.getPortName();
            Object payload = part.getPayload();

            switch (port) {
                case "subjectOut":
                    assertEquals(subject, payload);
                    break;
                case "predicateOut":
                    assertEquals(predicate, payload);
                    break;
                case "objectOut":
                    assertEquals(object, payload);
                    break;
                case "subjectValue":
                    assertEquals("http://example.org/Alice", payload);
                    break;
                case "predicateKind":
                    assertEquals(TripleComponent.Kind.IRI, payload);
                    break;
                case "objectRole":
                    assertEquals(TripleComponent.Role.OBJECT, payload);
                    break;
                default:
                    fail("Unexpected port: " + port);
            }
        }
    }
}
