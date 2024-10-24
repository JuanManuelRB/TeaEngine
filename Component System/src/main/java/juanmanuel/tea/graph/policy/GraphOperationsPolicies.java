package juanmanuel.tea.graph.policy;

import juanmanuel.tea.graph.GraphElement;

import java.util.*;

public final class GraphOperationsPolicies {
    private final PolicyCheckAlgorithm defaultPolicyCheckAlgorithm;
    private Map<Policy.NullaryPolicy, PolicyState> nullaryPolicyStateMap;
    private Map<Policy.UnaryPolicy<?>, WeakHashMap<GraphElement, PolicyState>> unaryObjectPolicyStateMap;
    private Map<Policy.UnaryPolicy<?>, HashMap<Class<? extends GraphElement>, PolicyState>> unaryTypePolicyStateMap;
    private Map<Policy.BinaryPolicy<?, ?>, HashMap<Pair<GraphElement, GraphElement>, PolicyState>> binaryObjectPolicyStateMap; // TODO Use WeakReference
    private Map<Policy.BinaryPolicy<?, ?>, HashMap<Pair<Class<? extends GraphElement>, Class<? extends GraphElement>>, PolicyState>> binaryTypePolicyStateMap;

    public record Pair<F, S>(F first, S second) {}

    public GraphOperationsPolicies(PolicyCheckAlgorithm defaultPolicyCheckAlgorithm) {
        this.defaultPolicyCheckAlgorithm = defaultPolicyCheckAlgorithm;
    }

    public GraphOperationsPolicies() {
        this(PolicyCheckAlgorithm.OBJECT_OR_TYPE);
    }

    /**
     * Returns the state of the given policy.
     * @param nullaryVertexPolicy the policy to check
     * @return the state of the policy
     * @param <P> the type of the policy
     */
    public <P extends GraphPolicy & Policy.NullaryPolicy> PolicyState stateOf(P nullaryVertexPolicy) {
        if (nullaryPolicyStateMap != null)
            return nullaryPolicyStateMap.getOrDefault(nullaryVertexPolicy, PolicyState.UNSET);

        return PolicyState.UNSET;
    }

    /**
     * Returns the state of the given policy for the given graph element using the given algorithm.
     * @param unaryPolicy the policy to check
     * @param graphElement the graph element to check
     * @param algorithm the algorithm to use
     * @return the state of the policy
     * @param <P> the type of the policy
     * @param <E> the type of the graph element
     */
    public <P extends GraphPolicy & Policy.UnaryPolicy<E>, E extends GraphElement>
    PolicyState stateOf(P unaryPolicy, E graphElement, PolicyCheckAlgorithm algorithm) {
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

            case OBJECT_AND_TYPE -> {
                PolicyState objectState = unaryObjectPolicyStateMap == null
                        ? PolicyState.UNSET
                        : unaryObjectPolicyStateMap
                        .getOrDefault(unaryPolicy, new WeakHashMap<>())
                        .getOrDefault(graphElement, PolicyState.UNSET);

                PolicyState typeState = unaryTypePolicyStateMap == null
                        ? PolicyState.UNSET
                        : findMostSpecificTypePolicy(
                                unaryTypePolicyStateMap.getOrDefault(unaryPolicy, new HashMap<>()),
                                graphElement.getClass()
                        );

                yield objectState.and(typeState);
            }

            case OBJECT_OR_TYPE -> {
                PolicyState objectState = unaryObjectPolicyStateMap == null
                        ? PolicyState.UNSET
                        : unaryObjectPolicyStateMap
                            .getOrDefault(unaryPolicy, new WeakHashMap<>())
                            .getOrDefault(graphElement, PolicyState.UNSET);

                PolicyState typeState = unaryTypePolicyStateMap == null
                        ? PolicyState.UNSET
                        : findMostSpecificTypePolicy(
                                unaryTypePolicyStateMap.getOrDefault(unaryPolicy, new HashMap<>()),
                                graphElement.getClass()
                        );

                yield objectState.or(typeState);

//                yield unaryObjectPolicyStateMap == null
//                        ? PolicyState.UNSET.or(unaryTypePolicyStateMap == null
//                        ? PolicyState.UNSET
//                        : unaryTypePolicyStateMap
//                        .getOrDefault(unaryPolicy, new HashMap<>())
//                        .getOrDefault(graphElement.getClass(), PolicyState.UNSET))
//                        : unaryObjectPolicyStateMap
//                        .getOrDefault(unaryPolicy, new WeakHashMap<>())
//                        .getOrDefault(graphElement, PolicyState.UNSET)
//                        .or(unaryTypePolicyStateMap == null
//                                ? PolicyState.UNSET
//                                : unaryTypePolicyStateMap
//                                .getOrDefault(unaryPolicy, new HashMap<>())
//                                .getOrDefault(graphElement.getClass(), PolicyState.UNSET));
            }
        };
    }

    // Helper method to find the most specific matching type policy
    private PolicyState findMostSpecificTypePolicy(Map<Class<? extends GraphElement>, PolicyState> typePolicies, Class<?> targetClass) {
        return typePolicies.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(targetClass))
                .min((e1, e2) -> compareClassSpecificity(e1.getKey(), e2.getKey(), targetClass))
                .map(Map.Entry::getValue)
                .orElse(PolicyState.UNSET);
    }

    // Helper method to compare class specificity
    private int compareClassSpecificity(Class<?> class1, Class<?> class2, Class<?> targetClass) {
        if (class1 == class2) return 0;
        if (class1.isAssignableFrom(class2)) return 1;  // class2 is more specific
        if (class2.isAssignableFrom(class1)) return -1; // class1 is more specific

        // If neither is assignable from the other, compare their distance to the target class
        return Integer.compare(
                getInheritanceDistance(class1, targetClass),
                getInheritanceDistance(class2, targetClass)
        );
    }

    // Helper method to calculate inheritance distance
    private int getInheritanceDistance(Class<?> superClass, Class<?> targetClass) {
        int distance = 0;
        Class<?> currentClass = targetClass;

        while (currentClass != null && !currentClass.equals(superClass)) {
            distance++;
            if (superClass.isInterface()) {
                // Check interfaces
                if (Arrays.asList(currentClass.getInterfaces()).contains(superClass)) {
                    return distance;
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return currentClass == null ? Integer.MAX_VALUE : distance;
    }

    /**
     * Returns the state of the given policy for the given graph element using the default algorithm.
     * @param unaryPolicy the policy to check
     * @param graphElement the graph element to check
     * @return the state of the policy
     * @param <P> the type of the policy
     * @param <E> the type of the graph element
     */
    public <P extends GraphPolicy & Policy.UnaryPolicy<E>, E extends GraphElement> PolicyState stateOf(P unaryPolicy, E graphElement) {
        Objects.requireNonNull(unaryPolicy);
        Objects.requireNonNull(graphElement);

        return stateOf(unaryPolicy, graphElement, defaultPolicyCheckAlgorithm);
    }

    /**
     * Returns the state of the given policy for the given graph element type using the default algorithm.
     * @param unaryPolicy the policy to check
     * @param graphElementType the graph element type to check
     * @return the state of the policy
     * @param <P> the type of the policy
     * @param <E> the type of the graph element
     */
    public <P extends GraphPolicy & Policy.UnaryPolicy<E>, E extends GraphElement> PolicyState stateOf(P unaryPolicy, Class<E> graphElementType) {
        Objects.requireNonNull(unaryPolicy);
        Objects.requireNonNull(graphElementType);

        if (unaryTypePolicyStateMap != null)
            return unaryTypePolicyStateMap
                    .getOrDefault(unaryPolicy, new HashMap<>())
                    .getOrDefault(graphElementType, PolicyState.UNSET);

        return PolicyState.UNSET;
    }

    /**
     * Returns the state of the given policy for the given graph element type using the given algorithm.
     * @param binaryPolicy the policy to check
     * @param firstElement the first graph element to check
     * @param secondElement the second graph element to check
     * @param algorithm the algorithm to use
     * @return the state of the policy
     * @param <P> the type of the policy
     * @param <E1> the type of the first graph element
     * @param <E2> the type of the second graph element
     */
    public <P extends GraphPolicy & Policy.BinaryPolicy<E1, E2>, E1 extends GraphElement, E2 extends GraphElement>
    PolicyState stateOf(P binaryPolicy, E1 firstElement, E2 secondElement, PolicyCheckAlgorithm algorithm) {
        Objects.requireNonNull(binaryPolicy);
        Objects.requireNonNull(firstElement);
        Objects.requireNonNull(secondElement);

        return switch (defaultPolicyCheckAlgorithm) {
            case OBJECT_OVER_TYPE -> binaryObjectPolicyStateMap == null
                    ? PolicyState.UNSET
                    : binaryObjectPolicyStateMap
                        .getOrDefault(binaryPolicy, new HashMap<>())
                        .getOrDefault(new Pair<>(firstElement, secondElement), PolicyState.UNSET);

            case TYPE_OVER_OBJECT -> binaryTypePolicyStateMap == null
                    ? PolicyState.UNSET
                    : binaryTypePolicyStateMap
                        .getOrDefault(binaryPolicy, new HashMap<>())
                        .entrySet().stream()
                        .filter(entry -> entry.getKey().first().isAssignableFrom(firstElement.getClass())
                                && entry.getKey().second().isAssignableFrom(secondElement.getClass()))
                        .map(Map.Entry::getValue)
                        .findAny().orElse(PolicyState.UNSET);

            case OBJECT_AND_TYPE -> binaryObjectPolicyStateMap == null
                    ? PolicyState.UNSET
                    : binaryObjectPolicyStateMap
                        .getOrDefault(binaryPolicy, new HashMap<>())
                        .getOrDefault(new Pair<>(firstElement, secondElement), PolicyState.UNSET)
                        .and(binaryTypePolicyStateMap == null
                                ? PolicyState.UNSET
                                : binaryTypePolicyStateMap // FIXME: Type checks
                                    .getOrDefault(binaryPolicy, new HashMap<>())
                                    .entrySet().stream()
                                    .filter(entry -> entry.getKey().first().isAssignableFrom(firstElement.getClass())
                                            && entry.getKey().second().isAssignableFrom(secondElement.getClass()))
                                    .map(Map.Entry::getValue)
                                    .findAny().orElse(PolicyState.UNSET));

            case OBJECT_OR_TYPE -> binaryObjectPolicyStateMap == null
                    ? PolicyState.UNSET
                    : binaryObjectPolicyStateMap
                        .getOrDefault(binaryPolicy, new HashMap<>())
                        .getOrDefault(new Pair<>(firstElement, secondElement), PolicyState.UNSET)
                        .or(binaryTypePolicyStateMap == null
                                ? PolicyState.UNSET
                                : binaryTypePolicyStateMap
                                    .getOrDefault(binaryPolicy, new HashMap<>())
                                    .entrySet().stream()
                                    .filter(entry -> entry.getKey().first().isAssignableFrom(firstElement.getClass())
                                            && entry.getKey().second().isAssignableFrom(secondElement.getClass()))
                                    .map(Map.Entry::getValue)
                                    .findAny().orElse(PolicyState.UNSET));
        };
    }

    /**
     * Returns the state of the given policy for the given graph element type using the default algorithm.
     * @param binaryPolicy the policy to check
     * @param firstElement the first graph element to check
     * @param secondElement the second graph element to check
     * @return the state of the policy
     * @param <P> the type of the policy
     * @param <E1> the type of the first graph element
     * @param <E2> the type of the second graph element
     */
    public <P extends GraphPolicy & Policy.BinaryPolicy<E1, E2>, E1 extends GraphElement, E2 extends GraphElement> PolicyState stateOf(P binaryPolicy, E1 firstElement, E2 secondElement) {
        Objects.requireNonNull(binaryPolicy);
        Objects.requireNonNull(firstElement);
        Objects.requireNonNull(secondElement);

        return stateOf(binaryPolicy, firstElement, secondElement, defaultPolicyCheckAlgorithm);
    }

    /**
     * Returns the state of the given policy for the given graph element type pair using the default algorithm.
     * @param binaryPolicy the policy to check
     * @param graphElementTypePair the graph element type pair to check
     * @return the state of the policy
     * @param <P> the type of the policy
     * @param <E1> the type of the first graph element
     * @param <E2> the type of the second graph element
     */
    public <P extends GraphPolicy & Policy.BinaryPolicy<E1, E2>, E1 extends GraphElement, E2 extends GraphElement> PolicyState stateOf(P binaryPolicy, Pair<Class<? extends E1>, Class<? extends E2>> graphElementTypePair) {
        Objects.requireNonNull(binaryPolicy);
        Objects.requireNonNull(graphElementTypePair);

        if (binaryTypePolicyStateMap != null)
            return binaryTypePolicyStateMap
                    .getOrDefault(binaryPolicy, new HashMap<>())
                    .getOrDefault(graphElementTypePair, PolicyState.UNSET);

        return PolicyState.UNSET;
    }

    /**
     * Accepts the given policy.
     * @param unaryPolicy the policy to accept
     * @param <P> the type of the policy
     */
    public <P extends GraphPolicy & Policy.NullaryPolicy> void accept(P unaryPolicy) {
        Objects.requireNonNull(unaryPolicy);

        if (nullaryPolicyStateMap == null)
            nullaryPolicyStateMap = new HashMap<>();

        nullaryPolicyStateMap.put(unaryPolicy, PolicyState.ACCEPT);
    }

    /**
     * Rejects the given policy.
     * @param unaryPolicy the policy to reject
     * @param <P> the type of the policy
     */
    public <P extends GraphPolicy & Policy.NullaryPolicy> void reject(P unaryPolicy) {
        Objects.requireNonNull(unaryPolicy);

        if (nullaryPolicyStateMap == null)
            nullaryPolicyStateMap = new HashMap<>();

        nullaryPolicyStateMap.put(unaryPolicy, PolicyState.REJECT);
    }

    /**
     * Unsets the given policy.
     * @param unaryPolicy the policy to unset
     * @param <P> the type of the policy
     */
    public <P extends GraphPolicy & Policy.NullaryPolicy> void unset(P unaryPolicy) {
        Objects.requireNonNull(unaryPolicy);

        if (nullaryPolicyStateMap == null)
            nullaryPolicyStateMap = new HashMap<>();

        nullaryPolicyStateMap.put(unaryPolicy, PolicyState.UNSET);
    }

    /**
     * Accepts the given policy for the given graph element.
     * @param binaryPolicy the policy to accept
     * @param graphElement the graph element to accept the policy for
     * @param <P> the type of the policy
     * @param <G> the type of the graph element
     */
    public <P extends GraphPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void accept(P binaryPolicy, G graphElement) {
        Objects.requireNonNull(binaryPolicy);
        Objects.requireNonNull(graphElement);

        if (unaryObjectPolicyStateMap == null)
            unaryObjectPolicyStateMap = new HashMap<>();

        unaryObjectPolicyStateMap.computeIfAbsent(binaryPolicy, _ -> new WeakHashMap<>()).put(graphElement, PolicyState.ACCEPT);
    }

    /**
     * Rejects the given policy for the given graph element.
     * @param binaryPolicy the policy to reject
     * @param graphElement the graph element to reject the policy for
     * @param <P> the type of the policy
     * @param <G> the type of the graph element
     */
    public <P extends GraphPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void reject(P binaryPolicy, G graphElement) {
        Objects.requireNonNull(binaryPolicy);
        Objects.requireNonNull(graphElement);

        if (unaryObjectPolicyStateMap == null)
            unaryObjectPolicyStateMap = new HashMap<>();

        unaryObjectPolicyStateMap.computeIfAbsent(binaryPolicy, _ -> new WeakHashMap<>()).put(graphElement, PolicyState.REJECT);
    }

    /**
     * Unsets the given policy for the given graph element.
     * @param binaryPolicy the policy to unset
     * @param graphElement the graph element to unset the policy for
     * @param <P> the type of the policy
     * @param <G> the type of the graph element
     */
    public <P extends GraphPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void unset(P binaryPolicy, G graphElement) {
        Objects.requireNonNull(binaryPolicy);
        Objects.requireNonNull(graphElement);

        if (unaryObjectPolicyStateMap == null)
            unaryObjectPolicyStateMap = new HashMap<>();

        unaryObjectPolicyStateMap.computeIfAbsent(binaryPolicy, _ -> new WeakHashMap<>()).put(graphElement, PolicyState.UNSET);
    }

    /**
     * Accepts the given policy for the given graph element type.
     * @param binaryPolicy the policy to accept
     * @param graphElementType the graph element type to accept the policy for
     * @param <P> the type of the policy
     * @param <G> the type of the graph element
     */
    public <P extends GraphPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void accept(P binaryPolicy, Class<? extends G> graphElementType) {
        Objects.requireNonNull(binaryPolicy);
        Objects.requireNonNull(graphElementType);

        if (unaryTypePolicyStateMap == null)
            unaryTypePolicyStateMap = new HashMap<>();

        unaryTypePolicyStateMap.computeIfAbsent(binaryPolicy, _ -> new HashMap<>()).put(graphElementType, PolicyState.ACCEPT);
    }

    /**
     * Rejects the given policy for the given graph element type.
     * @param binaryPolicy the policy to reject
     * @param graphElementType the graph element type to reject the policy for
     * @param <P> the type of the policy
     * @param <G> the type of the graph element
     */
    public <P extends GraphPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void reject(P binaryPolicy, Class<? extends G> graphElementType) {
        Objects.requireNonNull(binaryPolicy);
        Objects.requireNonNull(graphElementType);

        if (unaryTypePolicyStateMap == null)
            unaryTypePolicyStateMap = new HashMap<>();

        unaryTypePolicyStateMap.computeIfAbsent(binaryPolicy, _ -> new HashMap<>()).put(graphElementType, PolicyState.REJECT);
    }

    /**
     * Unsets the given policy for the given graph element type.
     * @param binaryPolicy the policy to unset
     * @param graphElementType the graph element type to unset the policy for
     * @param <P> the type of the policy
     * @param <G> the type of the graph element
     */
    public <P extends GraphPolicy & Policy.UnaryPolicy<G>, G extends GraphElement> void unset(P binaryPolicy, Class<? extends G> graphElementType) {
        Objects.requireNonNull(binaryPolicy);
        Objects.requireNonNull(graphElementType);

        if (unaryTypePolicyStateMap == null)
            unaryTypePolicyStateMap = new HashMap<>();

        unaryTypePolicyStateMap.computeIfAbsent(binaryPolicy, _ -> new HashMap<>()).put(graphElementType, PolicyState.UNSET);
    }

    /**
     * Accepts the given policy for the given graph elements.
     * @param ternaryPolicy the policy to accept
     * @param firstElement the first graph element to accept the policy for
     * @param secondElement the second graph element to accept the policy for
     * @param <P> the type of the policy
     * @param <G1> the type of the first graph element
     * @param <G2> the type of the second graph element
     */
    public <P extends GraphPolicy & Policy.BinaryPolicy<G1, G2>, G1 extends GraphElement, G2 extends GraphElement> void accept(P ternaryPolicy, G1 firstElement, G2 secondElement) {
        Objects.requireNonNull(ternaryPolicy);
        Objects.requireNonNull(firstElement);
        Objects.requireNonNull(secondElement);

        if (binaryObjectPolicyStateMap == null)
            binaryObjectPolicyStateMap = new HashMap<>();

        binaryObjectPolicyStateMap
                .computeIfAbsent(ternaryPolicy, _ -> new HashMap<>())
                .put(new Pair<>(firstElement, secondElement), PolicyState.ACCEPT);
//                .put(new Pair<>(new WeakReference<>(firstElement), new WeakReference<>(secondElement)), PolicyState.ACCEPT);
    }

    /**
     * Rejects the given policy for the given graph elements.
     * @param ternaryPolicy the policy to reject
     * @param firstElement the first graph element to reject the policy for
     * @param secondElement the second graph element to reject the policy for
     * @param <P> the type of the policy
     * @param <G1> the type of the first graph element
     * @param <G2> the type of the second graph element
     */
    public <P extends GraphPolicy & Policy.BinaryPolicy<G1, G2>, G1 extends GraphElement, G2 extends GraphElement> void reject(P ternaryPolicy, G1 firstElement, G2 secondElement) {
        Objects.requireNonNull(ternaryPolicy);
        Objects.requireNonNull(firstElement);
        Objects.requireNonNull(secondElement);

        if (binaryObjectPolicyStateMap == null)
            binaryObjectPolicyStateMap = new HashMap<>();

        binaryObjectPolicyStateMap
                .computeIfAbsent(ternaryPolicy, _ -> new HashMap<>())
                .put(new Pair<>(firstElement, secondElement), PolicyState.REJECT);
//                .put(new Pair<>(new WeakReference<>(firstElement), new WeakReference<>(secondElement)), PolicyState.REJECT);
    }

    /**
     * Unsets the given policy for the given graph elements.
     * @param ternaryPolicy the policy to unset
     * @param firstElement the first graph element to unset the policy for
     * @param secondElement the second graph element to unset the policy for
     * @param <P> the type of the policy
     * @param <G1> the type of the first graph element
     * @param <G2> the type of the second graph element
     */
    public <P extends GraphPolicy & Policy.BinaryPolicy<G1, G2>, G1 extends GraphElement, G2 extends GraphElement> void unset(P ternaryPolicy, G1 firstElement, G2 secondElement) {
        Objects.requireNonNull(ternaryPolicy);
        Objects.requireNonNull(firstElement);
        Objects.requireNonNull(secondElement);

        if (binaryObjectPolicyStateMap == null)
            binaryObjectPolicyStateMap = new HashMap<>();

        binaryObjectPolicyStateMap
                .computeIfAbsent(ternaryPolicy, _ -> new HashMap<>())
                .put(new Pair<>(firstElement, secondElement), PolicyState.UNSET);
//                .put(new Pair<>(new WeakReference<>(firstElement), new WeakReference<>(secondElement)), PolicyState.UNSET);
    }

    /**
     * Accepts the given policy for the given graph element types.
     * @param ternaryPolicy the policy to accept
     * @param firstElementType the first graph element type to accept the policy for
     * @param secondElementType the second graph element type to accept the policy for
     * @param <P> the type of the policy
     * @param <G1> the type of the first graph element
     * @param <G2> the type of the second graph element
     */
    public <P extends GraphPolicy & Policy.BinaryPolicy<G1, G2>, G1 extends GraphElement, G2 extends GraphElement> void accept(P ternaryPolicy, Class<? extends G1> firstElementType, Class<? extends G2> secondElementType) {
        Objects.requireNonNull(ternaryPolicy);
        Objects.requireNonNull(firstElementType);
        Objects.requireNonNull(secondElementType);

        if (binaryTypePolicyStateMap == null)
            binaryTypePolicyStateMap = new HashMap<>();

        binaryTypePolicyStateMap
                .computeIfAbsent(ternaryPolicy, _ -> new HashMap<>())
                .put(new Pair<>(firstElementType, secondElementType), PolicyState.ACCEPT);
    }


}
