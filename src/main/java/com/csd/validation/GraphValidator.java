package com.csd.validation;

import com.csd.common.type.TypeRef;
import com.csd.common.type.TypeRefUtils;
import com.csd.config.EdgeConfig;
import com.csd.config.GraphConfig;
import com.csd.config.NodeConfig;
import com.csd.config.RouteConfig;
import com.csd.core.stage.StageDescriptor;
import com.csd.core.stage.StageRegistry;
import com.csd.core.split.SplitterDescriptor;
import com.csd.core.split.SplitterRegistry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

@Deprecated
public final class GraphValidator {

    private final StageRegistry     stageRegistry;
    private final SplitterRegistry  splitterRegistry;

    public GraphValidator(StageRegistry stageRegistry, SplitterRegistry splitterRegistry) {
        this.stageRegistry    = Objects.requireNonNull(stageRegistry, "StageRegistry");
        this.splitterRegistry = Objects.requireNonNull(splitterRegistry, "StageRegistry");
    }

    public ValidationResult validate(GraphConfig graph) {
        ValidationResult result = new ValidationResult();
        if (graph == null) {
            result.addError("GraphConfig is null");
            return result;
        }

        Map<String, NodeConfig> nodeMap   = extractNodeMap(graph);
        Set<String>             nodeNames = extractNodeNames(graph);

        // Structural Checks
        result.merge(validateUniqueNames(graph, nodeNames));
        result.merge(validateEdgesReferenceExistingNodes(graph, nodeNames, nodeMap));
        result.merge(validatekUnusedNodes(graph, nodeNames));
        List<String> topology = validateTopology(graph, nodeNames, result);

        // If invalid, skip the type cohesion check
        if(result.isValid()) 
            result.merge(validateTypeCohesion(graph, nodeNames, topology, nodeMap));

        return result;
    }

    private ValidationResult validateUniqueNames(GraphConfig graph, Set<String> nodeNames) {
        ValidationResult result = new ValidationResult();

        Map<String, Long> counts = graph.getNodes().stream()
                .map(NodeConfig::getName)
                .collect(Collectors.groupingBy(n -> n, LinkedHashMap::new, Collectors.counting()));

        for (NodeConfig n : graph.getNodes()) {
            String name = n.getName();
            if (name == null || name.isEmpty()) {
                result.addError("Node has missing/blank name: " + n);
            }
        }

        counts.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() > 1)
                .forEach(e -> result.addError("Duplicate node name: " + e.getKey()));

        return result;
    }

    private ValidationResult validatekUnusedNodes(GraphConfig graph, Set<String> nodeNames) {
        ValidationResult result = new ValidationResult();

        // Collect all node names that are referenced by edges.
        Set<String> used = new HashSet<>();
        for (EdgeConfig e : graph.getEdges()) {
            if (e.getFrom() != null)
                used.add(e.getFrom());
            if (e.getTo() != null)
                used.add(e.getTo());
        }

        // Remove nodes not referenced by any edge; issue warnings.
        for (Iterator<NodeConfig> it = graph.getNodes().iterator(); it.hasNext();) {
            NodeConfig n = it.next();
            String name = n.getName();

            if (name == null || !used.contains(name)) {
                it.remove();
                if (name != null) {
                    nodeNames.remove(name); 
                    result.addWarning("Removed unused node '" + name + "'");
                } else {
                    result.addWarning("Removed unnamed unused node");
                }
            }
        }

        return result;
    }

    private ValidationResult validateEdgesReferenceExistingNodes(GraphConfig graph, Set<String> nodeNames, Map<String, NodeConfig> nodeMap) {
        ValidationResult result = new ValidationResult();

        // Remove edges that reference missing nodes; issue warnings.
        for (Iterator<EdgeConfig> it = graph.getEdges().iterator(); it.hasNext();) {
            EdgeConfig e    = it.next();
            String from     = e.getFrom();
            String fromPort = e.getFromPort();
            String to       = e.getTo();

            boolean missingFrom = (from == null || !nodeNames.contains(from));
            boolean missingTo   = (to == null || !nodeNames.contains(to));
            boolean missingFromPort = false;

            if (!missingFrom && fromPort != null) {
                NodeConfig fromNode = nodeMap.get(from);
                Map<String, String> ports = fromNode.getRouteConf().getEffectivePorts();
                missingFromPort = !ports.containsKey(fromPort);
            }

            if (missingFrom || missingTo) {
                it.remove();
                StringBuilder reason = new StringBuilder();

                if (missingFrom)                        reason.append("missing 'from' node: ").append(from);
                if (missingFrom && 
                    (missingTo || missingFromPort))     reason.append("; ");
                if (missingTo)                          reason.append("missing 'to' node: ").append(to);
                if (missingFromPort)                    reason.append("missing 'from' port: ").append(fromPort);
    
                String repr = "(" + from + "." + fromPort + " -> " + to + ")";
                result.addWarning("Removed edge " + repr + " — " + reason);
            }
        }

        return result;
    }

    /**
    * It is used to check whether the Graph is Acyclic or not.
    * 
    * This is called after {@link checkUnusedNodes} and {@link checkEdgesReferenceExistingNodes} in validate
    * so we can assume that there are no more unused edges and invalid ports. Thus we can just check whether
    * the nodes themselved do not form a cyrcle.
    * 
    * @param graph     The Graph Configuration
    * @param nodeNames The Set of all the Node Names
    * @return The topology of the graph if valid or null
    */
    private List<String> validateTopology(GraphConfig graph, Set<String> nodeNames, ValidationResult result) {
        Map<String, List<String>> adj = initAdjacency(nodeNames);
        Map<String, Integer> indegree = initIndegree(nodeNames);
    
        // Only include edges with valid endpoints
        for (EdgeConfig e : graph.getEdges()) {
            String fromNode = e.getFrom();
            String toNode   = e.getTo();
    
            if (nodeNames.contains(fromNode) && nodeNames.contains(toNode)) {
                // DAG is node-based, so we ignore port in adjacency but validate it elsewhere
                adj.get(fromNode).add(toNode);
                indegree.put(toNode, indegree.get(toNode) + 1);
            }
        }
    
        Deque<String> q = new ArrayDeque<>();
        for (String n : nodeNames) {
            if (indegree.get(n) == 0)
                q.addLast(n);
        }
    
        List<String> topo = new ArrayList<>(nodeNames.size());
        while (!q.isEmpty()) {
            String u = q.removeFirst();
            topo.add(u);
            for (String w : adj.get(u)) {
                int next = indegree.get(w) - 1;
                indegree.put(w, next);
                if (next == 0)
                    q.addLast(w);
            }
        }
    
        if (topo.size() != nodeNames.size()) {
            List<String> cyclic = indegree.entrySet().stream()
                    .filter(en -> en.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .sorted()
                    .collect(Collectors.toList());
            result.addError("Graph contains cycle(s) involving nodes: " + cyclic);
            return null;
        }
    
        return topo;
    }

    private ValidationResult validateTypeCohesion(GraphConfig graph,
                                                  Set<String> nodeNames,
                                                  List<String> topology,
                                                  Map<String, NodeConfig> nodeMap)
    {
        // Holds resolved types for outputs: (nodeName, portName) -> concrete TypeRef
        Map<String, Map<String, TypeRef>> resolvedOutputs = new HashMap<>();
        ValidationResult                  result         = new ValidationResult();

        for (String nodeName : topology) {

            NodeConfig  node  = nodeMap.get(nodeName);
            RouteConfig route = node.getRouteConf();

            String      stageId     = route.getStageConf().getStageId();
            String      splitterId  = (route.getSplitterConf() == null) ? null : route.getSplitterConf().getType();  

            StageDescriptor    stageDesc    = stageRegistry.getDescriptor(stageId).get();
            SplitterDescriptor splitterDesc = splitterRegistry.getDescriptor(splitterId).get();
        
            List<TypeRef> inboundTypes = collectInboundTypes(graph, nodeName, resolvedOutputs);
        
            TypeRef expectedInput = stageDesc.getInputType();
            TypeRef commonInput   = TypeRefUtils.GCTypeInList(inboundTypes);

            if (commonInput == null)
            {
                result.addError("Type mismatch between input nodes feeding '" + nodeName);
                return result;
            } 
            
            TypeRef resolvedInput = TypeRefUtils.GCTypeInList(inboundTypes);

            if (resolvedInput == null){
                result.addError("Inbound unified type " + commonInput + " is not compatible with expected bound input " +
                                expectedInput + " for node '" + nodeName + "'");
                return result;
            } 
        
            if (splitterDesc == null) {
                TypeRef outType = TypeRefUtils.GCType(stageDesc.getOutputType(), resolvedInput);
                resolvedOutputs.computeIfAbsent(nodeName, k -> new HashMap<>())
                            .put(RouteConfig.DEFAULT_OUT_PORT, outType);
            } else {
                TypeRef stageOutConcrete = TypeRefUtils.GCType(stageDesc.getOutputType(), resolvedInput);
                if (!TypeRefUtils.isAssignable(stageOutConcrete, splitterDesc.getInputType())) {
                    result.addError("Splitter input type mismatch on node " + nodeName);
                    return result;
                }
                Map<String, String> ports = node.getRouteConf().getSplitterConf().getPortMappings();
                for (String portName : ports.keySet()) {
                    TypeRef portOut = TypeRefUtils.GCType(splitterDesc.getOutputType(), stageOutConcrete);
                    resolvedOutputs.computeIfAbsent(nodeName, k -> new HashMap<>())
                                .put(portName, portOut);
                }
            }
        }

        return result;
    }




    //-------- UTILITIES -------------
    private Map<String, List<String>> initAdjacency(Set<String> nodes) {
        Map<String, List<String>> adj = new HashMap<>(Math.max(16, nodes.size() * 2));
        for (String n : nodes)
            adj.put(n, new ArrayList<>());
        return adj;
    }

    private Map<String, Integer> initIndegree(Set<String> nodes) {
        Map<String, Integer> indeg = new HashMap<>(Math.max(16, nodes.size() * 2));
        for (String n : nodes)
            indeg.put(n, 0);
        return indeg;
    }   

    
    private Set<String> extractNodeNames(GraphConfig graph) {
        // LinkedHashSet preserves declaration order (nice for messages)
        return graph.getNodes().stream()
                .map(NodeConfig::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, NodeConfig> extractNodeMap(GraphConfig graph) {
        Map<String, NodeConfig> nodeMap = new LinkedHashMap<>();
        for (NodeConfig node : graph.getNodes()) {
            nodeMap.put(node.getName(), node);
        }

        return nodeMap;
    }

    // Helpers for validation checking
    private List<TypeRef> collectInboundTypes(GraphConfig graph,
                                          String targetNode,
                                          Map<String, Map<String, TypeRef>> resolvedOutputs) {
        List<TypeRef> inbound = new ArrayList<>();
        for (EdgeConfig e : graph.getEdges()) {
            if (targetNode.equals(e.getTo())) {
                String from     = e.getFrom();
                String fromPort = e.getFromPort();
    
                Map<String, TypeRef> fromPorts = resolvedOutputs.get(from);
                if (fromPorts == null) continue; // Upstream not resolved yet (shouldn't happen in topo order)
                TypeRef t = fromPorts.get(fromPort);
                if (t != null) inbound.add(t);
            }
        }
        return inbound;
    }
}
