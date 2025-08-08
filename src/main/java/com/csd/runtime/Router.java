package com.csd.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import com.csd.core.model.graph.Edge;
import com.csd.core.model.graph.Node;
import com.csd.core.model.graph.DepPolicy;
import com.csd.core.model.msg.EosMsg;
import com.csd.core.model.msg.Msg;

final class Router {
    private final Map<String, Node> nodes = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    // Track barrier EOS per downstream
    private final Map<String, Long> barrierNeeded = new HashMap<>();
    private final Map<String, Long> barrierSeen = new HashMap<>();

    void addNode(Node n){ nodes.put(n.name, n); }
    void addEdge(Edge e){ edges.add(e); }

    void initBarriers(){
        Map<String, Long> needed = edges.stream()
                .filter(e -> e.policy == DepPolicy.BARRIER)
                .collect(Collectors.groupingBy(e -> e.to, Collectors.counting()));
        barrierNeeded.clear(); barrierNeeded.putAll(needed);
        needed.keySet().forEach(k -> barrierSeen.put(k, 0L));
    }

    // Stage calls this to emit messages to all downstreams
    void emit(String from, List<Msg> outputs) {
        for (Msg msg : outputs) {
            for (Edge e : edges) {
                if (!e.from.equals(from)) continue;
                if (e.policy == DepPolicy.STREAM) {
                    // forward DATA and EOS
                    offer(nodes.get(e.to).inbox, msg);
                } else { // BARRIER
                    if (!msg.isEos()) {
                        // suppress data on barrier edges (downstream will read from storage on EOS)
                        continue;
                    }
                    // count EOS from this upstream; if all barrier deps done, deliver single EOS
                    long seen = barrierSeen.merge(e.to, 1L, Long::sum);
                    long need = barrierNeeded.getOrDefault(e.to, 0L);
                    if (seen == need) {
                        offer(nodes.get(e.to).inbox, EosMsg.INSTANCE);
                    }
                }
            }
        }
    }

    private void offer(BlockingQueue<Msg> q, Msg msg){
        try { q.put(msg); } catch (InterruptedException ie){ Thread.currentThread().interrupt(); }
    }

    Node node(String name){ return nodes.get(name); }
    Collection<Node> allNodes(){ return nodes.values(); }
}