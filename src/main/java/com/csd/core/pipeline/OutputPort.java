package com.csd.core.pipeline;

public final class OutputPort<T> implements Port<T> {
    private final String id;
    private final Class<T> type;

    public OutputPort(String id) {
        this.id = id;
        this.type = null;
    }

    @Override public String id() { return id; }
    @Override public Class<T> payloadType() { return type; }
    @Override public PortDirection direction() { return PortDirection.OUT; }
}