package com.csd.core.model;

public final class Node {
    // Add information such as the Stage it represents,
    // ???
    // The Node ID
    // Node State
    public enum NodeState {
        CREATED, READY, RUNNING,
        DRAINING, DONE, FAILED
    }
}