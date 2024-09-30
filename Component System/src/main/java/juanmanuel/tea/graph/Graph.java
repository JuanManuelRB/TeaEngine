package juanmanuel.tea.graph;

import juanmanuel.tea.graph.operation_failures.FailureResults;
import juanmanuel.tea.graph.policy.GraphOperationsPolicies;
import juanmanuel.tea.graph.policy.PolicyState;
import juanmanuel.tea.graph.validation.GraphOperationValidator;
import juanmanuel.tea.utils.Result;
import org.jgrapht.GraphIterables;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.GraphCycleProhibitedException;
import org.jgrapht.graph.GraphSpecificsStrategy;
import org.jgrapht.util.SupplierUtil;

import java.util.*;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static juanmanuel.tea.graph.policy.GraphPolicy.EdgeEffectGraphPolicy.ON_ADD_EDGE_POLICY;
import static juanmanuel.tea.graph.policy.GraphPolicy.EdgeModificationGraphPolicy.CREATE_EDGE_POLICY;
import static juanmanuel.tea.graph.policy.GraphPolicy.EdgeModificationGraphPolicy.REMOVE_EDGE_POLICY;
import static juanmanuel.tea.graph.policy.GraphPolicy.VertexEffectGraphPolicy.ON_ENTER_GRAPH_POLICY;
import static juanmanuel.tea.graph.policy.GraphPolicy.VertexEffectGraphPolicy.ON_LEAVE_GRAPH_POLICY;
import static juanmanuel.tea.graph.policy.GraphPolicy.VertexModificationGraphPolicy.ADD_VERTEX_POLICY;
import static juanmanuel.tea.graph.policy.GraphPolicy.VertexModificationGraphPolicy.REMOVE_VERTEX_POLICY;
import static juanmanuel.tea.graph.policy.PolicyState.ACCEPT;
import static juanmanuel.tea.graph.policy.VertexPolicy.EdgeModificationVertexPolicy.*;
import static juanmanuel.tea.graph.policy.VertexPolicy.GraphModificationVertexPolicy.ADD_TO_GRAPH_POLICY;
import static juanmanuel.tea.graph.validation.GraphOperationValidator.EdgeValidation.CREATE_EDGE_VALIDATION;
import static juanmanuel.tea.graph.validation.GraphOperationValidator.EdgeValidation.REMOVE_EDGE_VALIDATION;
import static juanmanuel.tea.graph.validation.GraphOperationValidator.VertexValidation.ADD_VERTEX_VALIDATION;
import static juanmanuel.tea.graph.validation.GraphOperationValidator.VertexValidation.REMOVE_VERTEX_VALIDATION;
import static juanmanuel.tea.graph.validation.VertexOperationValidator.GraphsOperationValidation.ADD_TO_GRAPH_VALIDATION;
import static juanmanuel.tea.graph.validation.VertexOperationValidator.VerticesOperationValidation.CONNECT_CHILD_VALIDATION;
import static juanmanuel.tea.graph.validation.VertexOperationValidator.VerticesOperationValidation.DISCONNECT_CHILD_VALIDATION;
import static juanmanuel.tea.utils.Result.fail;
import static juanmanuel.tea.utils.Result.success;

/**
 * A Directed Acyclic Graph.
 * Does not allow duplicate vertices or edges.
 * Does not allow null vertices or edges.
 */
public non-sealed class Graph<V extends Vertex<V>, E extends ApplicationEdge> implements GraphElement {
    protected final DirectedAcyclicGraph<V, E> graph;
    final protected GraphOperationValidator<V> validationsManager = new GraphOperationValidator<>();
    final GraphOperationsPolicies policiesManager = new GraphOperationsPolicies();
    private final Map<?, Consumer<V>> addVertexCallbacks = new HashMap<>();
    private final Map<?, Consumer<V>> removeVertexCallbacks = new HashMap<>();
    private final Map<?, BiConsumer<V, V>> addEdgeCallbacks = new HashMap<>();
    private final Map<?, BiConsumer<V, V>> removeEdgeCallbacks = new HashMap<>();
    private boolean acceptUnsetPolicy = false;

    public Graph(Class<E> edgeClass) {
        graph = new DirectedAcyclicGraph<>(null, SupplierUtil.createSupplier(edgeClass), true);
    }

    public Graph(Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, boolean weighted) {
        graph = new DirectedAcyclicGraph<>(vertexSupplier, edgeSupplier, weighted);
    }

    public Graph(Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, boolean weighted, boolean allowMultipleEdges) {
        graph = new DirectedAcyclicGraph<>(vertexSupplier, edgeSupplier, weighted, allowMultipleEdges);
    }

    public Graph(Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, boolean weighted, boolean allowMultipleEdges, GraphSpecificsStrategy<V, E> graphSpecificsStrategy) {
        graph = new DirectedAcyclicGraph<>(vertexSupplier, edgeSupplier, weighted, allowMultipleEdges, graphSpecificsStrategy);
    }

    public Set<V> vertexSet() {
        return graph.vertexSet();
    }

    public Set<E> edgeSet() {
        return graph.edgeSet();
    }

    public Set<E> edgesOf(V v) {
        return graph.edgesOf(v);
    }

    public Set<E> egressEdgesOf(V v) {
        return graph.outgoingEdgesOf(v);
    }

    public Set<E> ingressEdgesOf(V v) {
        return graph.incomingEdgesOf(v);
    }

    public GraphOperationsPolicies policiesManager() {
        return policiesManager;
    }

    public GraphOperationValidator<V> validationsManager() {
        return validationsManager;
    }

    private boolean shouldCallOnEnterVertexFor(V v) {
        return switch (policiesManager().stateOf(ON_ENTER_GRAPH_POLICY, v)) {
            case ACCEPT -> true;
            case REJECT -> false;
            case UNSET -> acceptUnsetPolicy();
        };
    }

    protected boolean shouldCallOnLeaveVertexFor(V v) {
        return switch (policiesManager().stateOf(ON_LEAVE_GRAPH_POLICY, v)) {
            case ACCEPT -> true;
            case REJECT -> false;
            case UNSET -> acceptUnsetPolicy();
        };
    }

    /**
     * Checks if a vertex can be added to the graph without checking if the vertex is already present in the graph.
     * @param v The vertex to add.
     * @return A result indicating if the vertex can be added or not.
     */
    public Result<Void, ShouldAddVertexFailure> shouldAddVertexNoCheckIfPresent(V v) {
        switch (policiesManager().stateOf(ADD_VERTEX_POLICY, v)) {
            case REJECT -> {
                return fail(new FailureResults.RejectedByGraphPolicy("The graph policy rejected the vertex addition", this));
            }
            case UNSET -> {
                if (!acceptUnsetPolicy())
                    return fail(new FailureResults.RejectedByGraphPolicy("The graph policy rejected the vertex addition", this));
            }
        }

        if (!validationsManager.validateOperation(ADD_VERTEX_VALIDATION, v))
            return fail(new FailureResults.RejectedByGraphValidation("The graph validation rejected the vertex addition", this));

        switch (v.policiesManager().stateOf(ADD_TO_GRAPH_POLICY, this)) {
            case PolicyState.REJECT -> {
                return fail(new FailureResults.RejectedByVertexPolicy("The vertex policy rejected the operation", v));
            }

            case PolicyState.UNSET -> {
                if (!v.acceptOnUnsetPolicy())
                    return fail(new FailureResults.RejectedByVertexPolicy("The vertex policy rejected the operation", v));
            }
        }

        if (v.validationsManager().validateOperation(ADD_TO_GRAPH_VALIDATION, this) instanceof Result.Failure<?, Set<String>> f)
            return fail(new FailureResults.RejectedByVertexValidation(
                    "The operation is not valid:\n\t" + f.cause()
                            .stream()
                            .reduce("", (a, b) -> a + "\n\t" + b),
                    v));

        return success();
    }

    /**
     * Checks if a vertex can be added to the graph.
     * @param v The vertex to add.
     * @return A result indicating if the vertex can be added or not.
     */
    public Result<Void, ShouldAddVertexFailure> shouldAddVertex(V v) {
        Objects.requireNonNull(v);

        if (containsVertex(v))
            return fail(new FailureResults.VertexAlreadyPresent("The vertex is already in the graph"));

        return shouldAddVertexNoCheckIfPresent(v);
    }

    /**
     * @return True if the policy should accept the operation when it is not set, false otherwise.
     */
    public boolean acceptUnsetPolicy() {
        return acceptUnsetPolicy;
    }

    /**
     * Sets the behavior of the policy when it is not set.
     * @param behavior True if the policy should accept the operation when it is not set, false otherwise.
     */
    public void acceptUnsetPolicy(boolean behavior) {
        acceptUnsetPolicy = behavior;
    }

    /**
     * Checks if a vertex can be removed from the graph.
     * @param v The vertex to remove.
     * @return A result indicating if the vertex can be removed or not.
     */
    public Result<Void, ShouldRemoveVertexFailure> shouldRemoveVertex(V v) {
        Objects.requireNonNull(v);

        if (!containsVertex(v))
            return fail(new FailureResults.VertexNotPresent("The vertex is not present in the graph", v));

        switch (policiesManager.stateOf(REMOVE_VERTEX_POLICY, v)) {
            case ACCEPT -> {}
            case REJECT -> {
                return fail(new FailureResults.RejectedByGraphPolicy("The graph policy rejected the vertex removal", this));
            }
            case UNSET -> {
                if (!acceptUnsetPolicy())
                    return fail(new FailureResults.RejectedByGraphPolicy("The graph policy rejected the vertex removal", this));
            }
        }

        if (!validationsManager.validateOperation(REMOVE_VERTEX_VALIDATION, v))
            return fail(new FailureResults.RejectedByGraphValidation("The graph validation rejected the vertex removal", this));

        var paResStr = parentsOf(v)
                .parallelStream()
                .map(parent -> shouldRemoveEdge(parent, v)); // TODO Gather first only

        var chResStr = childrenOf(v)
                .parallelStream()
                .map(child -> shouldRemoveEdge(v, child));

        return Stream.concat(paResStr, chResStr)
                .parallel()
                .filter(Result::isFailure)
                .findFirst()
                .map(shReEdFa -> switch (((Result.Failure<Void, ShouldRemoveEdgeFailure>) shReEdFa).cause()) {
                    case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy ->
                            Result.<Void, ShouldRemoveVertexFailure>fail(rejectedByGraphPolicy);

                    case FailureResults.RejectedByGraphValidation rejectedByGraphValidation ->
                            Result.<Void, ShouldRemoveVertexFailure>fail(rejectedByGraphValidation);

                    case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy ->
                            Result.<Void, ShouldRemoveVertexFailure>fail(rejectedByVertexPolicy);

                    case FailureResults.RejectedByVertexValidation rejectedByVertexValidation ->
                            Result.<Void, ShouldRemoveVertexFailure>fail(rejectedByVertexValidation);

                    case FailureResults.VertexNotPresent vertexNotPresent ->
                            throw new IllegalStateException("The child or parent is not present in the graph. Graph is inconsistent.");
                    case FailureResults.EdgeNotPresent edgeNotPresent ->
                            throw new IllegalStateException("The edge is not present in the graph. Graph is inconsistent.");
                })
                .orElse(success());
    }

    /**
     * Adds a vertex to the graph. The method checks if the vertex can be added and then adds it.
     * @param v The vertex to add.
     * @return A result indicating if the vertex was added or not.
     * @throws NullPointerException If the vertex is null.
     * @throws GraphOperationException If the vertex could not be added to the graph.
     */
    public Result<V, VertexAdditionFailure> addVertex(V v)
            throws NullPointerException, GraphOperationException {
        Objects.requireNonNull(v);

        return switch (addVertexValidated(v)) {
            case Result.Failure<V, VertexAdditionFailure> f -> f;
            case Result.Success<V, VertexAdditionFailure> s -> {
                processVertexAdditionCallbacks(v);
                yield s;
            }
        };
    }

    final Result<V, VertexAdditionFailure> addVertexValidated(V v)
            throws NullPointerException, GraphOperationException {
        Objects.requireNonNull(v);

        return switch (shouldAddVertex(v)) {
            case Result.Failure<?, ShouldAddVertexFailure>(var failure) -> switch (failure) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexAlreadyPresent vertexAlreadyPresent -> fail(vertexAlreadyPresent);
            };

            case Result.Success<Void, ?> _ -> {
                if (!graph.addVertex(v))
                    throw new GraphOperationException("The vertex could not be added to the graph");

                yield success(v);
            }
        };
    }

    final void processVertexAdditionCallbacks(V v) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var addVertexCallback : addVertexCallbacks.values())
                scope.fork(() -> {
                    addVertexCallback.accept(v);
                    return null;
                });

            if (shouldCallOnEnterVertexFor(v))
                scope.fork(() -> {
                    this.onEnterVertex(v);
                    return null;
                });

            if (v.shouldCallOnEnterGraphFor(this))
                scope.fork(() -> {
                    v.onEnterGraph(this);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Result<E, Graph.EdgeAdditionFailure> addEdge(V sourceVertex, V targetVertex) {
        return addEdge(sourceVertex, targetVertex, 1.0); // TODO: default weight?
    }

    public Result<E, Graph.EdgeRemovalFailure> removeEdge(E e) {
        return removeEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
    }

//    public boolean removeAllVertices(Collection<? extends V> vertices) {
//        return graph.removeAllVertices(vertices);
//    } TODO

//    public boolean removeAllEdges(Collection<? extends E> edges) {
//        return super.removeAllEdges(edges);
//    } TODO

//    public Set<E> removeAllEdges(V sourceVertex, V targetVertex) {
//        return super.removeAllEdges(sourceVertex, targetVertex);
//    } TODO

    public Set<V> childrenOf(V vertex) {
        return egressEdgesOf(vertex)
                .stream()
                .map(graph::getEdgeTarget)
                .collect(Collectors.toSet());
    }

    public Set<V> parentsOf(V vertex) {
        return ingressEdgesOf(vertex)
                .stream()
                .map(graph::getEdgeSource)
                .collect(Collectors.toSet());
    }

    public Set<V> ancestorsOf(V vertex) {
        return graph.getAncestors(vertex);
    }

    public Set<V> descendantsOf(V vertex) {
        return graph.getDescendants(vertex);
    }

    public void forEach(Consumer<? super V> action) {
        graph.forEach(action);
    }

    public Spliterator<V> spliterator() {
        return graph.spliterator();
    }

    public Set<E> getAllEdges(V sourceVertex, V targetVertex) {
        return graph.getAllEdges(sourceVertex, targetVertex);
    }

    public void setEdgeSupplier(Supplier<E> edgeSupplier) {
        graph.setEdgeSupplier(edgeSupplier);
    }

    public Supplier<E> getEdgeSupplier() {
        return graph.getEdgeSupplier();
    }

    public Optional<Supplier<V>> getVertexSupplier() {
        return Optional.ofNullable(graph.getVertexSupplier());
    }

    public void setVertexSupplier(Supplier<V> vertexSupplier) {
        graph.setVertexSupplier(vertexSupplier);
    }

    public Optional<E> getEdge(V sourceVertex, V targetVertex) {
        return Optional.ofNullable(graph.getEdge(sourceVertex, targetVertex));
    }

    public Optional<V> getEdgeSource(E e) {
        return Optional.ofNullable(graph.getEdgeSource(e));
    }

    public Optional<V> getEdgeTarget(E e) {
        return Optional.ofNullable(graph.getEdgeTarget(e));
    }

    public boolean containsEdge(E e) {
        return graph.containsEdge(e);
    }


    public boolean containsVertex(V v) {
        return graph.containsVertex(v);
    }

    public int degreeOf(V vertex) {
        return graph.degreeOf(vertex);
    }

    public int inDegreeOf(V vertex) {
        return graph.inDegreeOf(vertex);
    }

    public int outDegreeOf(V vertex) {
        return graph.outDegreeOf(vertex);
    }

    public double getEdgeWeight(E e) {
        return graph.getEdgeWeight(e);
    }

    public void setEdgeWeight(E e, double weight) {
        graph.setEdgeWeight(e, weight);
    }

    public void setEdgeWeight(V sourceVertex, V targetVertex, double weight) {
        graph.setEdgeWeight(sourceVertex, targetVertex, weight);
    }

    public GraphIterables<V, E> iterables() {
        return graph.iterables();
    }

    public Set<V> roots() {
        return graph.vertexSet().stream().filter(v -> graph.inDegreeOf(v) == 0).collect(Collectors.toSet());
    }
    public Set<V> sinks() {
        return graph.vertexSet().stream().filter(v -> graph.outDegreeOf(v) == 0).collect(Collectors.toSet());
    }

    public boolean containsEdge(V sourceVertex, V targetVertex) {
        return graph.containsEdge(sourceVertex, targetVertex);
    }

    /**
     * Gets the edge that connects the given vertices if it exists. If it does not exist, an empty optional is returned.
     * @param sourceVertex the source vertex of the edge.
     * @param targetVertex the target vertex of the edge.
     * @return the edge that connects the given vertices if it exists. If it does not exist, an empty optional is returned.
     */
    public Optional<E> edgeOf(V sourceVertex, V targetVertex) {
        return Optional.ofNullable(graph.getEdge(sourceVertex, targetVertex));
    }

    public int size() {
        return graph.vertexSet().size();
    }

    public Set<V> siblingsOf(V v) {
        Set<V> siblings = new HashSet<>();
        for (V parent : parentsOf(v)) {
            siblings.addAll(childrenOf(parent));
        }
        siblings.remove(v);
        return siblings;
    }

    public Set<V> fullSiblingsOf(V v) {
        Set<V> siblings = siblingsOf(v);
        for (V parent : parentsOf(v))
            siblings.removeIf(s -> !parentsOf(s).contains(parent));
        return siblings;
    }

    public Set<V> halfSiblingsOf(V v) {
        Set<V> siblings = siblingsOf(v);
        siblings.removeAll(fullSiblingsOf(v));
        return siblings;
    }

    /**
     * Gets the neighbors of the given vertex.
     * The neighbors of a vertex are the vertices that are connected to it.
     * @param v the vertex to search for its neighbors.
     * @return the neighbors of the given vertex.
     */
    public Set<V> neighborsOf(V v) {
        Set<V> neighbors = new HashSet<>();
        neighbors.addAll(parentsOf(v));
        neighbors.addAll(childrenOf(v));
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
            var dijkstra = new DijkstraShortestPath<>(graph);
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
            var dijkstra = new DijkstraShortestPath<>(graph);
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
        var dijkstra = new DijkstraShortestPath<>(graph);
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
        AllDirectedPaths<V, E> allDirectedPaths = new AllDirectedPaths<>(graph);
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
        var dijkstra = new DijkstraShortestPath<>(graph);
        var path = dijkstra.getPath(source, target);
        if (path == null)
            return List.of();

        return path.getVertexList();
    }

    /**
     * Removes a vertex from the graph. The method checks if the vertex can be removed and then removes it.
     * @param v The vertex to remove.
     * @return A result indicating if the vertex was removed or not.
     * @throws NullPointerException If the vertex is null.
     * @throws GraphOperationException If the vertex could not be removed from the graph.
     */
    public Result<V, VertexRemovalFailure> removeVertex(V v) throws NullPointerException, GraphOperationException {
        Objects.requireNonNull(v);

        return switch (shouldRemoveVertex(v)) {
            case Result.Failure<?, ShouldRemoveVertexFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Result.Success<Void, ?> _ -> {
                var parents = parentsOf(v);
                var children = childrenOf(v);

                if (!graph.removeVertex(v))
                    throw new GraphOperationException("The vertex could not be removed from the graph");

                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    for (var removeVertexCallback : removeVertexCallbacks.values()) // TODO: Check policy
                        scope.fork(() -> {
                            removeVertexCallback.accept(v);
                            return null;
                        });

                    if (v.shouldCallOnLeaveGraphFor(this))
                        scope.fork(() -> {
                            v.onLeaveGraph(this);
                            return null;
                        });

                    if (shouldCallOnLeaveVertexFor(v))
                        scope.fork(() -> {
                            this.onLeaveVertex(v);
                            return null;
                        });

                    // Calls to onDisconnectChild and onDisconnectParent
                    parents.forEach(parent -> {
                        scope.fork(() -> {
                            if (v.shouldCallOnDisconnectParentFor(parent))
                                v.onDisconnectParent(parent, this);
                            return null;
                        });
                        scope.fork(() -> {
                            if (parent.shouldCallOnDisconnectChildFor(v))
                                parent.onDisconnectChild(v, this);
                            return null;
                        });
                    });

                    children.forEach(child -> {
                        scope.fork(() -> {
                            if (v.shouldCallOnDisconnectChildFor(child))
                                v.onDisconnectChild(child, this);
                            return null;
                        });
                        scope.fork(() -> {
                            if (child.shouldCallOnDisconnectParentFor(v))
                                child.onDisconnectParent(v, this);
                            return null;
                        });
                    });

                    scope.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                yield success(v);
            }
        };
    }

    void processVertexRemovalCallbacks(V v) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var removeVertexCallback : removeVertexCallbacks.values())
                scope.fork(() -> {
                    removeVertexCallback.accept(v);
                    return null;
                });

            if (shouldCallOnLeaveVertexFor(v))
                scope.fork(() -> {
                    this.onLeaveVertex(v);
                    return null;
                });

            if (v.shouldCallOnLeaveGraphFor(this))
                scope.fork(() -> {
                    v.onLeaveGraph(this);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if an edge can be added to the graph.
     * The method checks if the source and target vertices are present in the graph and if the edge is not already
     * present in the graph or would create a cycle. Then, the method checks the policy and validation predicates.
     * @param source The source vertex of the edge.
     * @param target The target vertex of the edge.
     * @return A result indicating if the edge can be added or not.
     */
    public final Result<Void, ShouldAddEdgeFailure> shouldAddEdge(V source, V target) {
        if (containsEdge(target, source))
            return fail(new FailureResults.GraphCycleDetected("The edge addition would create a cycle in the graph"));

        if (containsEdge(source, target))
            return fail(new FailureResults.EdgeAlreadyExists("The edge already exists in the graph"));

        return shouldAddEdgeNoCheckEdge(source, target);
    }

    /**
     * Checks if an edge can be added to the graph without checking if the edge is already present in the graph or
     * causing a cycle.
     * @param source The source vertex of the edge.
     * @param target The target vertex of the edge.
     * @return A result indicating if the edge can be added or not.
     */
    protected Result<Void, ShouldAddEdgeFailure> shouldAddEdgeNoCheckEdge(V source, V target) {
        if (!containsVertex(source))
            return fail(new FailureResults.VertexNotPresent("The source vertex is not present in the graph", source));

        if (!containsVertex(target))
            return fail(new FailureResults.VertexNotPresent("The target vertex is not present in the graph", target));

        return edgeAdditionOperationCheck(source, target);
    }

    /**
     * Checks the policies and validations for adding an edge.
     * @param source The source vertex of the edge.
     * @param target The target vertex of the edge.
     * @return A result indicating if the edge can be added or not.
     */
    protected Result<Void, ShouldAddEdgeFailure> edgeAdditionOperationCheck(V source, V target) {
        switch (policiesManager.stateOf(CREATE_EDGE_POLICY, source, target)) {
            case ACCEPT -> {
                if (!validationsManager.validateOperation(CREATE_EDGE_VALIDATION, source, target))
                    return fail(new FailureResults.RejectedByGraphPolicy("The graph validation rejected the edge addition", this));
            }
            case REJECT -> {
                return fail(new FailureResults.RejectedByGraphPolicy("The graph policy rejected the edge addition", this));
            }
            case UNSET -> {
                if (!acceptUnsetPolicy())
                    return fail(new FailureResults.RejectedByGraphPolicy("The graph policy rejected the edge addition", this));
            }
        }

        switch (source.policiesManager().stateOf(CONNECT_CHILD_POLICY, target)) {
            case ACCEPT -> {}
            case PolicyState.REJECT -> {
                return fail(new FailureResults.RejectedByVertexPolicy("The vertex policy rejected the operation", source));
            }
            case PolicyState.UNSET -> {
                if (!source.acceptOnUnsetPolicy())
                    return fail(new FailureResults.RejectedByVertexPolicy("The vertex policy rejected the operation", source));
            }
        }

        switch (target.policiesManager().stateOf(CONNECT_PARENT_POLICY, source)) {
            case ACCEPT -> {}
            case PolicyState.REJECT -> {
                return fail(new FailureResults.RejectedByVertexPolicy("The vertex policy rejected the operation", target));
            }
            case PolicyState.UNSET -> {
                if (!target.acceptOnUnsetPolicy())
                    return fail(new FailureResults.RejectedByVertexPolicy("The vertex policy rejected the operation", target));
            }
        }

        if (!validationsManager.validateOperation(CREATE_EDGE_VALIDATION, source, target))
            return fail(new FailureResults.RejectedByGraphValidation("The graph validation rejected the edge addition", this));

        switch (source.validationsManager().validateOperation(CONNECT_CHILD_VALIDATION, target)) {
            case Result.Failure<Void, Set<String>> v -> {
                String message = v.cause().stream().reduce("", (a, b) -> a + "\n\t" + b);
                return fail(new FailureResults.RejectedByVertexValidation("The vertex validations rejected the operation:\n\t" + message, source));
            }
            case Result.Success<Void, Set<String>> _ -> {}
        }

        switch (target.validationsManager().validateOperation(CONNECT_CHILD_VALIDATION, source)) {
            case Result.Failure<Void, Set<String>> v -> {
                String message = v.cause().stream().reduce("", (a, b) -> a + "\n\t" + b);
                return fail(new FailureResults.RejectedByVertexValidation("The vertex validation rejected the operation:\n\t" + message, target));
            }
            case Result.Success<Void, Set<String>> _ -> {}
        }
        return success();
    }

    /**
     * Adds an edge to the graph. The method checks if the edge can be added and then adds it.
     * @param source The source vertex of the edge.
     * @param target The target vertex of the edge.
     * @return A result indicating if the edge was added or not.
     * @throws NullPointerException If the source or target vertex is null.
     * @throws GraphOperationException If the edge could not be added to the graph.
     * @throws UnsupportedOperationException If the graph does not support the operation or fails to create an edge.
     */
    public final Result<E, EdgeAdditionFailure> addEdge(V source, V target, double weight)
            throws NullPointerException, GraphOperationException, UnsupportedOperationException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        return switch (executeValidatedEdgeAdditionWithoutCallbacks(source, target, weight)) {
            case Result.Failure<E, EdgeAdditionFailure> f -> f;
            case Result.Success<E, EdgeAdditionFailure> s -> {
                processEdgeAdditionCallbacks(source, target);
                yield  s;
            }
        };
    }

    final Result<E, EdgeAdditionFailure> executeValidatedEdgeAdditionWithoutCallbacks(V source, V target, double weight)
            throws NullPointerException, GraphOperationException, UnsupportedOperationException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        return switch (shouldAddEdge(source, target)) {
            case Result.Failure<?, ShouldAddEdgeFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
            };

            case Result.Success<Void, ?> _ -> {
                E e;

                try {
                    e = graph.addEdge(source, target);
                } catch (GraphCycleProhibitedException ex) {
                    yield fail(new FailureResults.GraphCycleDetected("The edge addition would create a cycle in the graph: " + ex.getMessage()));

                } catch (IllegalArgumentException ex) {
                    throw new GraphOperationException("The edge could not be added to the graph");
                }

                if (e == null) {
                    if (containsEdge(source, target))
                        yield fail(new FailureResults.EdgeAlreadyExists("The edge already exists in the graph"));
                    throw new GraphOperationException("The edge could not be added to the graph");
                }

                setEdgeWeight(e, weight);
                yield success(e);
            }
        };
    }

    final void processEdgeAdditionCallbacks(V source, V target) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var addEdgeCallback : addEdgeCallbacks.values())
                scope.fork(() -> {
                    addEdgeCallback.accept(source, target);
                    return null;
                });

            if (policiesManager.stateOf(ON_ADD_EDGE_POLICY, source, target) == ACCEPT)
                scope.fork(() -> {
                    onConnect(source, target);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if an edge can be removed from the graph.
     * The method checks if the edge is present in the graph, if the source and target vertices are present in the graph,
     * and then checks the policy and validation predicates.
     * @param source The source vertex of the edge.
     * @param target The target vertex of the edge.
     * @return A result indicating if the edge can be removed or not.
     */
    public final Result<Void, ShouldRemoveEdgeFailure> shouldRemoveEdge(V source, V target) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        if (!containsVertex(source))
            return fail(new FailureResults.VertexNotPresent("The source vertex is not present in the graph", source));

        if (!containsVertex(target))
            return fail(new FailureResults.VertexNotPresent("The target vertex is not present in the graph", target));

        if (!containsEdge(source, target))
            return fail(new FailureResults.EdgeNotPresent("The edge is not present in the graph"));

        switch (policiesManager.stateOf(REMOVE_EDGE_POLICY, source, target)) {
            case REJECT -> {
                return fail(new FailureResults.RejectedByGraphPolicy("The graph policy rejected the edge removal", this));
            }
            case ACCEPT -> {}
            case UNSET -> {
                if (!acceptUnsetPolicy())
                    return fail(new FailureResults.RejectedByGraphPolicy("The graph policy rejected the edge removal", this));
            }
        }

        switch (source.policiesManager().stateOf(DISCONNECT_CHILD_POLICY, target)) {
            case ACCEPT -> {}
            case PolicyState.REJECT -> {
                return fail(new FailureResults.RejectedByVertexPolicy("Edge removal rejected by the vertex policy", source));
            }
            case PolicyState.UNSET -> {
                if (!source.acceptOnUnsetPolicy())
                    return fail(new FailureResults.RejectedByVertexPolicy("Edge removal rejected by the vertex policy", source));
            }
        }

        switch (target.policiesManager().stateOf(DISCONNECT_PARENT_POLICY, source)) {
            case ACCEPT -> {}
            case PolicyState.REJECT -> {
                return fail(new FailureResults.RejectedByVertexPolicy("Edge removal rejected by the vertex policy", target));
            }
            case PolicyState.UNSET -> {
                if (!target.acceptOnUnsetPolicy())
                    return fail(new FailureResults.RejectedByVertexPolicy("Edge removal rejected by the vertex policy", target));
            }
        }

        if (!validationsManager.validateOperation(REMOVE_EDGE_VALIDATION, source, target))
            return fail(new FailureResults.RejectedByGraphValidation("Edge removal rejected by the graph validation", this));

        switch (source.validationsManager().validateOperation(DISCONNECT_CHILD_VALIDATION, target)) {
            case Result.Failure<Void, Set<String>> v -> {
                String message = v.cause().stream().reduce("", (a, b) -> a + "\n\t" + b);
                return fail(new FailureResults.RejectedByVertexValidation("Edge removal rejected by the vertex validations:\n\t" + message, source));
            }
            case Result.Success<Void, Set<String>> _ -> {}
        }

        switch (target.validationsManager().validateOperation(DISCONNECT_CHILD_VALIDATION, source)) {
            case Result.Failure<Void, Set<String>> v -> {
                String message = v.cause().stream().reduce("", (a, b) -> a + "\n\t" + b);
                return fail(new FailureResults.RejectedByVertexValidation("Edge removal rejected by the vertex validations:\n\t" + message, target));
            }
            case Result.Success<Void, Set<String>> _ -> {}
        }

        return success();
    }

    /**
     * Removes an edge from the graph. The method checks if the edge can be removed and then removes it.
     * @param source The source vertex of the edge.
     * @param target The target vertex of the edge.
     * @return A result indicating if the edge was removed or not.
     */
    public final Result<E, EdgeRemovalFailure> removeEdge(V source, V target) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        return switch (shouldRemoveEdge(source, target)) {
            case Result.Failure<?, ShouldRemoveEdgeFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case FailureResults.EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
            };

            case Result.Success<Void, ?> _ -> {
                E e = graph.removeEdge(source, target);

                if (e == null)
                    throw new GraphOperationException("The edge could not be removed from the graph");

                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    for (var removeEdgeCallback : removeEdgeCallbacks.values())
                        scope.fork(() -> {
                            removeEdgeCallback.accept(source, target);
                            return null;
                        });

                    if (policiesManager.stateOf(ON_ADD_EDGE_POLICY, source, target) == ACCEPT)
                        scope.fork(() -> {
                            onDisconnect(source, target);
                            return null;
                        });

                    scope.join();
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }

                yield success(e);
            }
        };
    }

    /**
     * Removes all incoming edges to a vertex. The method checks if all the edges can be removed and then removes them.
     * @param v The vertex to remove the incoming edges from.
     * @return A result indicating if the edges were removed or not.
     */
    public final Result<Void, Map<V, EdgeRemovalFailure>> processIncomingEdgesRemoval(V v) {
        return null;
    }

    public final Result<Void, Map<V, EdgeRemovalFailure>> processOutgoingEdgesRemoval(V v) {
        return null;
    }

    final Result<E, EdgeRemovalFailure> executeValidatedEdgeRemovalWithoutCallbacks(V source, V target)
            throws NullPointerException, GraphOperationException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        return switch (shouldRemoveEdge(source, target)) {
            case Result.Failure<?, ShouldRemoveEdgeFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case FailureResults.EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
            };

            case Result.Success<Void, ?> _ -> {
                E e = graph.removeEdge(source, target);

                if (e == null)
                    throw new GraphOperationException("The edge could not be removed from the graph");

                yield success(e);
            }
        };
    }

    public final void processEdgeRemovalCallbacks(V source, V target) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var removeEdgeCallback : removeEdgeCallbacks.values())
                scope.fork(() -> {
                    removeEdgeCallback.accept(source, target);
                    return null;
                });

            if (policiesManager.stateOf(ON_ADD_EDGE_POLICY, source, target) == ACCEPT)
                scope.fork(() -> {
                    onDisconnect(source, target);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes a vertex from the graph if it is not connected to any other vertex.
     * @param v The vertex to remove.
     * @return A result indicating if the vertex was removed or not. If the vertex is connected to other vertices,
     * the result is a success with no value. If the vertex is not connected to any other vertex, the result of removing
     * the vertex is returned.
     */
    public final Result<V, VertexRemovalFailure> removeUnconnectedVertex(V v) throws NullPointerException {
        Objects.requireNonNull(v);

        if (degreeOf(v) > 0)
            return success();

        return removeVertex(v);
    }

    protected void addEnterVertexValidation(Predicate<V> predicate) {
        Objects.requireNonNull(predicate);
        validationsManager.addValidationForOperation(ADD_VERTEX_VALIDATION, predicate);
    }

    protected void addLeaveVertexValidation(Predicate<V> predicate) {
        Objects.requireNonNull(predicate);
        validationsManager.addValidationForOperation(REMOVE_VERTEX_VALIDATION, predicate);
    }

    protected void addCreateEdgeValidation(BiPredicate<V, V> predicate) {
        Objects.requireNonNull(predicate);
        validationsManager.addValidationForOperation(CREATE_EDGE_VALIDATION, predicate);
    }

    protected void addRemoveEdgeValidation(BiPredicate<V, V> predicate) {
        Objects.requireNonNull(predicate);
        validationsManager.addValidationForOperation(REMOVE_EDGE_VALIDATION, predicate);
    }

    protected void onEnterVertex(V v) throws RuntimeException {}

    protected void onLeaveVertex(V v) throws RuntimeException {}

    protected void onConnect(V source, V target) throws RuntimeException {} // TODO

    protected void onDisconnect(V source, V target) throws RuntimeException {} // TODO

    @Override
    public String toString() {
        return graph.toString();
    }

    public enum VertexCallbackType implements Graph.CallbackType {
        ON_ADD_VERTEX, ON_REMOVE_VERTEX
    }

    public enum EdgeCallbackType implements Graph.CallbackType {
        ON_ADD_EDGE, ON_REMOVE_EDGE
    }

    public sealed interface VertexAdditionFailure extends Graph.FailureOperationResult permits
            FailureResults.VertexAlreadyPresent,
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation {}

    public sealed interface FailureOperationResult {
        String message();
    }

    public sealed interface ShouldAddVertexFailure extends FailureOperationResult permits FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.VertexAlreadyPresent, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation {}

    public sealed interface ShouldRemoveVertexFailure extends FailureOperationResult permits
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation,
            FailureResults.VertexNotPresent {}

    public sealed interface VertexRemovalFailure extends FailureOperationResult permits
            FailureResults.VertexNotPresent,
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation {}

    public sealed interface ShouldAddEdgeFailure extends FailureOperationResult permits
            FailureResults.GraphCycleDetected,
            FailureResults.EdgeAlreadyExists,
            FailureResults.VertexNotPresent,
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation {}

    public sealed interface EdgeAdditionFailure extends FailureOperationResult permits
            FailureResults.GraphCycleDetected,
            FailureResults.EdgeAlreadyExists,
            FailureResults.VertexNotPresent,
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation {}

    public sealed interface ShouldRemoveEdgeFailure extends FailureOperationResult permits
            FailureResults.EdgeNotPresent,
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation,
            FailureResults.VertexNotPresent {}

    public sealed interface EdgeRemovalFailure extends FailureOperationResult permits
            FailureResults.EdgeNotPresent,
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation,
            FailureResults.VertexNotPresent {}

    public sealed interface CallbackType {}
}
