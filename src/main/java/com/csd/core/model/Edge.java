package com.csd.core.model;

import java.util.concurrent.BlockingQueue;

public final class Edge {
    private final String id;
    private final Node from;
    private final Node to;
    private final EdgePolicy policy;
    private final BlockingQueue<Object> queue;

    public Edge(String id, Node from, Node to, EdgePolicy policy, int capacity) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.policy = policy;
        this.queue = new java.util.concurrent.ArrayBlockingQueue<>(capacity);
    }

    // Getters
    public String                getId()     { return id; }
    public Node                  getFrom()   { return from; }
    public Node                  getTo()     { return to; }
    public EdgePolicy            getPolicy() { return policy; }
    public BlockingQueue<Object> getQueue()  { return queue; }
}