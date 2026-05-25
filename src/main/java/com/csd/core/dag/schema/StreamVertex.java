package com.csd.core.dag.schema;

import java.util.ArrayList;
import java.util.List;

import com.csd.core.dag.execution.operations.Operator;

public class StreamVertex<IN, OUT> {
    private final Operator<IN, OUT> payload;
    
    private final List<StreamVertex<?, IN>> incomingEdges = new ArrayList<>();
    private final List<StreamVertex<OUT, ?>> outgoingEdges = new ArrayList<>();

    public StreamVertex(Operator<IN, OUT> payload) {
        this.payload = payload;
    }

    public void connectTo(StreamVertex<OUT, ?> child) {
        this.outgoingEdges.add(child);
        child.incomingEdges.add(this);
    }

    public Operator<IN, OUT> getPayload() { return payload; }
    public List<StreamVertex<OUT, ?>> getOutgoingEdges() { return outgoingEdges; }
    public List<StreamVertex<?, IN>> getIncomingEdges() { return incomingEdges; }
}