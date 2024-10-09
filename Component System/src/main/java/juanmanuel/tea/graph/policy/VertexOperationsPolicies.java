package juanmanuel.tea.graph.policy;

import juanmanuel.tea.graph.GraphElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public final class VertexOperationsPolicies {
    private final PolicyCheckAlgorithm defaultPolicyCheckAlgorithm;
    private Map<Policy.NullaryPolicy, PolicyState> nullaryPolicyStateMap;
    private Map<Policy.UnaryPolicy<?>, WeakHashMap<GraphElement, PolicyState>> unaryObjectPolicyStateMap;
    private Map<Policy.UnaryPolicy<?>, HashMap<Class<? extends GraphElement>, PolicyState>> unaryTypePolicyStateMap;

    public VertexOperationsPolicies(PolicyCheckAlgorithm defaultPolicyCheckAlgorithm) {
        this.defaultPolicyCheckAlgorithm = defaultPolicyCheckAlgorithm;
    }

    public VertexOperationsPolicies() {
        this(PolicyCheckAlgorithm.OBJECT_OR_TYPE);
    }

    public <P extends VertexPolicy & Policy.NullaryPolicy> PolicyState stateOf(P nullaryVertexPolicy) {
        if (nullaryPolicyStateMap != null)
            return nullaryPolicyStateMap.getOrDefault(nullaryVertexPolicy, PolicyState.UNSET);

        return PolicyState.UNSET;
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> PolicyState stateOf(P unaryPolicy, G graphElement, PolicyCheckAlgorithm algorithm) {
        Objects.requireNonNull(unaryPolicy);
        Objects.requireNonNull(graphElement);
        Objects.requireNonNull(algorithm);

        return switch (algorithm) {
            case OBJECT_OVER_TYPE -> unaryObjectPolicyStateMap == null
                    ? PolicyState.UNSET
                    : unaryObjectPolicyStateMap
                        .getOrDefault(unaryPolicy, new WeakHashMap<>())
                        .getOrDefault(graphElement, PolicyState.UNSET);

            case TYPE_OVER_OBJECT -> unaryTypePolicyStateMap == null
                    ? PolicyState.UNSET
                    : unaryTypePolicyStateMap
                        .getOrDefault(unaryPolicy, new HashMap<>())
                        .getOrDefault(graphElement.getClass(), PolicyState.UNSET);

            case OBJECT_AND_TYPE -> unaryObjectPolicyStateMap == null
                    ? PolicyState.UNSET
                    : unaryObjectPolicyStateMap
                        .getOrDefault(unaryPolicy, new WeakHashMap<>())
                        .getOrDefault(graphElement, PolicyState.UNSET)
                        .and(unaryTypePolicyStateMap == null
                                ? PolicyState.UNSET
                                : unaryTypePolicyStateMap
                                    .getOrDefault(unaryPolicy, new HashMap<>())
                                    .getOrDefault(graphElement.getClass(), PolicyState.UNSET));

            case OBJECT_OR_TYPE -> unaryObjectPolicyStateMap == null
                    ? PolicyState.UNSET.or(unaryTypePolicyStateMap == null
                        ? PolicyState.UNSET
                        : unaryTypePolicyStateMap
                            .getOrDefault(unaryPolicy, new HashMap<>())
                            .getOrDefault(graphElement.getClass(), PolicyState.UNSET))
                    : unaryObjectPolicyStateMap
                        .getOrDefault(unaryPolicy, new WeakHashMap<>())
                        .getOrDefault(graphElement, PolicyState.UNSET)
                        .or(unaryTypePolicyStateMap == null
                                ? PolicyState.UNSET
                                : unaryTypePolicyStateMap
                                    .getOrDefault(unaryPolicy, new HashMap<>())
                                    .getOrDefault(graphElement.getClass(), PolicyState.UNSET));
        };
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> PolicyState stateOf(P unaryPolicy, G graphElement) {
        Objects.requireNonNull(unaryPolicy);
        Objects.requireNonNull(graphElement);

        return stateOf(unaryPolicy, graphElement, defaultPolicyCheckAlgorithm);
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> PolicyState stateOf(P unaryPolicy, Class<G> graphElementType) {
        Objects.requireNonNull(unaryPolicy);
        Objects.requireNonNull(graphElementType);

        if (unaryTypePolicyStateMap != null)
            return unaryTypePolicyStateMap
                    .getOrDefault(unaryPolicy, new HashMap<>())
                    .getOrDefault(graphElementType, PolicyState.UNSET);

        return PolicyState.UNSET;
    }

    public <P extends VertexPolicy & Policy.NullaryPolicy> boolean isAccepted(P nullaryPolicy) {
        return stateOf(nullaryPolicy) == PolicyState.ACCEPT;
    }

    public <P extends VertexPolicy & Policy.NullaryPolicy> boolean isRejected(P nullaryPolicy) {
        return stateOf(nullaryPolicy) == PolicyState.REJECT;
    }

    public <P extends VertexPolicy & Policy.NullaryPolicy> boolean isUnset(P nullaryPolicy) {
        return stateOf(nullaryPolicy) == PolicyState.UNSET;
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> boolean isAccepted(P unaryPolicy, G graphElement) {
        return stateOf(unaryPolicy, graphElement) == PolicyState.ACCEPT;
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> boolean isRejected(P unaryPolicy, G graphElement) {
        return stateOf(unaryPolicy, graphElement) == PolicyState.REJECT;
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> boolean isUnset(P unaryPolicy, G graphElement) {
        return stateOf(unaryPolicy, graphElement) == PolicyState.UNSET;
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> boolean isAccepted(P unaryPolicy, Class<G> graphElementType) {
        return stateOf(unaryPolicy, graphElementType) == PolicyState.ACCEPT;
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> boolean isRejected(P unaryPolicy, Class<G> graphElementType) {
        return stateOf(unaryPolicy, graphElementType) == PolicyState.REJECT;
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> boolean isUnset(P unaryPolicy, Class<G> graphElementType) {
        return stateOf(unaryPolicy, graphElementType) == PolicyState.UNSET;
    }

    public <P extends VertexPolicy & Policy.NullaryPolicy> void accept(P nullaryPolicy) {
        if (nullaryPolicyStateMap == null)
            nullaryPolicyStateMap = new HashMap<>();

        nullaryPolicyStateMap.put(nullaryPolicy, PolicyState.ACCEPT);
    }

    public <P extends VertexPolicy & Policy.NullaryPolicy> void reject(P nullaryPolicy) {
        if (nullaryPolicyStateMap == null)
            nullaryPolicyStateMap = new HashMap<>();

        nullaryPolicyStateMap.put(nullaryPolicy, PolicyState.REJECT);
    }

    public <P extends VertexPolicy & Policy.NullaryPolicy> void unset(P nullaryPolicy) {
        if (nullaryPolicyStateMap == null)
            return;

        nullaryPolicyStateMap.put(nullaryPolicy, PolicyState.UNSET);
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void accept(P unaryPolicy, G graphElement) {
        if (unaryObjectPolicyStateMap == null)
            unaryObjectPolicyStateMap = new HashMap<>();

        unaryObjectPolicyStateMap.computeIfAbsent(unaryPolicy, _ -> new WeakHashMap<>()).put(graphElement, PolicyState.ACCEPT);
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void reject(P unaryPolicy, G graphElement) {
        if (unaryObjectPolicyStateMap == null)
            unaryObjectPolicyStateMap = new HashMap<>();

        unaryObjectPolicyStateMap.computeIfAbsent(unaryPolicy, _ -> new WeakHashMap<>()).put(graphElement, PolicyState.REJECT);
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void unset(P unaryPolicy, G graphElement) {
        if (unaryObjectPolicyStateMap == null)
            return;

        unaryObjectPolicyStateMap.computeIfAbsent(unaryPolicy, _ -> new WeakHashMap<>()).put(graphElement, PolicyState.UNSET);
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void accept(P unaryPolicy, Class<? extends G> graphElementType) {
        if (unaryTypePolicyStateMap == null)
            unaryTypePolicyStateMap = new HashMap<>();

        unaryTypePolicyStateMap.computeIfAbsent(unaryPolicy, _ -> new HashMap<>()).put(graphElementType, PolicyState.ACCEPT);
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void reject(P unaryPolicy, Class<? extends G> graphElementType) {
        if (unaryTypePolicyStateMap == null)
            unaryTypePolicyStateMap = new HashMap<>();

        unaryTypePolicyStateMap.computeIfAbsent(unaryPolicy, _ -> new HashMap<>()).put(graphElementType, PolicyState.REJECT);
    }

    public <P extends VertexPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void unset(P unaryPolicy, Class<G> graphElementType) {
        if (unaryTypePolicyStateMap == null)
            return;

        unaryTypePolicyStateMap.computeIfAbsent(unaryPolicy, _ -> new HashMap<>()).put(graphElementType, PolicyState.UNSET);
    }
}
