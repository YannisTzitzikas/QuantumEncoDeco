package com.csd.config;

public final class EdgeConfig {
    private final String id;    // optional, for logging/debug
    
    private final String from;  // NodeConfig.name
    private final String to;    // NodeConfig.name

    private final String fromPort;  // NodeConfig.RouteConfig.port
    
    public EdgeConfig(String id, String from, String to) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.fromPort = null;
    }

    public EdgeConfig(String id, String from, String to, String fromPort) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.fromPort = fromPort;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getFromPort() {
        return fromPort;
    }

    public String getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "EdgeConfig{id='" + id + "', from='" + from + "' port= '" + fromPort  + "', to='" + to + "'}";
    }
}
