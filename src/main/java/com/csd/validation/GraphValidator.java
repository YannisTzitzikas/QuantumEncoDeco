package com.csd.validation;

import com.csd.config.EdgeConfig;
import com.csd.config.GraphConfig;
import com.csd.config.NodeConfig;
import com.csd.stage.StageRegistry;
import com.csd.stage.provider.StageProvider;
import com.csd.stage.StageProfile;
import com.csd.common.type.TypeRef;
import com.csd.common.type.TypeRef.Arg;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class GraphValidator {

    private final StageRegistry registry;

    public GraphValidator(StageRegistry registry) {
        if (registry == null) throw new IllegalArgumentException("StageRegistry is null");
        this.registry = registry;
    }

    public ValidationResult validate(GraphConfig graph) {
        ValidationResult result = new ValidationResult();
        if (graph == null) {
            result.addError("GraphConfig is null");
            return result;
        }

        // 1) Basic structural checks
        checkNamesUnique(graph, result);
        checkEdgesReferenceExistingNodes(graph, result);

        // 2) DAG check (Kahn’s algorithm) -> topological order
        List<String> topo = checkDagAndToposort(graph, result);
        if (topo == null) {
            // Cycle(s) detected; skip deeper checks because types can’t be validated reliably.
            return result;
        }

        // 3) Stage existence via registry
        Map<String, StageProvider> providers = checkStagesExist(graph, result);
        if (providers.isEmpty()) return result;

        // 4) Type compatibility across edges using StageProfile and TypeRef (topo-driven)
        checkTypeCompatibility(graph, providers, topo, result);

        return result;
    }

    private void checkNamesUnique(GraphConfig graph, ValidationResult result) {
        Set<String> seen = new HashSet<>();
        for (NodeConfig n : graph.getNodes()) {
            String name = n.getName();
            if (name == null || name.isEmpty()) {
                result.addError("Node has missing/blank name: " + n);
                continue;
            }
            if (!seen.add(name)) {
                result.addError("Duplicate node name: " + name);
            }
        }
    }

    private void checkEdgesReferenceExistingNodes(GraphConfig graph, ValidationResult result) {
        Set<String> nodeNames = graph.getNodes().stream()
                .map(NodeConfig::getName)
                .collect(Collectors.toSet());

        for (EdgeConfig e : graph.getEdges()) {
            if (e.getFrom() == null || !nodeNames.contains(e.getFrom())) {
                result.addError("Edge " + e + " references missing 'from' node: " + e.getFrom());
            }
            if (e.getTo() == null || !nodeNames.contains(e.getTo())) {
                result.addError("Edge " + e + " references missing 'to' node: " + e.getTo());
            }
        }
    }

    // 2) DAG check with topological sort
    // Returns topological order when acyclic; null when cycle(s) found (and records errors).
    private List<String> checkDagAndToposort(GraphConfig graph, ValidationResult result) {
        // Nodes
        List<String> nodes = graph.getNodes().stream()
                .map(NodeConfig::getName)
                .collect(Collectors.toList());
        Set<String> nodeSet = new HashSet<>(nodes);

        // Build indegree and adjacency for valid edges only
        Map<String, Integer> indeg = new HashMap<>();
        Map<String, List<String>> adj = new HashMap<>();
        for (String n : nodes) {
            indeg.put(n, 0);
            adj.put(n, new ArrayList<>());
        }
        for (EdgeConfig e : graph.getEdges()) {
            String u = e.getFrom();
            String v = e.getTo();
            if (nodeSet.contains(u) && nodeSet.contains(v)) {
                adj.get(u).add(v);
                indeg.put(v, indeg.get(v) + 1);
            }
        }

        // Kahn’s algorithm
        ArrayDeque<String> q = new ArrayDeque<>();
        for (String n : nodes) if (indeg.get(n) == 0) q.add(n);

        List<String> topo = new ArrayList<>(nodes.size());
        int processed = 0;

        while (!q.isEmpty()) {
            String u = q.removeFirst();
            topo.add(u);
            processed++;
            for (String w : adj.get(u)) {
                indeg.put(w, indeg.get(w) - 1);
                if (indeg.get(w) == 0) q.add(w);
            }
        }

        if (processed != nodes.size()) {
            // Nodes still with indegree > 0 are in cycles
            List<String> cyclic = indeg.entrySet().stream()
                    .filter(en -> en.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .sorted()
                    .collect(Collectors.toList());
            result.addError("Graph contains cycle(s) involving nodes: " + cyclic);
            return null;
        }
        return topo;
    }

    // 3) Stage existence
    private Map<String, StageProvider> checkStagesExist(GraphConfig graph, ValidationResult result) {
        Map<String, StageProvider> providers = new HashMap<>();
        for (NodeConfig n : graph.getNodes()) {
            String stageId = n.getStageConf() != null ? n.getStageConf().getStageId() : null;
            if (stageId == null || stageId.isEmpty()) {
                result.addError("Node '" + n.getName() + "' has missing stage id");
                continue;
            }
            Optional<StageProvider> provider = registry.get(stageId);
            if (!provider.isPresent()) {
                result.addError("Node '" + n.getName() + "' refers to unknown stage id: " + stageId);
            } else {
                providers.put(n.getName(), provider.get());
            }
        }
        return providers;
    }

    // 4) Type Compatibility (topology-driven)
    private void checkTypeCompatibility(GraphConfig graph,
                                        Map<String, StageProvider> providers,
                                        List<String> topoOrder,
                                        ValidationResult result) {
        Map<String, List<String>> incoming = new HashMap<>();
        for (NodeConfig n : graph.getNodes()) incoming.put(n.getName(), new ArrayList<>());
        for (EdgeConfig e : graph.getEdges()) {
            List<String> inc = incoming.get(e.getTo());
            if (inc != null) inc.add(e.getFrom());
        }

        Map<String, StageProfile> profiles = new HashMap<>();
        for (Map.Entry<String, StageProvider> en : providers.entrySet()) {
            profiles.put(en.getKey(), en.getValue().defaultProfile());
        }

        Map<String, TypeRef> resolvedOutput = new HashMap<>();
        Set<String> failed = new HashSet<>();

        // First, handle sources
        for (String node : topoOrder) {
            List<String> preds = incoming.get(node);
            if (preds == null || !preds.isEmpty()) continue;

            StageProfile p = profiles.get(node);
            if (p == null) continue; // stage errors already recorded

            TypeRef in = p.getInputType();
            TypeRef out = p.getOutputType();

            if (in != null && in.hasBoundArgs()) {
                result.addError("Node '" + node + "': input type " + TypeOps.debug(in) +
                        " has bound argument(s) but the node has no incoming edges");
                failed.add(node);
            }
            if (out != null && out.hasBoundArgs()) {
                result.addError("Node '" + node + "': output type " + TypeOps.debug(out) +
                        " has bound argument(s) that depend on input, but the node has no incoming edges");
                failed.add(node);
            }
            if (out != null && !out.hasBoundArgs()) {
                resolvedOutput.put(node, out);
            }
        }

        // Then process in topo order
        for (String node : topoOrder) {
            if (resolvedOutput.containsKey(node) || failed.contains(node)) continue;
            StageProfile profile = profiles.get(node);
            if (profile == null) continue;

            TypeRef expectedInput = profile.getInputType();
            TypeRef stageOutput = profile.getOutputType();
            List<String> preds = incoming.get(node);
            if (preds == null) preds = Collections.emptyList();

            if (preds.isEmpty()) continue; // already handled as source

            // Ensure all predecessors resolved (they should be by topo order unless they failed)
            if (!preds.stream().allMatch(p -> resolvedOutput.containsKey(p))) {
                // If any predecessor failed, this node can’t be resolved properly
                if (preds.stream().anyMatch(failed::contains)) {
                    failed.add(node);
                }
                continue;
            }

            // Unify across all incoming edges to derive single bound X (if any)
            TypeRef boundX = null;
            for (String pred : preds) {
                TypeRef predOut = resolvedOutput.get(pred);
                TypeOps.UnifyResult ur = TypeOps.unify(expectedInput, predOut, boundX);
                if (!ur.ok) {
                    result.addError("Type mismatch into node '" + node + "': expected " +
                            TypeOps.debug(expectedInput) + " but incoming from '" + pred +
                            "' is " + TypeOps.debug(predOut) + " (" + ur.reason + ")");
                    failed.add(node);
                    boundX = null;
                    break;
                }
                boundX = ur.bound;
            }
            if (failed.contains(node)) continue;

            if (expectedInput != null && expectedInput.hasBoundArgs() && boundX == null) {
                result.addError("Node '" + node + "': input has bound argument(s) but could not derive a concrete binding from predecessors");
                failed.add(node);
                continue;
            }

            // Resolve input/output by substituting bounds
            TypeRef resolvedIn = TypeOps.substituteBounds(expectedInput, boundX);
            if (resolvedIn == null || (resolvedIn.hasBoundArgs())) {
                result.addError("Node '" + node + "': failed to resolve input type " +
                        TypeOps.debug(expectedInput));
                failed.add(node);
                continue;
            }

            TypeRef resolvedOutType = TypeOps.substituteBounds(stageOutput, boundX);
            if (resolvedOutType == null || (resolvedOutType.hasBoundArgs())) {
                result.addError("Node '" + node + "': failed to resolve output type " +
                        TypeOps.debug(stageOutput));
                failed.add(node);
                continue;
            }

            resolvedOutput.put(node, resolvedOutType);
        }

        // Optional final pass: strict edges into nodes with concrete expected input (no bounds)
        for (EdgeConfig e : graph.getEdges()) {
            String from = e.getFrom();
            String to = e.getTo();
            TypeRef predOut = resolvedOutput.get(from);
            StageProfile toProf = profiles.get(to);
            if (predOut != null && toProf != null) {
                TypeRef expectedIn = toProf.getInputType();
                if (expectedIn != null && !expectedIn.hasBoundArgs()) {
                    if (!TypeOps.equal(expectedIn, predOut)) {
                        result.addError("Type mismatch: edge " + from + " -> " + to +
                                " sends " + TypeOps.debug(predOut) + " but '" + to +
                                "' expects " + TypeOps.debug(expectedIn));
                    }
                }
            }
        }
    }

    // --- Type operations helper ---
    private static final class TypeOps {

        static final class UnifyResult {
            final boolean ok;
            final TypeRef bound;   // possibly null
            final String reason;   // when !ok
            UnifyResult(boolean ok, TypeRef bound, String reason) {
                this.ok = ok; this.bound = bound; this.reason = reason;
            }
            static UnifyResult ok(TypeRef bound) { return new UnifyResult(true, bound, null); }
            static UnifyResult fail(String reason) { return new UnifyResult(false, null, reason); }
        }

        static UnifyResult unify(TypeRef expectedInput, TypeRef predOut, TypeRef currentBound) {
            if (expectedInput == null || predOut == null) {
                return UnifyResult.fail("null type");
            }

            if (expectedInput.isParameterized()) {
                return equal(expectedInput, predOut)
                        ? UnifyResult.ok(currentBound)
                        : UnifyResult.fail("a type mismatch: expected " + debug(expectedInput) +
                          " vs " + debug(predOut));
            }

            if (!Objects.equals(expectedInput.rawType(), predOut.rawType())) {
                return UnifyResult.fail("raw type mismatch: expected " + expectedInput.rawType().getTypeName() +
                        " vs " + (predOut.rawType() != null ? predOut.rawType().getTypeName() : "null"));
            }

            if (expectedInput.arity() != predOut.arity()) {
                return UnifyResult.fail("arity mismatch: expected " + expectedInput.arity() +
                        " vs " + predOut.arity());
            }

            TypeRef bound = currentBound;
            for (int i = 0; i < expectedInput.arity(); i++) {
                Arg inArg = expectedInput.args().get(i);
                Arg outArg = predOut.args().get(i);

                if (inArg.isBound()) {
                    Optional<TypeRef> outConcrete = outArg.concrete();
                    if (!outConcrete.isPresent()) {
                        return UnifyResult.fail("predecessor provided bound argument where concrete was required at index " + i);
                    }
                    TypeRef candidate = outConcrete.get();
                    if (bound == null) {
                        bound = candidate;
                    } else if (!equal(bound, candidate)) {
                        return UnifyResult.fail("conflicting bound types: " + debug(bound) + " vs " + debug(candidate));
                    }
                } else {
                    Optional<TypeRef> inConcrete = inArg.concrete();
                    Optional<TypeRef> outConcrete = outArg.concrete();
                    if (!inConcrete.isPresent() || !outConcrete.isPresent()) {
                        return UnifyResult.fail("unexpected bound argument in predecessor at index " + i);
                    }
                    if (!equal(inConcrete.get(), outConcrete.get())) {
                        return UnifyResult.fail("argument mismatch at index " + i + ": expected " +
                                debug(inConcrete.get()) + " vs " + debug(outConcrete.get()));
                    }
                }
            }
            return UnifyResult.ok(bound);
        }

        static TypeRef substituteBounds(TypeRef t, TypeRef bound) {
            if (t == null) return null;
            if (!t.isParameterized()) return t;

            Arg[] newArgs = new Arg[t.arity()];
            for (int i = 0; i < t.arity(); i++) {
                Arg a = t.args().get(i);
                if (a.isBound()) {
                    if (bound == null) return null;
                    newArgs[i] = Arg.of(deepSubstitute(bound, bound));
                } else {
                    TypeRef inner = a.concrete().orElse(null);
                    if (inner == null) return null;
                    newArgs[i] = Arg.of(deepSubstitute(inner, bound));
                }
            }
            return TypeRef.parameterized(t.rawType(), newArgs);
        }

        private static TypeRef deepSubstitute(TypeRef t, TypeRef bound) {
            if (t == null) return null;
            if (!t.isParameterized()) return t;
            Arg[] args = new Arg[t.arity()];
            for (int i = 0; i < t.arity(); i++) {
                Arg a = t.args().get(i);
                if (a.isBound()) {
                    if (bound == null) return null;
                    args[i] = Arg.of(deepSubstitute(bound, bound));
                } else {
                    TypeRef inner = a.concrete().orElse(null);
                    if (inner == null) return null;
                    args[i] = Arg.of(deepSubstitute(inner, bound));
                }
            }
            return TypeRef.parameterized(t.rawType(), args);
        }

        static boolean equal(TypeRef a, TypeRef b) {

            if (a == b) return true;
            if (a == null || b == null) return false;
            if (!a.rawType().isAssignableFrom(b.rawType())) { return false;}
            if (a.arity() != b.arity()) return false;

            for (int i = 0; i < a.arity(); i++) {
                Arg aa = a.args().get(i);
                Arg bb = b.args().get(i);
               // if (aa.isBound() || bb.isBound()) return false;
                TypeRef ca = aa.concrete().orElse(null);
                TypeRef cb = bb.concrete().orElse(null);
                if (!equal(ca, cb)) return false;
            }

            return true;
        }

        static String debug(TypeRef t) {
            return t == null ? "null" : t.toString();
        }
    }
}
