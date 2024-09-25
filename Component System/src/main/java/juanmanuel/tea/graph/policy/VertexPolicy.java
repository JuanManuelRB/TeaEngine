package juanmanuel.tea.graph.policy;

import juanmanuel.tea.graph.ApplicationGraph;
import juanmanuel.tea.graph.ApplicationVertex;

/**
 * Policies of a vertex.
 */
public sealed interface VertexPolicy extends Policy {

    /**
     * Query policies control if some queries can be executed.
     */
    enum QueryVertexPolicy implements VertexPolicy, NullaryPolicy {

        /**
         * Whether children can be queried.
         */
        QUERY_CHILDREN_POLICY,

        /**
         * Whether parents can be queried.
         */
        QUERY_PARENTS_POLICY,

        /**
         * Whether the graph can be queried.
         */
        QUERY_GRAPH_POLICY
    }

    /**
     * Effect policies control the behavior of some side effects produced by an operation.
     */
    enum EffectVertexPolicy implements VertexPolicy, UnaryPolicy<ApplicationVertex<?>> {
        /**
         * Whether onConnectChild should be executed when a child is connected.
         */
        ON_CONNECT_CHILD_POLICY,

        /**
         * Whether onConnectParent should be executed when a parent is connected.
         */
        ON_CONNECT_PARENT_POLICY,

        /**
         * Whether onDisconnectChild should be executed when a child is disconnected.
         */
        ON_DISCONNECT_CHILD_POLICY,

        /**
         * Whether onDisconnectParent should be executed when a parent is disconnected.
         */
        ON_DISCONNECT_PARENT_POLICY
    }

    /**
     * Modification policies control the behavior of some operations.
     */
    enum EdgeModificationVertexPolicy implements VertexPolicy, UnaryPolicy<ApplicationVertex<?>> {
        /**
         * Whether a child could be connected.
         */
        CONNECT_CHILD_POLICY,

        /**
         * Whether a parent could be connected.
         */
        CONNECT_PARENT_POLICY,

        /**
         * Whether a child could be disconnected.
         */
        DISCONNECT_CHILD_POLICY,

        /**
         * Whether a parent could be disconnected.
         */
        DISCONNECT_PARENT_POLICY
    }

    enum GraphModificationVertexPolicy implements VertexPolicy, UnaryPolicy<ApplicationGraph<?>> {
        /**
         * Whether the vertex could be added to a graph.
         */
        ADD_TO_GRAPH_POLICY,

        /**
         * Whether the vertex could be removed from a graph.
         */
        REMOVE_FROM_GRAPH_POLICY,

        /**
         * Whether the call to onEnterGraph should be executed when the vertex enters a graph.
         */
        ON_ENTER_GRAPH_POLICY,

        /**
         * Whether the call to onLeaveGraph should be executed when the vertex leaves a graph.
         */
        ON_LEAVE_GRAPH_POLICY,
    }
}
