package com.csd.split;

import com.csd.core.split.SplitStrategy;
import com.csd.common.reflection.AccessStep;
import com.csd.common.reflection.ReflectionPathResolver;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Map;

import java.util.List;
import java.util.ArrayList;

import java.util.Objects;
import java.util.Collections;


public class BeanFieldsSplitStrategy implements SplitStrategy {

    private final Map<String, String> portToPath;
    private final boolean allowPrivateAccess;
    private final ReflectionPathResolver resolver;

    // cache: for each runtime class, cache the resolved access chain per path
    private final Map<Class<?>, Map<String, List<AccessStep>>> cache = new ConcurrentHashMap<>();

    public BeanFieldsSplitStrategy(Map<String, String> portToPath) {
        this(portToPath, true, true);
    }

    public BeanFieldsSplitStrategy(Map<String, String> portToPath,
                                   boolean preferGetters,
                                   boolean allowPrivateAccess) {
        this.portToPath = Objects.requireNonNull(portToPath, "portToPath");
        this.allowPrivateAccess = allowPrivateAccess;
        this.resolver = new ReflectionPathResolver(preferGetters);
    }

    @Override
    public List<SplitPart> split(Object input) {
        if (input == null) {
            return Collections.emptyList();
        }
        Class<?> rootClass = input.getClass();
        Map<String, List<AccessStep>> classCache =
            cache.computeIfAbsent(rootClass, k -> new ConcurrentHashMap<>());

        List<SplitPart> parts = new ArrayList<>(portToPath.size());
        for (Map.Entry<String, String> e : portToPath.entrySet()) {
            String port = e.getKey();
            String path = e.getValue();

            List<AccessStep> chain =
                classCache.computeIfAbsent(path,
                    p -> resolver.resolveChain(rootClass, p));

            Object value = readChain(input, chain);
            parts.add(new SplitPart(port, value));
        }
        return parts;
    }

    private Object readChain(Object root, List<AccessStep> chain) {
        Object current = root;
        for (AccessStep step : chain) {
            if (current == null) {
                return null;
            }
            current = step.read(current, allowPrivateAccess);
        }
        return current;
    }
}
