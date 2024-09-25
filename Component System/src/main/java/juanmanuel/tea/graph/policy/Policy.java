package juanmanuel.tea.graph.policy;

import juanmanuel.tea.graph.GraphElement;

public sealed interface Policy permits GraphPolicy, Policy.UnaryPolicy, Policy.BinaryPolicy, Policy.NullaryPolicy, VertexPolicy {

    /**
     * Policy that takes no arguments, the host object is the only implicit argument and therefore no arguments are needed.
     */
    sealed interface NullaryPolicy extends Policy permits GraphPolicy.QueryGraphPolicy, VertexPolicy.QueryVertexPolicy {}

    /**
     * Policy that takes one argument, the host object is the first implicit argument and the second argument is needed.
     */
    sealed interface UnaryPolicy<G extends GraphElement> extends Policy permits EdgePolicy, GraphPolicy.VertexEffectGraphPolicy, GraphPolicy.VertexModificationGraphPolicy, VertexPolicy.EdgeModificationVertexPolicy, VertexPolicy.EffectVertexPolicy, VertexPolicy.GraphModificationVertexPolicy {}

    /**
     * Policy that takes two arguments, the host object is the first implicit argument and the second and third arguments are needed.
     */
    sealed interface BinaryPolicy<G1 extends GraphElement, G2 extends GraphElement> extends Policy permits GraphPolicy.EdgeEffectGraphPolicy, GraphPolicy.EdgeModificationGraphPolicy {}
}
