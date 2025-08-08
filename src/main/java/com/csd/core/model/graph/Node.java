package com.csd.core.model.graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.csd.core.model.msg.Msg;

public final class Node {
    public final String name;
    public final Stage<?,?> stage;
    public final BlockingQueue<Msg> inbox;
    Node(String name, Stage<?,?> stage, int capacity){
        this.name = name;
        this.stage = stage;
        this.inbox = new ArrayBlockingQueue<>(capacity);
    }
}