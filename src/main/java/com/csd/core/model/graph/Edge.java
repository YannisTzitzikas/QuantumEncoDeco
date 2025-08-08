package com.csd.core.model.graph;

public final class Edge {
    public final String from;
    public final String to;
    public final DepPolicy policy;
    Edge(String from, String to, DepPolicy policy){ this.from = from; this.to = to; this.policy = policy; }
}