package com.csd.split;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.csd.core.split.SplitStrategy;

public class BeanFieldsSplitStrategy implements SplitStrategy {

    private final Map<String, String> portToPath;
    private final boolean preferGetters;
    private final boolean allowPrivateAccess;

    // cache: for each runtime class, cache the resolved access chain per path
    private final Map<Class<?>, Map<String, List<AccessStep>>> cache = new ConcurrentHashMap<>();

    public BeanFieldsSplitStrategy(Map<String, String> portToPath) {
        this(portToPath, true, true);
    }

    public BeanFieldsSplitStrategy(Map<String, String> portToPath,
            boolean preferGetters,
            boolean allowPrivateAccess) {
        this.portToPath = Objects.requireNonNull(portToPath, "portToPath");
        this.preferGetters = preferGetters;
        this.allowPrivateAccess = allowPrivateAccess;
    }

    @Override
    public List<SplitPart> split(Object input) {
        if (input == null)
            return Collections.emptyList();
        Class<?> rootClass = input.getClass();
        Map<String, List<AccessStep>> classCache = cache.computeIfAbsent(rootClass, k -> new ConcurrentHashMap<>());

        List<SplitPart> out = new ArrayList<>(portToPath.size());
        for (Map.Entry<String, String> e : portToPath.entrySet()) {
            String port = e.getKey();
            String path = e.getValue();

            List<AccessStep> chain = classCache.computeIfAbsent(path, p -> buildChain(rootClass, p));
            Object value = readChain(input, chain);
            out.add(new SplitPart(port, value));
        }
        return out;
    }

    private Object readChain(Object root, List<AccessStep> chain) {
        Object current = root;
        for (AccessStep step : chain) {
            if (current == null)
                return null;
            current = step.read(current, allowPrivateAccess);
        }
        return current;
    }

    private List<AccessStep> buildChain(Class<?> rootClass, String path) {
        if (path == null || path.isEmpty())
            throw new IllegalArgumentException("Empty path");

        String[] segments = path.split("\\.");
        List<AccessStep> chain = new ArrayList<>(segments.length);
        Class<?> currentClass = rootClass;

        for (String seg : segments) {
            AccessStep step = resolveStep(currentClass, seg);
            chain.add(step);
            currentClass = step.getResultType();
        }
        return chain;
    }

    private AccessStep resolveStep(Class<?> cls, String name) {
        if (preferGetters) {
            Method m = findGetter(cls, name);
            if (m != null)
                return AccessStep.forMethod(m);
            Field f = findField(cls, name);
            if (f != null)
                return AccessStep.forField(f);
        } else {
            Field f = findField(cls, name);
            if (f != null)
                return AccessStep.forField(f);
            Method m = findGetter(cls, name);
            if (m != null)
                return AccessStep.forMethod(m);
        }
        throw new IllegalArgumentException("No readable property '" + name + "' on " + cls.getName());
    }

    private Method findGetter(Class<?> cls, String name) {
        String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        String[] candidates = new String[] { "get" + cap, "is" + cap };
        for (String methodName : candidates) {
            try {
                Method m = cls.getMethod(methodName);
                if (m.getParameterCount() == 0)
                    return m;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private Field findField(Class<?> cls, String name) {
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    // Represents one hop in a.b.c path
    private static final class AccessStep {
        private final java.lang.reflect.Member member;
        private final Class<?> resultType;
        private final boolean isMethod;

        private AccessStep(Member member, Class<?> resultType, boolean isMethod) {
            this.member = member;
            this.resultType = resultType;
            this.isMethod = isMethod;
        }

        static AccessStep forMethod(Method m) {
            return new AccessStep(m, m.getReturnType(), true);
        }

        static AccessStep forField(Field f) {
            return new AccessStep(f, f.getType(), false);
        }

        Class<?> getResultType() {
            return resultType;
        }

        Object read(Object target, boolean allowPrivate) {
            try {
                if (isMethod) {
                    Method m = (Method) member;
                    if (allowPrivate) {
                        boolean orig = m.isAccessible();
                        try {
                            if (!orig)
                                m.setAccessible(true);
                            return m.invoke(target);
                        } finally {
                            if (!orig)
                                m.setAccessible(false);
                        }
                    } else {
                        return m.invoke(target);
                    }
                } else {
                    Field f = (Field) member;
                    if (allowPrivate) {
                        boolean orig = f.isAccessible();
                        try {
                            if (!orig)
                                f.setAccessible(true);
                            return f.get(target);
                        } finally {
                            if (!orig)
                                f.setAccessible(false);
                        }
                    } else {
                        return f.get(target);
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to read " + member + " on " + target.getClass().getName(), e);
            }
        }
    }
}
