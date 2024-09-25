package juanmanuel.tea.graph;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.util.SupplierUtil;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A Directed Acyclic Graph.
 * Does not allow duplicate vertices or edges.
 * Does not allow null vertices or edges.
 */
public class Graph<V extends Vertex<V>, E extends DefaultWeightedEdge> extends DirectedAcyclicGraph<V, E> {

    public Graph(Class<? extends E> edgeClass) {
        super(null, SupplierUtil.createSupplier(edgeClass), true);
    }

    public Graph(Class<? extends E> edgeClass, Supplier<V> vertexSupplier) {
        super(null, SupplierUtil.createSupplier(edgeClass), true);
        setVertexSupplier(vertexSupplier);
    }

    @Override
    public Set<V> vertexSet() {
        return super.vertexSet();
    }

    @Override
    public Set<E> edgeSet() {
        return super.edgeSet();
    }

    public Set<V> roots() {
        return super.vertexSet().stream().filter(v -> super.inDegreeOf(v) == 0).collect(Collectors.toSet());
    }

    public Set<V> sinks() {
        return super.vertexSet().stream().filter(v -> super.outDegreeOf(v) == 0).collect(Collectors.toSet());
    }

    @Override
    public Set<E> edgesOf(V v) {
        return super.edgesOf(v);
    }

    /**
     * Gets the edge that connects the given vertices if it exists. If it does not exist, an empty optional is returned.
     * @param sourceVertex the source vertex of the edge.
     * @param targetVertex the target vertex of the edge.
     * @return the edge that connects the given vertices if it exists. If it does not exist, an empty optional is returned.
     */
    public Optional<E> edgeOf(V sourceVertex, V targetVertex) {
        return Optional.ofNullable(super.getEdge(sourceVertex, targetVertex));
    }

    public Set<E> egressEdgesOf(V v) {
        return super.edgesOf(v).stream().filter(e -> super.getEdgeSource(e).equals(v)).collect(Collectors.toSet());
    }

    public Set<E> ingressEdgesOf(V v) {
        return super.edgesOf(v).stream().filter(e -> super.getEdgeTarget(e).equals(v)).collect(Collectors.toSet());
    }

    public int size() {
        return super.vertexSet().size();
    }

    /**
     * Gets the parent vertices of the given vertex.
     * @param v the vertex to search for its children.
     * @return the parent vertices of the given vertex.
     */
    public Set<V> getParents(V v) {
        return super.incomingEdgesOf(v).stream().map(super::getEdgeSource).collect(Collectors.toSet());
    }

    /**
     * Gets the child vertices of the given vertex.
     * @param v the vertex to search for its children.
     * @return the child vertices of the given vertex.
     */
    public Set<V> getChildren(V v) {
        return super.outgoingEdgesOf(v).stream().map(super::getEdgeTarget).collect(Collectors.toSet());
    }

    /**
     * Gets the neighbors of the given vertex.
     * The neighbors of a vertex are the vertices that are connected to it.
     * @param v the vertex to search for its neighbors.
     * @return the neighbors of the given vertex.
     */
    public Set<V> getNeighbors(V v) {
        Set<V> neighbors = new HashSet<>();
        neighbors.addAll(getParents(v));
        neighbors.addAll(getChildren(v));
        return neighbors;
    }

    /**
     * Collects the roots of the graph that connect to the given vertex.
     * @param v the vertex to search for its sources.
     * @return the sources of the given vertex.
     */
    public Set<V> sourcesOf(V v) {
        Set<V> roots = new HashSet<>();
        for (V vertex : roots()) {
            var dijkstra = new DijkstraShortestPath<>(this);
            var path = dijkstra.getPath(vertex, v);
            if (path != null)
                roots.add(vertex);
        }
        return roots;
    }

    /**
     * Collects the vertices that have no outgoing edges and have a path from the given vertex.
     * @param v the vertex to search for its sinks.
     * @return the sinks of the given vertex.
     */
    public Set<V> sinksOf(V v) {
        Set<V> sinks = new HashSet<>();
        for (V vertex : sinks()) {
            var dijkstra = new DijkstraShortestPath<>(this);
            var path = dijkstra.getPath(v, vertex);
            if (path != null)
                sinks.add(vertex);
        }
        return sinks;
    }

    /**
     * Gets the shortest path between the source and the target vertices.
     * @param source the source vertex.
     * @param target the target vertex.
     * @return the shortest path between the source and the target vertices.
     */
    public SequencedCollection<E> shortestPathBetween(V source, V target) {
        var dijkstra = new DijkstraShortestPath<>(this);
        var path = dijkstra.getPath(source, target);
        if (path == null)
            return List.of();

        return path.getEdgeList();
    }

    /**
     * Gets the paths between the source vertex and the target vertex.
     * @param source the source vertex.
     * @param target the target vertex.
     * @return a set of paths between the source vertex and the target vertex.
     */
    public Set<SequencedCollection<E>> pathsBetween(V source, V target) {
        AllDirectedPaths<V, E> allDirectedPaths = new AllDirectedPaths<>(this);
        List<GraphPath<V, E>> paths = allDirectedPaths.getAllPaths(source, target, true, null);
        return paths.stream()
                .map(GraphPath::getEdgeList)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the vertices of the shortest path between the source and the target vertices including the source and the
     * target vertices.
     * @param source the source vertex.
     * @param target the target vertex.
     * @return the vertices of the shortest path between the source and the target vertices including the source and the
     * target vertices.
     */
    public SequencedCollection<V> shortestVertexPathBetween(V source, V target) {
        var dijkstra = new DijkstraShortestPath<>(this);
        var path = dijkstra.getPath(source, target);
        if (path == null)
            return List.of();

        return path.getVertexList();
    }
}
