package juanmanuel.tea.graph.policy;

import juanmanuel.tea.graph.ApplicationEdge;

public sealed interface EdgePolicy extends Policy.UnaryPolicy<ApplicationEdge> {
    enum QueryEdgePolicy implements EdgePolicy {
        QUERY_SOURCE_POLICY,
        QUERY_TARGET_POLICY,
        QUERY_WEIGHT_POLICY
    }

    enum ModificationEdgePolicy implements EdgePolicy {
        CREATE_EDGE_POLICY,
        REMOVE_EDGE_POLICY
    }
}
