package com.csd.validation;

import com.csd.config.EdgeConfig;
import com.csd.config.GraphConfig;
import com.csd.config.NodeConfig;
import com.csd.stage.StageRegistry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

@Deprecated
public final class GraphValidator {

    private final StageRegistry registry;

    public GraphValidator(StageRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "StageRegistry");
    }

    public ValidationResult validate(GraphConfig graph) {
        ValidationResult result = new ValidationResult();
        if (graph == null) {
            result.addError("GraphConfig is null");
            return result;
        }

        Set<String> nodeNames = extractNodeNames(graph);

        result.merge(checkNamesUnique(graph, nodeNames));
        result.merge(checkEdgesReferenceExistingNodes(graph, nodeNames));
        result.merge(checkUnusedNodes(graph, nodeNames));

        Optional<List<String>> topoOpt = topoSort(graph, nodeNames, result);
        if (!topoOpt.isPresent()) {
            return result;
        }

        // Map<String, StageProvider> providers = resolveStageProviders(graph, result);
        // if (providers.isEmpty()) {
        //     return result;
        // }
        // result.merge(checkTypeCohesion(graph));
        return result;
    }

    // ---------- Structural ----------

    private Set<String> extractNodeNames(GraphConfig graph) {
        // LinkedHashSet preserves declaration order (nice for messages)
        return graph.getNodes().stream()
                .map(NodeConfig::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private ValidationResult checkNamesUnique(GraphConfig graph, Set<String> nodeNames) {
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

    private ValidationResult checkUnusedNodes(GraphConfig graph, Set<String> nodeNames) {
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

    private ValidationResult checkEdgesReferenceExistingNodes(GraphConfig graph, Set<String> nodeNames) {
        ValidationResult result = new ValidationResult();

        // Remove edges that reference missing nodes; issue warnings.
        for (Iterator<EdgeConfig> it = graph.getEdges().iterator(); it.hasNext();) {
            EdgeConfig e = it.next();
            String from = e.getFrom();
            String to = e.getTo();

            boolean missingFrom = (from == null || !nodeNames.contains(from));
            boolean missingTo = (to == null || !nodeNames.contains(to));

            if (missingFrom || missingTo) {
                it.remove();

                StringBuilder reason = new StringBuilder();
                if (missingFrom)
                    reason.append("missing 'from' node: ").append(from);
                if (missingFrom && missingTo)
                    reason.append("; ");
                if (missingTo)
                    reason.append("missing 'to' node: ").append(to);

                String repr = "(" + from + " -> " + to + ")";
                result.addWarning("Removed edge " + repr + " — " + reason);
            }
        }

        return result;
    }

    // ---------- DAG / Toposort ----------
    private Optional<List<String>> topoSort(GraphConfig graph, Set<String> nodeNames, ValidationResult result) {
        Map<String, List<String>> adj = initAdjacency(nodeNames);
        Map<String, Integer> indegree = initIndegree(nodeNames);

        // Only include edges with valid endpoints
        for (EdgeConfig e : graph.getEdges()) {
            String u = e.getFrom();
            String v = e.getTo();
            if (nodeNames.contains(u) && nodeNames.contains(v)) {
                adj.get(u).add(v);
                indegree.put(v, indegree.get(v) + 1);
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
            return Optional.empty();
        }

        return Optional.of(topo);
    }

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

    // ---------- Stage registry ----------
    // private Map<String, StageProvider> resolveStageProviders(GraphConfig graph, ValidationResult result) {
    //     Map<String, StageProvider> providers = new HashMap<>();
    //     for (NodeConfig n : graph.getNodes()) {
    //         String nodeName = n.getName();
    //         String stageId = (n.getStageConf() != null) ? n.getStageConf().getStageId() : null;

    //         if (stageId == null || stageId.isEmpty()) {
    //             result.addError("Node '" + nodeName + "' has missing stage id");
    //             continue;
    //         }

    //         Optional<StageProvider> provider = registry.get(stageId);
    //         if (provider.isPresent()) {
    //             providers.put(nodeName, provider.get());
    //         } else {
    //             result.addError("Node '" + nodeName + "' refers to unknown stage id: " + stageId);
    //         }
    //     }
    //     return providers;
    // }

    // private ValidationResult checkTypeCohesion(GraphConfig graph) {
    //     ValidationResult result = new ValidationResult();

    //     Map<String, NodeConfig> nodeByName = graph.getNodes().stream()
    //             .collect(Collectors.toMap(NodeConfig::getName, n -> n));

    //     Map<String, StageProvider> providerByNode = new HashMap<>();
    //     for (NodeConfig n : graph.getNodes()) {
    //         String stageId = (n.getStageConf() != null) ? n.getStageConf().getStageId() : null;
    //         if (stageId == null || stageId.isEmpty()) {
    //             result.addError("Node '" + n.getName() + "' has missing stage id (cannot check types)");
    //             continue;
    //         }
    //         Optional<StageProvider> sp = registry.get(stageId);
    //         if (!sp.isPresent()) {
    //             result.addError(
    //                     "Node '" + n.getName() + "' refers to unknown stage id '" + stageId + "' (cannot check types)");
    //             continue;
    //         }
    //         providerByNode.put(n.getName(), sp.get());
    //     }

    //     // Build indegree and incoming edges
    //     Map<String, Integer> indeg = new HashMap<>();
    //     Map<String, List<EdgeConfig>> incoming = new HashMap<>();
    //     for (NodeConfig n : graph.getNodes()) {
    //         indeg.put(n.getName(), 0);
    //         incoming.put(n.getName(), new ArrayList<>());
    //     }
    //     for (EdgeConfig e : graph.getEdges()) {
    //         String from = e.getFrom();
    //         String to = e.getTo();
    //         if (!indeg.containsKey(from) || !indeg.containsKey(to))
    //             continue; // invalid edges handled elsewhere
    //         indeg.put(to, indeg.get(to) + 1);
    //         incoming.get(to).add(e);
    //     }

    //     // Helper: extract binding key(s) for target input's bound positions from a
    //     // source output
    //     // Returns a list of TypeRef values corresponding to each bound position in
    //     // target input (in index order).
    //     BiFunction<TypeRef, TypeRef, Optional<List<TypeRef>>> extractBindingKeys = (sourceOut,
    //             targetIn) -> {
    //         // Entire type is bound: the "key" is the whole source type
    //         if (targetIn.isBound()) {
    //             return Optional.of(Collections.singletonList(sourceOut));
    //         }
    //         if (!targetIn.isParameterized()) {
    //             // No bound args present
    //             return Optional.of(Collections.emptyList());
    //         }
    //         // Identify bound positions in target
    //         List<Integer> boundIdx = new ArrayList<>();
    //         for (int i = 0; i < targetIn.arity(); i++) {
    //             if (targetIn.args().get(i).isBound())
    //                 boundIdx.add(i);
    //         }
    //         if (boundIdx.isEmpty()) {
    //             return Optional.of(Collections.emptyList());
    //         }

    //         // Best-effort mapping rules:
    //         // 1) If raw types equal and arity matches: take source args at the same
    //         // indices.
    //         if (Objects.equals(sourceOut.rawType(), targetIn.rawType()) &&
    //                 sourceOut.isParameterized() &&
    //                 sourceOut.arity() == targetIn.arity()) {
    //             List<TypeRef> keys = new ArrayList<>(boundIdx.size());
    //             for (int idx : boundIdx) {
    //                 TypeRef.Arg sArg = sourceOut.args().get(idx);
    //                 if (sArg.isBound() || !sArg.concrete().isPresent())
    //                     return Optional.empty();
    //                 keys.add(sArg.concrete().get());
    //             }
    //             return Optional.of(keys);
    //         }

    //         // 2) Heuristic for common 1-arity hierarchies (e.g., List<E> -> Iterable<E>)
    //         if (targetIn.arity() == 1 && !boundIdx.isEmpty() && boundIdx.size() == 1) {
    //             if (sourceOut.isParameterized() && sourceOut.arity() == 1 &&
    //                     targetIn.rawType().isAssignableFrom(sourceOut.rawType())) {
    //                 TypeRef.Arg sArg = sourceOut.args().get(0);
    //                 if (sArg.isBound() || !sArg.concrete().isPresent())
    //                     return Optional.empty();
    //                 return Optional.of(Collections.singletonList(sArg.concrete().get()));
    //             }
    //         }

    //         // 3) If target has bound args but we cannot infer mapping, fail
    //         return Optional.empty();
    //     };

    //     // 1) First-stage nodes must have concrete input and output
    //     for (Map.Entry<String, Integer> en : indeg.entrySet()) {
    //         if (en.getValue() == 0) {
    //             String node = en.getKey();
    //             StageProvider sp = providerByNode.get(node);
    //             if (sp == null)
    //                 continue; // already reported
    //             TypeRef in = sp.defaultProfile().getInputType();
    //             TypeRef out = sp.defaultProfile().getOutputType();
    //             if (!TypeRefUtils.isConcrete(in) || !TypeRefUtils.isConcrete(out)) {
    //                 System.out.println(in.isBound() + " " + out.isBound());
    //                 result.addError(
    //                         "Source node '" + node + "' must have concrete input and output types, found input=" +
    //                                 (in == null ? "null" : in.toString()) + ", output=" +
    //                                 (out == null ? "null" : out.toString()));
    //             }
    //         }
    //     }

    //     // Precompute node input/output types
    //     Map<String, TypeRef> nodeInType = new HashMap<>();
    //     Map<String, TypeRef> nodeOutType = new HashMap<>();
    //     for (String node : nodeByName.keySet()) {
    //         StageProvider sp = providerByNode.get(node);
    //         if (sp != null) {
    //             nodeInType.put(node, sp.defaultProfile().getInputType());
    //             nodeOutType.put(node, sp.defaultProfile().getOutputType());
    //         }
    //     }

    //     // 2) For every edge, output must be assignable to following input
    //     for (EdgeConfig e : graph.getEdges()) {
    //         String u = e.getFrom(), v = e.getTo();
    //         if (!nodeOutType.containsKey(u) || !nodeInType.containsKey(v))
    //             continue;
    //         TypeRef outU = nodeOutType.get(u);
    //         TypeRef inV = nodeInType.get(v);
    //         if (!TypeRefUtils.isAssignable(outU, inV)) {
    //             result.addError("Type mismatch on edge " + u + " -> " + v +
    //                     ": output " + outU + " is not assignable to input " + inV);
    //         }
    //     }

    //     // 3) and 4) For each node, all incoming must be assignable; if input uses
    //     // BOUND, they must agree on the same bound(s)
    //     for (String node : nodeByName.keySet()) {
    //         List<EdgeConfig> inEdges = incoming.get(node);
    //         if (inEdges == null || inEdges.isEmpty())
    //             continue;

    //         TypeRef targetIn = nodeInType.get(node);
    //         if (targetIn == null)
    //             continue;

    //         // 3) If any incoming is not assignable to input -> error
    //         boolean anyNotAssignable = false;
    //         for (EdgeConfig e : inEdges) {
    //             TypeRef srcOut = nodeOutType.get(e.getFrom());
    //             if (srcOut == null)
    //                 continue;
    //             if (!TypeRefUtils.isAssignable(srcOut, targetIn)) {
    //                 anyNotAssignable = true;
    //                 result.addError("Node '" + node + "': incoming from '" + e.getFrom() + "' has output " +
    //                         srcOut + " not assignable to input " + targetIn);
    //             }
    //         }
    //         // If input is BOUND or has bound args, enforce same bound(s) across all inputs
    //         if ((targetIn.isBound() || targetIn.hasBoundArgs()) && !inEdges.isEmpty()) {
    //             List<TypeRef> firstKeys = null;
    //             String firstFrom = null;
    //             boolean bindingError = false;

    //             for (EdgeConfig e : inEdges) {
    //                 TypeRef srcOut = nodeOutType.get(e.getFrom());
    //                 if (srcOut == null)
    //                     continue;
    //                 Optional<List<TypeRef>> keysOpt = extractBindingKeys.apply(srcOut, targetIn);
    //                 if (!keysOpt.isPresent()) {
    //                     result.addError("Node '" + node + "': cannot infer bound argument(s) from incoming '" +
    //                             e.getFrom() + "' with output " + srcOut + " against input pattern " + targetIn);
    //                     bindingError = true;
    //                     continue;
    //                 }
    //                 List<TypeRef> keys = keysOpt.get();
    //                 if (firstKeys == null) {
    //                     firstKeys = keys;
    //                     firstFrom = e.getFrom();
    //                 } else {
    //                     if (keys.size() != firstKeys.size()) {
    //                         result.addError("Node '" + node + "': inconsistent bound arity between inputs '" +
    //                                 firstFrom + "' and '" + e.getFrom() + "'");
    //                         bindingError = true;
    //                     } else {
    //                         for (int i = 0; i < keys.size(); i++) {
    //                             TypeRef a = firstKeys.get(i);
    //                             TypeRef b = keys.get(i);
    //                             if (!TypeRefUtils.deepEquals(a, b)) {
    //                                 result.addError("Node '" + node + "': bound type mismatch at position " + i +
    //                                         " between inputs '" + firstFrom + "' (" + a + ") and '" +
    //                                         e.getFrom() + "' (" + b + ") for input pattern " + targetIn);
    //                                 bindingError = true;
    //                                 break;
    //                             }
    //                         }
    //                     }
    //                 }
    //             }

    //             // If any binding error or non-assignable edge found, we've already recorded
    //             // error(s).
    //             if (!bindingError && !anyNotAssignable && firstKeys == null) {
    //                 // No incoming keys could be established; treat as error for robustness
    //                 result.addError("Node '" + node + "': unable to establish bound type(s) for input " + targetIn);
    //             }
    //         }
    //     }

    //     return result;
    // }
}
