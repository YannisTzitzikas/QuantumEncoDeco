package com.csd.core.pipeline;

public final class InputPort<T> implements Port<T> {
    private final String id;
    private final Class<T> type;

    public InputPort(String id) {
        this.id = id;
        this.type = null;
    }

    @Override public String id() { return id; }
    @Override public Class<T> payloadType() { return type; }
    @Override public PortDirection direction() { return PortDirection.IN; }
}