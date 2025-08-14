package com.csd.common.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a chain of AccessStep from a dot‐delimited path.
 */
public final class ReflectionPathResolver {

    private final boolean preferGetters;

    public ReflectionPathResolver(boolean preferGetters) {
        this.preferGetters = preferGetters;
    }

    /**
     * Build a chain of AccessSteps to traverse `rootClass` by the given path (e.g. "a.b.c").
     */
    public List<AccessStep> resolveChain(Class<?> rootClass, String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Empty path");
        }
        String[] segments = path.split("\\.");
        List<AccessStep> chain = new ArrayList<>(segments.length);
        Class<?> cls = rootClass;
        for (String seg : segments) {
            AccessStep step = resolveStep(cls, seg);
            chain.add(step);
            cls = step.getResultType();
        }
        return chain;
    }

    private AccessStep resolveStep(Class<?> cls, String name) {
        if (preferGetters) {
            Method m = findGetter(cls, name);
            if (m != null) {
                return AccessStep.forMethod(m);
            }
            Field f = findField(cls, name);
            if (f != null) {
                return AccessStep.forField(f);
            }
        } else {
            Field f = findField(cls, name);
            if (f != null) {
                return AccessStep.forField(f);
            }
            Method m = findGetter(cls, name);
            if (m != null) {
                return AccessStep.forMethod(m);
            }
        }
        throw new IllegalArgumentException(
            "No readable property '" + name + "' on " + cls.getName());
    }

    private Method findGetter(Class<?> cls, String prop) {
        String cap = Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
        for (String prefix : new String[]{"get", "is"}) {
            try {
                Method m = cls.getMethod(prefix + cap);
                if (m.getParameterCount() == 0) {
                    return m;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private Field findField(Class<?> cls, String prop) {
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(prop);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
    }
}
