package juanmanuel.tea.graph.policy;

import juanmanuel.tea.graph.Vertex;

/**
 * A policy that can be applied on a graph.
 */
public sealed interface GraphPolicy extends Policy {
    enum QueryGraphPolicy implements GraphPolicy, NullaryPolicy {
        /**
         * Whether vertices can be queried.
         */
        QUERY_VERTICES_POLICY,

        /**
         * Whether edges can be queried.
         */
        QUERY_EDGES_POLICY
    }

    enum VertexModificationGraphPolicy implements GraphPolicy, UnaryPolicy<Vertex<?>> {
        /**
         * Whether the graph could be added.
         */
        ADD_VERTEX_POLICY,

        /**
         * Whether the vertex could be removed from the graph
         */
        REMOVE_VERTEX_POLICY
    }

    enum EdgeModificationGraphPolicy implements GraphPolicy, BinaryPolicy<Vertex<?>, Vertex<?>> {
        CREATE_EDGE_POLICY,
        REMOVE_EDGE_POLICY
    }

    enum VertexEffectGraphPolicy implements GraphPolicy, UnaryPolicy<Vertex<?>> {
        /**
         * Whether onEnterGraph should be executed when the vertex enters a graph.
         */
        ON_ENTER_GRAPH_POLICY,

        /**
         * Whether onLeaveGraph should be executed when the vertex leaves a graph.
         */
        ON_LEAVE_GRAPH_POLICY,
    }

    enum EdgeEffectGraphPolicy implements GraphPolicy, BinaryPolicy<Vertex<?>, Vertex<?>> {
        /**
         * Whether onConnect should be executed when an edge is connected.
         */
        ON_ADD_EDGE_POLICY,

        /**
         * Whether onDisconnect should be executed when an edge is disconnected.
         */
        ON_REMOVE_EDGE_POLICY
    }
}
