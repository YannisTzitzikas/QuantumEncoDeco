package com.csd.config;

import java.util.Objects;

public final class NodeConfig {
    private final String name;             // unique graph name
    private final RouteConfig routeConf;   // routing + stage + optional splitter + ports

    public NodeConfig(String name, RouteConfig routeConf) {
        this.name = Objects.requireNonNull(name, "name");
        this.routeConf = Objects.requireNonNull(routeConf, "routeConf");
    }

    public String getName() {
        return name;
    }

    public RouteConfig getRouteConf() {
        return routeConf;
    }

    @Override
    public String toString() {
        return "NodeConfig{name='" + name + "', routeConf=" + routeConf + "}";
    }
}
