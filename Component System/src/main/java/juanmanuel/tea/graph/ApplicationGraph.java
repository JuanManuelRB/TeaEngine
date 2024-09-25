package juanmanuel.tea.graph;

import juanmanuel.tea.graph.operation_failures.FailureResults.*;
import juanmanuel.tea.graph.policy.GraphOperationsPolicies;
import juanmanuel.tea.graph.validation.GraphOperationValidator;
import juanmanuel.tea.utils.Result;
import org.jgrapht.graph.GraphCycleProhibitedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
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

@SuppressWarnings("unused")
public non-sealed class ApplicationGraph
        <V extends ApplicationVertex<V>>
        extends Graph<V, ApplicationEdge> implements GraphElement {

    final GraphOperationsPolicies policiesManager = new GraphOperationsPolicies();
    final protected GraphOperationValidator<V> validationsManager = new GraphOperationValidator<>();
//    final protected CallbackManager<Self, Graph<Self, ?>> callbackManager; // Externally added callbacks TODO

    private final Map<?, Consumer<V>> addVertexCallbacks = new HashMap<>();
    private final Map<?, Consumer<V>> removeVertexCallbacks = new HashMap<>();
    private final Map<?, BiConsumer<V, V>> addEdgeCallbacks = new HashMap<>();
    private final Map<?, BiConsumer<V, V>> removeEdgeCallbacks = new HashMap<>();
    private boolean acceptUnsetPolicy = false;

    public sealed interface CallbackType {}

    public enum VertexCallbackType implements CallbackType {
        ON_ADD_VERTEX, ON_REMOVE_VERTEX
    }

    public enum EdgeCallbackType implements CallbackType {
        ON_ADD_EDGE, ON_REMOVE_EDGE
    }

    public ApplicationGraph(Class<? extends ApplicationEdge> edgeClass) {
        super(edgeClass);
    }

    public ApplicationGraph(Class<? extends ApplicationEdge> edgeClass, VertexSupplier<V> vertexSupplier) {
        super(edgeClass, vertexSupplier);
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

    public sealed interface FailureOperationResult {
        String message();
    }

    public sealed interface ShouldAddVertexFailure extends FailureOperationResult permits RejectedByGraphPolicy, RejectedByGraphValidation, VertexAlreadyPresent, RejectedByVertexPolicy, RejectedByVertexValidation {}

    /**
     * Checks if a vertex can be added to the graph without checking if the vertex is already present in the graph.
     * @param v The vertex to add.
     * @return A result indicating if the vertex can be added or not.
     */
    public Result<Void, ShouldAddVertexFailure> shouldAddVertexNoCheckIfPresent(V v) {
        switch (policiesManager().stateOf(ADD_VERTEX_POLICY, v)) {
            case REJECT -> {
                return fail(new RejectedByGraphPolicy("The graph policy rejected the vertex addition", this));
            }
            case UNSET -> {
                if (acceptUnsetPolicy())
                    return success();

                return fail(new RejectedByGraphPolicy("The graph policy rejected the vertex addition", this));
            }
        }

        if (!validationsManager.validateOperation(ADD_VERTEX_VALIDATION, v))
            return fail(new RejectedByGraphValidation("The graph validation rejected the vertex addition", this));

        switch (v.policiesManager().stateOf(ADD_TO_GRAPH_POLICY, this)) {
            case REJECT -> {
                return fail(new RejectedByVertexPolicy("The vertex policy rejected the operation", v));
            }

            case UNSET -> {
                if (!v.acceptOnUnsetPolicy())
                    return fail(new RejectedByVertexPolicy("The vertex policy rejected the operation", v));
            }
        }

        if (v.validationsManager().validateOperation(ADD_TO_GRAPH_VALIDATION, this) instanceof Result.Failure<?, Set<String>> f)
            return fail(new RejectedByVertexValidation(
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
            return fail(new VertexAlreadyPresent("The vertex is already in the graph"));

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
    public void setAcceptUnsetPolicy(boolean behavior) {
        acceptUnsetPolicy = behavior;
    }

    public sealed interface ShouldRemoveVertexFailure extends FailureOperationResult permits
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation,
            VertexNotPresent {}

    /**
     * Checks if a vertex can be removed from the graph.
     * @param v The vertex to remove.
     * @return A result indicating if the vertex can be removed or not.
     */
    public Result<Void, ShouldRemoveVertexFailure> shouldRemoveVertex(V v) {
        Objects.requireNonNull(v);

        if (!containsVertex(v))
            return fail(new VertexNotPresent("The vertex is not present in the graph", v));

        switch (policiesManager.stateOf(REMOVE_VERTEX_POLICY, v)) {
            case ACCEPT -> {}
            case REJECT -> {
                return fail(new RejectedByGraphPolicy("The graph policy rejected the vertex removal", this));
            }
            case UNSET -> {
                if (!acceptUnsetPolicy())
                    return fail(new RejectedByGraphPolicy("The graph policy rejected the vertex removal", this));
            }
        }

        if (!validationsManager.validateOperation(REMOVE_VERTEX_VALIDATION, v))
            return fail(new RejectedByGraphValidation("The graph validation rejected the vertex removal", this));

        var paResStr = getParents(v)
                .parallelStream()
                .map(parent -> shouldRemoveEdge(parent, v)); // TODO Gather first only

        var chResStr = getChildren(v)
                .parallelStream()
                .map(child -> shouldRemoveEdge(v, child));

        return Stream.concat(paResStr, chResStr)
                .parallel()
                .filter(Result::isFailure)
                .findFirst()
                .map(shReEdFa -> switch (((Result.Failure<Void, ShouldRemoveEdgeFailure>) shReEdFa).cause()) {
                    case RejectedByGraphPolicy rejectedByGraphPolicy ->
                            Result.<Void, ShouldRemoveVertexFailure>fail(rejectedByGraphPolicy);

                    case RejectedByGraphValidation rejectedByGraphValidation ->
                            Result.<Void, ShouldRemoveVertexFailure>fail(rejectedByGraphValidation);

                    case RejectedByVertexPolicy rejectedByVertexPolicy ->
                            Result.<Void, ShouldRemoveVertexFailure>fail(rejectedByVertexPolicy);

                    case RejectedByVertexValidation rejectedByVertexValidation ->
                            Result.<Void, ShouldRemoveVertexFailure>fail(rejectedByVertexValidation);

                    case VertexNotPresent vertexNotPresent ->
                            throw new IllegalStateException("The child or parent is not present in the graph. Graph is inconsistent.");
                    case EdgeNotPresent edgeNotPresent ->
                            throw new IllegalStateException("The edge is not present in the graph. Graph is inconsistent.");
                })
                .orElse(success());
    }

    public sealed interface VertexAdditionFailure extends FailureOperationResult permits
            VertexAlreadyPresent,
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation {}

    /**
     * Adds a vertex to the graph. The method checks if the vertex can be added and then adds it.
     * @param v The vertex to add.
     * @return A result indicating if the vertex was added or not.
     * @throws NullPointerException If the vertex is null.
     * @throws GraphOperationException If the vertex could not be added to the graph.
     */
    public Result<V, VertexAdditionFailure> processVertexAddition(V v)
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
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexAlreadyPresent vertexAlreadyPresent -> fail(vertexAlreadyPresent);
            };

            case Result.Success<Void, ?> _ -> {
                if (!super.addVertex(v))
                    throw new GraphOperationException("The vertex could not be added to the graph");

                yield success(v);
            }
        };
    }

    void processVertexAdditionCallbacks(V v) {
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

    public sealed interface VertexRemovalFailure extends FailureOperationResult permits
            VertexNotPresent,
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation {}

    /**
     * Removes a vertex from the graph. The method checks if the vertex can be removed and then removes it.
     * @param v The vertex to remove.
     * @return A result indicating if the vertex was removed or not.
     * @throws NullPointerException If the vertex is null.
     * @throws GraphOperationException If the vertex could not be removed from the graph.
     */
    public Result<V, VertexRemovalFailure> processVertexRemoval(V v) throws NullPointerException, GraphOperationException {
        Objects.requireNonNull(v);

        return switch (shouldRemoveVertex(v)) {
            case Result.Failure<?, ShouldRemoveVertexFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Result.Success<Void, ?> _ -> {
                var parents = getParents(v);
                var children = getChildren(v);

                if (!super.removeVertex(v))
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

    public sealed interface ShouldAddEdgeFailure extends FailureOperationResult permits
            GraphCycleDetected,
            EdgeAlreadyExists,
            VertexNotPresent,
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation {}

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
            return fail(new GraphCycleDetected("The edge addition would create a cycle in the graph"));

        if (containsEdge(source, target))
            return fail(new EdgeAlreadyExists("The edge already exists in the graph"));

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
            return fail(new VertexNotPresent("The source vertex is not present in the graph", source));

        if (!containsVertex(target))
            return fail(new VertexNotPresent("The target vertex is not present in the graph", target));

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
                    return fail(new RejectedByGraphPolicy("The graph validation rejected the edge addition", this));
            }
            case REJECT -> {
                return fail(new RejectedByGraphPolicy("The graph policy rejected the edge addition", this));
            }
            case UNSET -> {
                if (!acceptUnsetPolicy())
                    return fail(new RejectedByGraphPolicy("The graph policy rejected the edge addition", this));
            }
        }

        switch (source.policiesManager().stateOf(CONNECT_CHILD_POLICY, target)) {
            case ACCEPT -> {}
            case REJECT -> {
                return fail(new RejectedByVertexPolicy("The vertex policy rejected the operation", source));
            }
            case UNSET -> {
                if (!source.acceptOnUnsetPolicy())
                    return fail(new RejectedByVertexPolicy("The vertex policy rejected the operation", source));
            }
        }

        switch (target.policiesManager().stateOf(CONNECT_PARENT_POLICY, source)) {
            case ACCEPT -> {}
            case REJECT -> {
                return fail(new RejectedByVertexPolicy("The vertex policy rejected the operation", target));
            }
            case UNSET -> {
                if (!target.acceptOnUnsetPolicy())
                    return fail(new RejectedByVertexPolicy("The vertex policy rejected the operation", target));
            }
        }

        if (!validationsManager.validateOperation(CREATE_EDGE_VALIDATION, source, target))
            return fail(new RejectedByGraphValidation("The graph validation rejected the edge addition", this));

        switch (source.validationsManager().validateOperation(CONNECT_CHILD_VALIDATION, target)) {
            case Result.Failure<Void, Set<String>> v -> {
                String message = v.cause().stream().reduce("", (a, b) -> a + "\n\t" + b);
                return fail(new RejectedByVertexValidation("The vertex validations rejected the operation:\n\t" + message, source));
            }
            case Result.Success<Void, Set<String>> _ -> {}
        }

        switch (target.validationsManager().validateOperation(CONNECT_CHILD_VALIDATION, source)) {
            case Result.Failure<Void, Set<String>> v -> {
                String message = v.cause().stream().reduce("", (a, b) -> a + "\n\t" + b);
                return fail(new RejectedByVertexValidation("The vertex validation rejected the operation:\n\t" + message, target));
            }
            case Result.Success<Void, Set<String>> _ -> {}
        }
        return success();
    }

    public sealed interface EdgeAdditionFailure extends FailureOperationResult permits
            GraphCycleDetected,
            EdgeAlreadyExists,
            VertexNotPresent,
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation {}

    /**
     * Adds an edge to the graph. The method checks if the edge can be added and then adds it.
     * @param source The source vertex of the edge.
     * @param target The target vertex of the edge.
     * @return A result indicating if the edge was added or not.
     * @throws NullPointerException If the source or target vertex is null.
     * @throws GraphOperationException If the edge could not be added to the graph.
     * @throws UnsupportedOperationException If the graph does not support the operation or fails to create an edge.
     */
    public final Result<ApplicationEdge, EdgeAdditionFailure> processEdgeAddition(V source, V target, double weight)
            throws NullPointerException, GraphOperationException, UnsupportedOperationException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        return switch (executeValidatedEdgeAdditionWithoutCallbacks(source, target, weight)) {
            case Result.Failure<ApplicationEdge, EdgeAdditionFailure> f -> f;
            case Result.Success<ApplicationEdge, EdgeAdditionFailure> s -> {
                processEdgeAdditionCallbacks(source, target);
                yield  s;
            }
        };
    }

    final Result<ApplicationEdge, EdgeAdditionFailure> executeValidatedEdgeAdditionWithoutCallbacks(V source, V target, double weight)
            throws NullPointerException, GraphOperationException, UnsupportedOperationException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        return switch (shouldAddEdge(source, target)) {
            case Result.Failure<?, ShouldAddEdgeFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
            };

            case Result.Success<Void, ?> _ -> {
                ApplicationEdge e;

                try {
                    e = super.addEdge(source, target);
                } catch (GraphCycleProhibitedException ex) {
                    yield fail(new GraphCycleDetected("The edge addition would create a cycle in the graph: " + ex.getMessage()));

                } catch (IllegalArgumentException ex) {
                    throw new GraphOperationException("The edge could not be added to the graph");
                }

                if (e == null) {
                    if (containsEdge(source, target))
                        yield fail(new EdgeAlreadyExists("The edge already exists in the graph"));
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

    public sealed interface ShouldRemoveEdgeFailure extends FailureOperationResult permits
            EdgeNotPresent,
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation,
            VertexNotPresent {}

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
            return fail(new VertexNotPresent("The source vertex is not present in the graph", source));

        if (!containsVertex(target))
            return fail(new VertexNotPresent("The target vertex is not present in the graph", target));

        if (!containsEdge(source, target))
            return fail(new EdgeNotPresent("The edge is not present in the graph"));

        switch (policiesManager.stateOf(REMOVE_EDGE_POLICY, source, target)) {
            case REJECT -> {
                return fail(new RejectedByGraphPolicy("The graph policy rejected the edge removal", this));
            }
            case ACCEPT -> {}
            case UNSET -> {
                if (!acceptUnsetPolicy())
                    return fail(new RejectedByGraphPolicy("The graph policy rejected the edge removal", this));
            }
        }

        switch (source.policiesManager().stateOf(DISCONNECT_CHILD_POLICY, target)) {
            case ACCEPT -> {}
            case REJECT -> {
                return fail(new RejectedByVertexPolicy("Edge removal rejected by the vertex policy", source));
            }
            case UNSET -> {
                if (!source.acceptOnUnsetPolicy())
                    return fail(new RejectedByVertexPolicy("Edge removal rejected by the vertex policy", source));
            }
        }

        switch (target.policiesManager().stateOf(DISCONNECT_PARENT_POLICY, source)) {
            case ACCEPT -> {}
            case REJECT -> {
                return fail(new RejectedByVertexPolicy("Edge removal rejected by the vertex policy", target));
            }
            case UNSET -> {
                if (!target.acceptOnUnsetPolicy())
                    return fail(new RejectedByVertexPolicy("Edge removal rejected by the vertex policy", target));
            }
        }

        if (!validationsManager.validateOperation(REMOVE_EDGE_VALIDATION, source, target))
            return fail(new RejectedByGraphValidation("Edge removal rejected by the graph validation", this));

        switch (source.validationsManager().validateOperation(DISCONNECT_CHILD_VALIDATION, target)) {
            case Result.Failure<Void, Set<String>> v -> {
                String message = v.cause().stream().reduce("", (a, b) -> a + "\n\t" + b);
                return fail(new RejectedByVertexValidation("Edge removal rejected by the vertex validations:\n\t" + message, source));
            }
            case Result.Success<Void, Set<String>> _ -> {}
        }

        switch (target.validationsManager().validateOperation(DISCONNECT_CHILD_VALIDATION, source)) {
            case Result.Failure<Void, Set<String>> v -> {
                String message = v.cause().stream().reduce("", (a, b) -> a + "\n\t" + b);
                return fail(new RejectedByVertexValidation("Edge removal rejected by the vertex validations:\n\t" + message, target));
            }
            case Result.Success<Void, Set<String>> _ -> {}
        }

        return success();
    }

    public sealed interface EdgeRemovalFailure extends FailureOperationResult permits
            EdgeNotPresent,
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation,
            VertexNotPresent {}

    /**
     * Removes an edge from the graph. The method checks if the edge can be removed and then removes it.
     * @param source The source vertex of the edge.
     * @param target The target vertex of the edge.
     * @return A result indicating if the edge was removed or not.
     */
    public final Result<ApplicationEdge, EdgeRemovalFailure> processEdgeRemoval(V source, V target) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        return switch (shouldRemoveEdge(source, target)) {
            case Result.Failure<?, ShouldRemoveEdgeFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
            };

            case Result.Success<Void, ?> _ -> {
                ApplicationEdge e = super.removeEdge(source, target);

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

    final Result<ApplicationEdge, EdgeRemovalFailure> executeValidatedEdgeRemovalWithoutCallbacks(V source, V target)
            throws NullPointerException, GraphOperationException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        return switch (shouldRemoveEdge(source, target)) {
            case Result.Failure<?, ShouldRemoveEdgeFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
            };

            case Result.Success<Void, ?> _ -> {
                ApplicationEdge e = super.removeEdge(source, target);

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

        return processVertexRemoval(v);
    }

    @Override
    public boolean addVertex(V v) {
        Objects.requireNonNull(v);
        return super.addVertex(v);
    }

    @Override
    public boolean removeVertex(V v) {
        Objects.requireNonNull(v);
        return super.removeVertex(v);
    }

    @Override
    public final ApplicationEdge addEdge(V sourceVertex, V targetVertex) {
        Objects.requireNonNull(sourceVertex);
        Objects.requireNonNull(targetVertex);

        return super.addEdge(sourceVertex, targetVertex);
    }

    @Override
    public final ApplicationEdge removeEdge(V sourceVertex, V targetVertex) {
        Objects.requireNonNull(sourceVertex);
        Objects.requireNonNull(targetVertex);

        return super.removeEdge(sourceVertex, targetVertex);
    }

    @Override
    public final boolean removeEdge(ApplicationEdge e) {
        var source = getEdgeSource(e);
        var target = getEdgeTarget(e);
        return removeEdge(source, target) != null;
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

}
