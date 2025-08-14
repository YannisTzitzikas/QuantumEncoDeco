package com.csd.common.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * One hop in a property path (either a getter or a field).
 */
public final class AccessStep {
    private final Member member;
    private final Class<?> resultType;
    private final boolean isMethod;

    private AccessStep(Member member, Class<?> resultType, boolean isMethod) {
        this.member = member;
        this.resultType = resultType;
        this.isMethod = isMethod;
    }

    public static AccessStep forMethod(Method m) {
        return new AccessStep(m, m.getReturnType(), true);
    }

    public static AccessStep forField(Field f) {
        return new AccessStep(f, f.getType(), false);
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public Object read(Object target, boolean allowPrivate) {
        try {
            if (isMethod) {
                return invoke((Method) member, target, allowPrivate);
            } else {
                return access((Field) member, target, allowPrivate);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                "Failed to read " + member + " on " + target.getClass().getName(), e);
        }
    }

    private Object invoke(Method m, Object target, boolean allowPrivate)
            throws ReflectiveOperationException {
        if (allowPrivate) {
            boolean orig = m.isAccessible();
            try {
                if (!orig) m.setAccessible(true);
                return m.invoke(target);
            } finally {
                if (!orig) m.setAccessible(false);
            }
        }
        return m.invoke(target);
    }

    private Object access(Field f, Object target, boolean allowPrivate)
            throws ReflectiveOperationException {
        if (allowPrivate) {
            boolean orig = f.isAccessible();
            try {
                if (!orig) f.setAccessible(true);
                return f.get(target);
            } finally {
                if (!orig) f.setAccessible(false);
            }
        }
        return f.get(target);
    }
}
