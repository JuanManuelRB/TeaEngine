package juanmanuel.tea.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

/**
 * Represents a vertex in a graph.
 * Provides methods to interact with the graphs that contain the vertex.
 *
 * @param <V> the type of the vertex.
 */
public abstract class Vertex<V extends Vertex<V>> {
    public static <V extends Vertex<V>, E extends DefaultWeightedEdge> Set<V> verticesIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        return graph.vertexSet();
    }

    public static <V extends Vertex<V>, E extends DefaultWeightedEdge> Set<E> edgesIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        return graph.edgeSet();
    }

    /**
     * Returns the egress edges of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the egress edges of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> Set<E> egressEdgesIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this))
            return Set.of();

        return graph.egressEdgesOf((V) this);
    }

    /**
     * Returns the ingress edges of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the ingress edges of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> Set<E> ingressEdgesIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this))
            return Set.of();

        return graph.ingressEdgesOf((V) this);
    }

    /**
     * Returns the children of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the children of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> Set<V> childrenIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this))
            return Set.of();

        return graph.getChildren((V) this);
    }

    /**
     * Returns the parents of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the parents of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> Set<V> parentsIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this))
            return Set.of();

        return graph.getParents((V) this);
    }

    /**
     * Returns the neighbors of the vertex on the given graph.
     * The neighbors of a vertex are the vertices that are connected to it.
     * @param graph the graph that contains the vertex.
     * @return the neighbors of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> Set<V> neighborsIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this))
            return Set.of();

        return graph.getNeighbors((V) this);
    }

    /**
     * Collects the children, the children of the children, and so on, of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the descendants of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> Set<V> descendantsIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this))
            return Set.of();

        return graph.getDescendants((V) this);
    }

    /**
     * Collects the parents, the parents of the parents, and so on, of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the ancestors of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> Set<V> ancestorsIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this))
            return Set.of();

        return graph.getAncestors((V) this);
    }


    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> Set<E> edgePathTo(V target, Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this) || !graph.containsVertex(target))
            return Set.of();

        return new HashSet<>(graph.shortestPathBetween((V) this, target));
    }

    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> Set<E> edgePathFrom(V source, Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this) || !graph.containsVertex(source))
            return Set.of();

        return new HashSet<>(graph.shortestPathBetween(source, (V) this));
    }

    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> SequencedCollection<V> shortestPathTo(V target, Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this) || !graph.containsVertex(target))
            return List.of();

        return graph.shortestVertexPathBetween((V) this, target);
    }

    @SuppressWarnings("unchecked")
    public final <E extends DefaultWeightedEdge> SequencedCollection<V> shortestPathFrom(V source, Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((V) this) || !graph.containsVertex(source))
            return List.of();

        return graph.shortestVertexPathBetween(source, (V) this);
    }
}
