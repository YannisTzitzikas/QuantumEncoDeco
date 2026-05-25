package com.csd.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.csd.core.execution.operations.FilterOp;
import com.csd.core.execution.operations.MapOp;
import com.csd.core.execution.operations.SinkOp;
import com.csd.core.schema.StreamVertex;

public class StreamBuilder<T> {
    private final StreamEnvironment graph;
    private final List<StreamVertex<?, T>> tailNodes;

    StreamBuilder(StreamEnvironment graph, List<StreamVertex<?, T>> nodes) {
        this.graph = graph;
        this.tailNodes = new ArrayList<>(nodes);
    }

    public <R> StreamBuilder<R> map(Function<T, R> mapper) {
        StreamVertex<T, R> nextNode = new StreamVertex<>(new MapOp<>(mapper));
        
        // Fan-in: Wire all current tail nodes to this single new map node
        for (StreamVertex<?, T> tail : tailNodes) {
            tail.connectTo(nextNode);
        }
        
        graph.registerNode(nextNode);
        return new StreamBuilder<>(graph, List.of(nextNode));
    }

    public StreamBuilder<T> filter(Predicate<T> predicate) {
        StreamVertex<T, T> nextNode = new StreamVertex<>(new FilterOp<>(predicate));
        
        for (StreamVertex<?, T> tail : tailNodes) {
            tail.connectTo(nextNode);
        }
        
        graph.registerNode(nextNode);
        return new StreamBuilder<>(graph, List.of(nextNode));
    }

    @SafeVarargs
    public final StreamBuilder<T> merge(StreamBuilder<T>... branches) {
        List<StreamVertex<?, T>> combinedTails = new ArrayList<>(this.tailNodes);
        for (StreamBuilder<T> branch : branches) {
            combinedTails.addAll(branch.tailNodes);
        }
        // No new nodes created, just expanded set of tail nodes
        return new StreamBuilder<>(graph, combinedTails);
    }

    public void sink(Consumer<T> consumer) {
        StreamVertex<T, Void> sinkNode = new StreamVertex<>(new SinkOp<>(consumer));
        for (StreamVertex<?, T> tail : tailNodes) {
            tail.connectTo(sinkNode);
        }
        graph.registerNode(sinkNode);
    }
}