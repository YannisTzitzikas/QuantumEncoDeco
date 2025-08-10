package com.csd.config;

public final class EdgeConfig {
    private final String id;    // optional, for logging/debug
    private final String from;  // NodeConfig.name
    private final String to;    // NodeConfig.name

    public EdgeConfig(String id, String from, String to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "EdgeConfig{id='" + id + "', from='" + from + "', to='" + to + "'}";
    }
}
