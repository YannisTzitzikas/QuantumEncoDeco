package com.csd.stage;

import java.util.function.Predicate;

import com.csd.common.type.TypeRef;

public final class ParameterDescriptor {
    private final String name;
    private final TypeRef type;
    private final boolean required;
    private final Object value;
    private final Predicate<Object> validator; // may be null

    private ParameterDescriptor(String name, TypeRef type, boolean required, Object value, Predicate<Object> validator) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.value = value;
        this.validator = validator;
    }

    public String getName() {
        return name;
    }

    public TypeRef getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getValue() {
        return value;
    }

    public Predicate<Object> getValidator() {
        return validator;
    }

    public static Builder of(String name, TypeRef type) {
        return new Builder(name, type);
    }

    public static final class Builder {
        private final String name;
        private final TypeRef type;
        private boolean required = false;
        private Object defaultValue = null;
        private Predicate<Object> validator = null;

        private Builder(String name, TypeRef type) {
            this.name = name;
            this.type = type;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder defaultValue(Object v) {
            this.defaultValue = v;
            return this;
        }

        public Builder validate(Predicate<Object> v) {
            this.validator = v;
            return this;
        }

        public ParameterDescriptor build() {
            return new ParameterDescriptor(name, type, required, defaultValue, validator);
        }
    }
}
