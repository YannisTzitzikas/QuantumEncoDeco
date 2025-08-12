package com.csd.core.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public final class Edge {
    private final String id;
    private final Node from;
    private final Node to;
    private final BlockingQueue<Message> queue;

    public Edge(String id, Node from, Node to, int capacity) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    // Getters
    public String                    getId()     { return id; }
    public Node                      getFrom()   { return from; }
    public Node                      getTo()     { return to; }
    public BlockingQueue<Message>    getQueue()  { return queue; }
}