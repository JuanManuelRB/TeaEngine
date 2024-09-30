package juanmanuel.tea.graph;

import juanmanuel.tea.graph.callbacks.VertexCallbackManager;
import juanmanuel.tea.graph.operation_failures.FailureResults;
import juanmanuel.tea.graph.policy.VertexOperationsPolicies;
import juanmanuel.tea.graph.validation.VertexOperationValidator;
import juanmanuel.tea.utils.Result;
import org.jgrapht.graph.GraphCycleProhibitedException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static juanmanuel.tea.graph.policy.PolicyState.ACCEPT;
import static juanmanuel.tea.graph.policy.PolicyState.UNSET;
import static juanmanuel.tea.graph.policy.VertexPolicy.EffectVertexPolicy.*;
import static juanmanuel.tea.graph.policy.VertexPolicy.GraphModificationVertexPolicy.ON_ENTER_GRAPH_POLICY;
import static juanmanuel.tea.graph.policy.VertexPolicy.GraphModificationVertexPolicy.ON_LEAVE_GRAPH_POLICY;
import static juanmanuel.tea.utils.Result.fail;
import static juanmanuel.tea.utils.Result.success;

/// A vertex that can be used in a [Graph].
/// @param <Self> The type of the vertex.
@NullMarked
public abstract non-sealed class Vertex<Self extends Vertex<Self>> implements GraphElement {
    /// The policies manager of this vertex.
    /// This is used to manage the operations policies of the vertex.
    /// The policies manager is created lazily.
    @Nullable
    protected VertexOperationsPolicies policiesManager;

    /// The validations manager of this vertex.
    /// This is used to manage the operations validations of the vertex.
    /// The validations manager is created lazily.
    @Nullable
    protected VertexOperationValidator<Self> validationsManager;

    /// The callbacks manager of this vertex.
    /// This is used to manage the operations callbacks of the vertex.
    /// The callbacks manager is created lazily.
    @Nullable
    protected VertexCallbackManager<Self> callbackManager;

    /// Whether the policy should accept the operation when its state is UNSET.
    protected boolean acceptOnUnsetPolicy;

    public static <V extends Vertex<V>, E extends ApplicationEdge> Set<V> verticesIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        return graph.vertexSet();
    }

    public static <V extends Vertex<V>, E extends ApplicationEdge> Set<E> edgesIn(Graph<V, E> graph) {
        Objects.requireNonNull(graph);
        return graph.edgeSet();
    }

    /// @return True if the policy should accept the operation when it is not set, false otherwise.
    public boolean acceptOnUnsetPolicy() {
        return acceptOnUnsetPolicy;
    }

    /// Gets the policies manager of this vertex or creates it if it does not exist.
    /// @return The policies manager of this vertex.
    VertexOperationsPolicies policiesManager() {
        if (policiesManager == null)
            policiesManager = new VertexOperationsPolicies();

        return policiesManager;
    }

    /// Gets the validations manager of this vertex or creates it if it does not exist.
    /// @return The validations manager of this vertex.
    protected VertexOperationValidator<Self> validationsManager() {
        if (validationsManager == null)
            validationsManager = new VertexOperationValidator<>();
        return validationsManager;
    }

    /// Gets the callbacks manager of this vertex or creates it if it does not exist.
    /// @return The callbacks manager of this vertex.
    protected VertexCallbackManager<Self> callbackManager() {
        if (callbackManager == null)
            callbackManager = new VertexCallbackManager<>();

        return callbackManager;
    }

    /// @return this vertex as a Self type.
    @SuppressWarnings("unchecked")
    protected Self self() {
        return (Self) this;
    }

    /// Checks if this vertex has the given vertex as a child.
    /// @param child The vertex to check
    /// @return true if the other vertex is a child of this vertex, false otherwise
    public final boolean hasChild(Self child, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return graph.egressEdgesOf(self())
                .stream()
                .anyMatch(e -> graph.getEdgeTarget(e).equals(child));
    }

    /// Checks if this vertex has a child of the given type.
    /// @param childClass The type of the vertex to check.
    /// @return true if this vertex has a child of the given type, false otherwise
    public final boolean hasChild(Class<? extends Self> childClass, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(childClass);
        Objects.requireNonNull(graph);

        return graph.egressEdgesOf(self())
                .stream()
                .map(graph::getEdgeTarget)
                .anyMatch(childClass::isInstance);
    }

    /// Checks how many children of the given class this vertex has.
    /// @param childClass The class of the children to check
    /// @return The number of children of the given class this GameObject has
    public final long numberOfChildren(Class<? extends Self> childClass, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(childClass);
        Objects.requireNonNull(graph);

        return graph.egressEdgesOf(self())
                .stream()
                .map(graph::getEdgeTarget)
                .filter(childClass::isInstance)
                .count();
    }

    /// Checks if this vertex has the given vertex as a parent.
    /// @param parent The head to check
    /// @return true if this GameObject has the head, false otherwise
    public final boolean hasParent(Self parent, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return graph.ingressEdgesOf(self())
                .stream()
                .anyMatch(e -> graph.getEdgeSource(e)
                                .map(o -> o.equals(parent))
                                .orElse(false)
                );
    }

    /**
     * Checks if this vertex has a parent of the given class.
     * @param parentClass The class of the head to check
     * @return true if this GameObject has a head of the given class, false otherwise
     */
    public final boolean hasParent(Class<? extends Vertex<?>> parentClass, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(parentClass);
        Objects.requireNonNull(graph);

        return graph.ingressEdgesOf(self())
                .stream()
                .map(graph::getEdgeSource)
                .anyMatch(parentClass::isInstance);
    }

    /// Checks how many parents of the given class this vertex has.
    /// @param parentClass The class of the parents to check
    /// @return The number of parents of the given class this GameObject has
    public final long numberOfParents(Class<? extends Vertex<?>> parentClass, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(parentClass);
        Objects.requireNonNull(graph);

        return graph.ingressEdgesOf(self())
                .stream()
                .map(graph::getEdgeSource)
                .filter(parentClass::isInstance)
                .count();
    }

    /**
     *
     * @param descendant The descendant to check
     * @return true if this GameObject has the descendant, false otherwise
     */
    public final boolean hasDescendant(Self descendant, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(descendant);
        Objects.requireNonNull(graph);

        return graph.descendantsOf(self()).contains(descendant);
    }

    /**
     * Checks how many descendants of the given class this GameObject has.
     * @param descendantClass The class of the descendants to check
     * @return The number of descendants of the given class this GameObject has
     */
    public final boolean hasDescendant(Class<? extends Self> descendantClass, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(descendantClass);
        Objects.requireNonNull(graph);

        return graph.descendantsOf(self()).stream().anyMatch(descendantClass::isInstance);
    }

    /**
     * Checks how many descendants of the given class this GameObject has.
     * @param descendantClass The class of the descendants to check
     * @return The number of descendants of the given class this GameObject has
     */
    public final long numberOfDescendants(Class<? extends Self> descendantClass, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(descendantClass);
        Objects.requireNonNull(graph);

        return graph.descendantsOf(self()).stream().filter(descendantClass::isInstance).count();
    }

    /**
     *
     * @param ancestor The ancestor to check
     * @return true if this GameObject has the ancestor, false otherwise
     */
    public final boolean hasAncestor(Self ancestor, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(ancestor);
        Objects.requireNonNull(graph);

        return graph.ancestorsOf(self()).contains(ancestor);
    }

    /**
     * Checks if this GameObject has an ancestor of the given class.
     * @param ancestorClass The class of the ancestor to check
     * @return true if this GameObject has an ancestor of the given class, false otherwise
     */
    public final boolean hasAncestor(Class<? extends Self> ancestorClass, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(ancestorClass);
        Objects.requireNonNull(graph);

        return graph.ancestorsOf(self()).stream().anyMatch(ancestorClass::isInstance);
    }

    /**
     * Checks how many ancestors of the given class this GameObject has.
     * @param ancestorClass The class of the ancestors to check
     * @return The number of ancestors of the given class this GameObject has
     */
    public final long numberOfAncestors(Class<? extends Vertex<?>> ancestorClass, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(ancestorClass);
        Objects.requireNonNull(graph);

        return graph.ancestorsOf(self()).stream().filter(ancestorClass::isInstance).count();
    }

    /**
     *
     * @return true if this GameObject has children, false otherwise
     */
    public final boolean hasChildren(Graph<? super Self, ApplicationEdge> graph) {
        return !graph.egressEdgesOf(self()).isEmpty();
    }

    /**
     *
     * @return true if this GameObject has parents, false otherwise
     */
    public final boolean hasParents(Graph<? super Self, ApplicationEdge> graph) {
        return !graph.ingressEdgesOf(self()).isEmpty();
    }

    protected Set<BiConsumer<Self, Graph<?, ?>>> enterChildCallbacks() {
        return callbackManager.getCallbacksFor(VertexCallbackType.ON_CONNECT_CHILD);
    }

    protected Set<BiConsumer<Self, Graph<?, ?>>> enterParentCallbacks() {
        return callbackManager.getCallbacksFor(VertexCallbackType.ON_CONNECT_PARENT);
    }

    protected Set<BiConsumer<Self, Graph<?, ?>>> leaveChildCallbacks() {
        return callbackManager.getCallbacksFor(VertexCallbackType.ON_DISCONNECT_CHILD);
    }

    protected Set<BiConsumer<Self, Graph<?, ?>>> leaveParentCallbacks() {
        return callbackManager.getCallbacksFor(VertexCallbackType.ON_DISCONNECT_PARENT);
    }

    protected Set<Consumer<Graph<?, ?>>> enterGraphCallbacks() {
        return callbackManager.getCallbacksFor(GraphCallbackType.ON_ENTER_GRAPH);
    }

    protected Set<Consumer<Graph<?, ?>>> leaveGraphCallbacks() {
        return callbackManager.getCallbacksFor(GraphCallbackType.ON_LEAVE_GRAPH);
    }

    /**
     * Adds a callback to be called when a tail is added to this GameObject.
     * @param callback A Consumer that takes the tail that was added as a parameter.
     */
    public final void addOnConnectChildCallback(BiConsumer<Self, Graph<?, ?>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, VertexCallbackType.ON_CONNECT_CHILD);
    }

    /**
     * Adds a callback to be called when a head is added to this GameObject.
     * @param callback A Consumer that takes the head that this GameObject was added to as a parameter.
     */
    public final void addOnConnectParentCallback(BiConsumer<Self, Graph<?, ?>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, VertexCallbackType.ON_CONNECT_PARENT);
    }

    /**
     * Adds a callback to be called when a tail is removed from this GameObject.
     * @param callback A Consumer that takes the tail that was removed as a parameter.
     */
    public final void addOnLeaveChildCallback(BiConsumer<Self, Graph<?, ?>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, VertexCallbackType.ON_DISCONNECT_CHILD);
    }

    /**
     * Adds a callback to be called when a head is removed from this GameObject.
     * @param callback A Consumer that takes the head that this GameObject was removed from as a parameter.
     */
    public final void addOnLeaveParentCallback(BiConsumer<Self, Graph<?, ?>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, VertexCallbackType.ON_DISCONNECT_PARENT);
    }

    public final void addOnEnterGraphCallback(Consumer<Graph<?, ?>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, GraphCallbackType.ON_ENTER_GRAPH);
    }

    public final void addOnExitGraphCallback(Consumer<Graph<?, ?>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, GraphCallbackType.ON_LEAVE_GRAPH);
    }

    /**
     * Determines if a child node can be connected to the current node in the specified graph.
     * This vertex and the child vertex should be present in the graph
     * The policies of this node and the child node are checked to determine if the operation could be performed.
     *
     * @param child The child node to check.
     * @return A {@link Result} indicating whether the edge can be created, and if not, the rejection reason.
     */
    public final Result<Void, Vertex.ShouldConnectChildFailure> shouldConnectChild(Self child, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        if (child == this)
            return fail(new FailureResults.SelfReference("Cannot add itself as a child"));

        return switch (graph.shouldAddEdge(self(), child)) {
            case Result.Failure<Void, Graph.ShouldAddEdgeFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Result.Success<Void, Graph.ShouldAddEdgeFailure> _ -> success();
        };
    }

    /**
     * Determines if a parent node can be connected to the current node in the specified graph.
     * This vertex and the parent vertex should be present in the graph.
     * The policies of this node and the parent node are checked to determine if the operation could be performed.
     *
     * @param parent The parent node to check.
     * @return A {@link Result} indicating whether the edge can be created, and if not, the rejection reason.
     * @throws NullPointerException if the parent is null.
     */
    public final Result<Void, Vertex.ShouldConnectParentFailure> shouldConnectParent(Self parent, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        if (parent == this)
            return fail(new FailureResults.SelfReference("Cannot add itself as a child"));

        return switch (graph.shouldAddEdge(parent, self())) {
            case Result.Failure<Void, Graph.ShouldAddEdgeFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Result.Success<Void, Graph.ShouldAddEdgeFailure> _ -> success();
        };
    }

    /**
     * Determines if a child node can be disconnected from the current node.
     * The child node must be present in the graph and have the same graph as the current node.
     * The policies of the current node and the child node are checked to determine if the edge can be removed.
     *
     * @param child The child node to check.
     * @return A {@link Result} indicating whether the edge can be removed, and if not, the reason for rejection.
     * @throws NullPointerException if the child is null.
     */
    public final Result<Void, Vertex.ShouldDisconnectChildFailure> shouldDisconnectChild(Self child, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        if (child == this)
            return fail(new FailureResults.SelfReference("Cannot disconnect itself"));

        return switch (graph.shouldRemoveEdge(self(), child)) {
            case Result.Failure<Void, Graph.ShouldRemoveEdgeFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Result.Success<Void, Graph.ShouldRemoveEdgeFailure> _ -> success();
        };
    }

    /**
     * Determines if a parent node can be disconnected from the current node.
     * The parent node must be present in the graph and have the same graph as the current node.
     * The policies of the current node and the parent node are checked to determine if the edge can be removed.
     *
     * @param parent The parent node to check.
     * @return A {@link Result} indicating whether the edge can be removed, and if not, the reason for rejection.
     */
    public final Result<Void, Vertex.ShouldDisconnectParentFailure> shouldDisconnectParent(Self parent, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        if (parent == this)
            return fail(new FailureResults.SelfReference("Cannot remove itself"));

        return switch (graph.shouldRemoveEdge(parent, self())) {
            case Result.Failure<Void, Graph.ShouldRemoveEdgeFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Result.Success<Void, Graph.ShouldRemoveEdgeFailure> _ -> success();
        };
    }

    /**
     * This method is used to determine if a child node should be added to the current node.
     * It first checks if the child node is null, and if it is, it throws a NullPointerException.
     * Then, it checks if the child node can be attached to the current node by calling the shouldAttachChild method.
     * If the child node is not present in the graph, it returns an Absent Result.
     * If the child node cannot be attached to the current node due to a failure in the shouldAttachChild method, it returns a Failure Result.
     * If the child node is present in the graph and can be attached to the current node, it returns a Present Result containing the child node.
     *
     * @param child The child node to be added.
     * @return A Result indicating whether the child node should be added. This can be:
     *         - An Absent Result if the child node is not present in the graph.
     *         - A Failure Result if the child node cannot be attached to the current node.
     *         - A Present Result containing the child node if the child node is present in the graph and can be attached to the current node.
     * @throws NullPointerException if the child node is null.
     */
    public final Result<Void, Vertex.ShouldAddChildFailure> shouldAddChild(Self child, Graph<? super Self, ApplicationEdge> graph)
            throws NullPointerException ,GraphOperationException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (shouldConnectChild(child, graph)) {
            case Result.Failure<?, Vertex.ShouldConnectChildFailure>(Vertex.ShouldConnectChildFailure cause) -> switch (cause) {
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.SelfReference selfReference -> fail(selfReference);
                case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                case FailureResults.VertexNotPresent _ -> switch (graph.shouldAddVertex(child)) {
                    case Result.Failure<Void, Graph.ShouldAddVertexFailure>(var f) -> switch (f) {
                        case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                        case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                        case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                        case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                        case FailureResults.VertexAlreadyPresent _ ->
                                throw new GraphOperationException("The vertex is already in the graph. Graph is inconsistent");
                    };
                    case Result.Success<Void, Graph.ShouldAddVertexFailure> _ -> success();
                };
            };
            case Result.Success<Void, ?> _ -> fail(new FailureResults.VertexAlreadyPresent("The vertex is already in the graph"));
        };
    }

    /**
     * Determines if a parent node should be added as a parent to the current node.
     * If the parent has no graph, the addition is allowed. A result of {@link Result.Success} will be returned.
     * Checks the policies of the current node and the parent node to determine if the parent can be added.
     *
     * @param parent The parent node to be added.
     * @return A {@link Result} indicating whether the parent should be added, and if not, the reason for rejection.
     * @throws NullPointerException if the parent is null.
     */
    public final Result<Void, Vertex.ShouldAddParentFailure> shouldAddParent(Self parent, Graph<? super Self, ApplicationEdge> graph)
            throws NullPointerException, GraphOperationException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return switch (shouldConnectParent(parent, graph)) {
            case Result.Failure<?, Vertex.ShouldConnectParentFailure>(Vertex.ShouldConnectParentFailure cause) -> switch (cause) {
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.SelfReference selfReference -> fail(selfReference);
                case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);

                case FailureResults.VertexNotPresent _ -> switch (graph.shouldAddVertex(parent)) {
                    case Result.Failure<Void, Graph.ShouldAddVertexFailure>(var f) -> switch (f) {
                        case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                        case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                        case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                        case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                        case FailureResults.VertexAlreadyPresent _ ->
                                throw new GraphOperationException("The vertex is already in the graph. Graph is inconsistent");
                    };
                    case Result.Success<Void, Graph.ShouldAddVertexFailure> _ -> success();
                };
            };

            case Result.Success<Void, ?> _ -> fail(new FailureResults.VertexAlreadyPresent("The vertex is already in the graph"));
        };
    }

    /// Determines if a child node could be warframe removed from the current node.
    /// If the child has no graph, the removal is allowed. A result of [Result.Success] will be returned.
    /// Checks the policies of the current node and the child node to determine if the child can be removed.
    ///
    /// @param child The child node to be removed.
    /// @return A [Result] indicating whether the child should be removed, and if not, the reason for rejection.
    /// @throws NullPointerException if the child is null.
    public final Result<Void, Vertex.ShouldRemoveChildFailure> shouldRemoveChild(Self child, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        if (child == this)
            return fail(new FailureResults.SelfReference("Cannot remove itself"));

        if (!graph.containsVertex(self()))
            return fail(new FailureResults.VertexNotPresent("The vertex is not present in the graph", self()));

        if (!graph.containsEdge(self(), child))
            return fail(new FailureResults.EdgeNotPresent("The edge is not present in the graph"));

        // Graph::shouldRemoveVertex checks if the parents and children of the vertex allow the removal and
        // disconnection of the vertex. So, no need to check if the disconnect is allowed here.
        return switch (graph.shouldRemoveVertex(child)) {
            case Result.Failure<Void, Graph.ShouldRemoveVertexFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Result.Success<Void, Graph.ShouldRemoveVertexFailure> _ -> success();
        };

//        return switch (shouldDisconnectChild(child, graph)) {
//            case Failure<?, ShouldDisconnectChildFailure>(Vertex.ShouldDisconnectChildFailure cause) -> switch (cause) {
//                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
//                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
//                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
//                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
//                case VertexNotPresent vertexNotPresentInTheGraph -> fail(vertexNotPresentInTheGraph);
//                case EdgeNotPresentInGraph edgeNotPresentInGraph -> fail(edgeNotPresentInGraph);
//                case SelfReference selfReference -> fail(selfReference);
//            };
//
//            case Success<Void, ?> _ -> switch (graph.shouldRemoveVertex(child)) {
//                case Failure<Void, Graph.ShouldRemoveVertexFailure>(var f) -> switch (f) {
//                    case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
//                    case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
//                    case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
//                    case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
//                    case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
//                };
//                case Result.Success<Void, Graph.ShouldRemoveVertexFailure> _ -> success();
//            };
//        };
    }

    /**
     * Determines if a parent node should be removed as a parent to the current node.
     * If the parent has no graph, the removal is allowed. A result of {@link Result.Success} will be returned.
     * Checks the policies of the current node and the parent node to determine if the parent can be removed.
     *
     * @param parent The parent node to be removed.
     * @return A {@link Result} indicating whether the parent should be removed, and if not, the reason for rejection.
     */
    private Result<Void, Vertex.ShouldRemoveParentFailure> shouldRemoveParent(Self parent, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        if (parent == this)
            return fail(new FailureResults.SelfReference("Cannot remove itself"));

        if (!graph.containsVertex(self()))
            return fail(new FailureResults.VertexNotPresent("The vertex is not present in the graph", self()));

        if (!graph.containsEdge(parent, self()))
            return fail(new FailureResults.EdgeNotPresent("The edge is not present in the graph"));

        // Graph::shouldRemoveVertex checks if the parents and children of the vertex allow the removal and
        // disconnection of the vertex. So, no need to check if the disconnect is allowed here.
        return switch (graph.shouldRemoveVertex(parent)) {
            case Result.Failure<Void, Graph.ShouldRemoveVertexFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Result.Success<Void, Graph.ShouldRemoveVertexFailure> _ -> success();
        };
    }

    /// Connects a child to the current node if possible.
    ///
    /// Performs the following steps:
    /// - Checks if the child can be connected to the current node.
    /// - Calls the graph to add the edge connecting the child to the current node.
    /// - Calls the post-connection process for the child.
    ///
    /// @param child The child node to connect.
    /// @param weight The weight of the edge connecting the child to the current node.
    /// @param graph The graph in which the operation is performed.
    /// @return A [Result] indicating whether the child was connected, and if not, the reason for failure.
    /// @throws NullPointerException if the child or the graph is null.
    /// @throws UnsupportedOperationException if the operation is not supported or the graph could not create the edge.
    /// @throws GraphOperationException if an exceptional state is encountered during the operation.
    private Result<ApplicationEdge, Vertex.ChildConnectionFailure> handleChildConnection(Self child, double weight, Graph<? super Self, ApplicationEdge> graph)
            throws NullPointerException, UnsupportedOperationException, GraphOperationException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (graph.addEdge(self(), child, weight)) {
            case Result.Failure<ApplicationEdge, Graph.EdgeAdditionFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
            };

            case Result.Success<ApplicationEdge, Graph.EdgeAdditionFailure>(var e) -> {
                if (e == null)
                    yield  fail(new FailureResults.EdgeAlreadyExists("The vertex is already a child of this vertex"));

                postConnectChildProcess(child, graph);
                yield success(e);
            }
        };
    }

    /// Processes the post-connection callbacks for the child node.
    private void postConnectChildProcess(Self child, Graph<?, ApplicationEdge> graph) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> {
                // Call Vertex::onConnectChild callback if the policy permits it
                switch (policiesManager().stateOf(ON_CONNECT_CHILD_POLICY, child)) {
                    case ACCEPT -> this.onConnectChild(child, graph);
                    case REJECT, UNSET -> {}
                }
                return null;
            });

            scope.fork(() -> {
                // Call Vertex::onConnectParent callback on the child if the policy permits it
                switch (child.policiesManager().stateOf(ON_CONNECT_PARENT_POLICY, self())) {
                    case ACCEPT -> child.onConnectParent(self(), graph);
                    case REJECT, UNSET -> {}
                }
                return null;
            });

            scope.join();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /// Connects a parent to the current node if possible.
    ///
    /// Performs the following steps:
    /// - Checks if the parent can be connected to the current node.
    /// - Calls the graph to add the edge connecting the parent to the current node.
    /// - Calls the post-connection process for the parent.
    ///
    /// @param parent The parent node to connect.
    /// @param weight The weight of the edge connecting the parent to the current node.
    /// @param graph The graph in which the operation is performed.
    /// @return A [Result] indicating whether the parent was connected, and if not, the reason for failure.
    /// @throws NullPointerException if the parent or the graph is null.
    /// @throws UnsupportedOperationException if the operation is not supported or the graph could not create the edge.
    /// @throws GraphOperationException if an exceptional state is encountered during the operation.
    private Result<ApplicationEdge, Vertex.ParentConnectionFailure> handleParentConnection(Self parent, double weight, Graph<? super Self, ApplicationEdge> graph)
            throws NullPointerException, UnsupportedOperationException, GraphOperationException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return switch (graph.addEdge(parent, self(), weight)) {
            case Result.Failure<ApplicationEdge, Graph.EdgeAdditionFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
            };

            case Result.Success<ApplicationEdge, Graph.EdgeAdditionFailure>(var e) -> {
                if (e == null)
                    yield  fail(new FailureResults.EdgeAlreadyExists("The vertex is already a child of this vertex"));

                postConnectParentProcess(parent, graph);
                yield success(e);
            }
        };
    }

    private void postConnectParentProcess(Self parent, Graph<? super Self, ApplicationEdge> graph) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> {
                // Call Vertex::onConnectChild callback on the parent if the policy permits it
                switch (parent.policiesManager().stateOf(ON_CONNECT_CHILD_POLICY, self())) {
                    case ACCEPT -> this.onConnectChild(self(), graph);
                    case REJECT, UNSET -> {}
                }
                return null;
            });

            scope.fork(() -> {
                // Call Vertex::onConnectParent callback if the policy permits it
                switch (policiesManager().stateOf(ON_CONNECT_PARENT_POLICY, parent)) {
                    case ACCEPT -> onConnectParent(parent, graph);
                    case REJECT, UNSET -> {}
                }
                return null;
            });

            scope.join();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /// Adds a child to the current node if possible.
    /// Performs the following steps:
    /// - Checks if the child can be added to the current node.
    /// - Checks if the child can be connected to the current node.
    /// - Calls the graph to add the child to the graph.
    /// - Calls the graph to add the edge connecting the child to the current node.
    /// - Calls the post-connection process for the child.
    ///
    /// @param child The child node to add.
    /// @param weight The weight of the edge connecting the child to the current node.
    /// @param graph The graph in which the operation is performed.
    /// @return A [Result] indicating whether the child was added, and if not, the reason for failure.
    /// @throws NullPointerException if the child or the graph is null.
    /// @throws UnsupportedOperationException if the operation is not supported or the graph could not create the edge.
    private Result<Self, Vertex.ChildAdditionFailure> handleChildAddition(Self child, double weight, Graph<Self, ApplicationEdge> graph)
            throws NullPointerException, GraphCycleProhibitedException, UnsupportedOperationException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (graph.addVertexValidated(child)) {
            case Result.Success<Self, Graph.VertexAdditionFailure>(Self v) -> {
                if (v == null)
                    throw new GraphOperationException("The vertex is already in the graph. Graph is inconsistent"); // TODO: Improve error message

                yield switch (graph.executeValidatedEdgeAdditionWithoutCallbacks(self(), child, weight)) {
                    case Result.Failure<ApplicationEdge, Graph.EdgeAdditionFailure>(var eaf) -> {
                        graph.removeVertex(child);
                        yield switch (eaf) {
                            case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                            case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                            case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                            case FailureResults.RejectedByVertexValidation rejectedByVertexValidation ->
                                    fail(rejectedByVertexValidation);
                            case FailureResults.VertexNotPresent _ ->
                                    throw new GraphOperationException("The vertex is not present in the graph. Graph is inconsistent");
                            case FailureResults.EdgeAlreadyExists _ ->
                                    throw new GraphOperationException("The edge already exists. Graph is inconsistent");
                            case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                        };
                    }

                    case Result.Success<ApplicationEdge, Graph.EdgeAdditionFailure> _ -> {
                        graph.processVertexAdditionCallbacks(child);
                        graph.processEdgeAdditionCallbacks(self(), child);
                        postConnectChildProcess(child, graph);
                        yield success(child);
                    }
                };
            }

            case Result.Failure<Self, Graph.VertexAdditionFailure>(var vaf) -> switch (vaf) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexAlreadyPresent _ -> switch (handleChildConnection(child, weight, graph)) {
                    case Result.Failure<ApplicationEdge, Vertex.ChildConnectionFailure>(var ccf) -> switch (ccf) {
                        case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                        case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                        case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                        case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                        case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                        case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                        case FailureResults.SelfReference selfReference -> fail(selfReference);
                        case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                    };
                    case Result.Success<ApplicationEdge, Vertex.ChildConnectionFailure> _ -> success();
                };
            };
        };
    }

    /// Adds a parent to the current node if possible.
    /// Performs the following steps:
    /// - Checks if the parent can be added to the current node.
    /// - Checks if the parent can be connected to the current node.
    /// - Calls the graph to add the parent to the graph.
    /// - Calls the graph to add the edge connecting the parent to the current node.
    /// - Calls the post-connection process for the parent.
    ///
    /// @param parent The parent node to add.
    /// @param weight The weight of the edge connecting the parent to the current node.
    /// @param graph The graph in which the operation is performed.
    /// @return A [Result] indicating whether the parent was added, and if not, the reason for failure.
    /// @throws NullPointerException if the parent or the graph is null.
    /// @throws GraphCycleProhibitedException if a cycle is detected in the graph.
    /// @throws UnsupportedOperationException if the operation is not supported or the graph could not create the edge.
    private Result<Self, Vertex.ParentAdditionFailure> handleParentAddition(Self parent, double weight, Graph<Self, ApplicationEdge> graph)
            throws NullPointerException, GraphCycleProhibitedException, UnsupportedOperationException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return switch (graph.addVertexValidated(parent)) {
            case Result.Success<Self, Graph.VertexAdditionFailure>(Self v) -> {
                if (v == null)
                    throw new GraphOperationException("The vertex is already in the graph. Graph is inconsistent"); // TODO: Improve error message

                yield switch (graph.executeValidatedEdgeAdditionWithoutCallbacks(parent, self(), weight)) {
                    case Result.Failure<ApplicationEdge, Graph.EdgeAdditionFailure>(var eaf) -> {
                        graph.removeVertex(parent);
                        yield switch (eaf) {
                            case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                            case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                            case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                            case FailureResults.RejectedByVertexValidation rejectedByVertexValidation ->
                                    fail(rejectedByVertexValidation);
                            case FailureResults.VertexNotPresent _ ->
                                    throw new GraphOperationException("The vertex is not present in the graph. Graph is inconsistent");
                            case FailureResults.EdgeAlreadyExists _ ->
                                    throw new GraphOperationException("The edge already exists. Graph is inconsistent");
                            case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                        };
                    }

                    case Result.Success<ApplicationEdge, Graph.EdgeAdditionFailure> _ -> {
                        graph.processVertexAdditionCallbacks(parent);
                        graph.processEdgeAdditionCallbacks(parent, self());
                        postConnectParentProcess(parent, graph);
                        yield success(parent);
                    }
                };
            }

            case Result.Failure<Self, Graph.VertexAdditionFailure>(var vaf) -> switch (vaf) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexAlreadyPresent _ -> switch (handleParentConnection(parent, weight, graph)) {
                    case Result.Failure<ApplicationEdge, Vertex.ParentConnectionFailure>(var pcf) -> switch (pcf) {
                        case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                        case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                        case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                        case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                        case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                        case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                        case FailureResults.SelfReference selfReference -> fail(selfReference);
                        case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                    };
                    case Result.Success<ApplicationEdge, Vertex.ParentConnectionFailure> _ -> success();
                };
            };
        };
    }

    /// @param child
    /// @param graph
    /// @return
    /// @throws NullPointerException
    /// @throws UnsupportedOperationException
    /// @throws GraphOperationException
    protected Result<ApplicationEdge, Vertex.ChildDisconnectionFailure> handleChildDisconnection(Self child, Graph<? super Self, ApplicationEdge> graph)
            throws NullPointerException, UnsupportedOperationException, GraphOperationException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (graph.removeEdge(self(), child)) {
            case Result.Failure<ApplicationEdge, Graph.EdgeRemovalFailure>(var erf) -> switch (erf) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case FailureResults.EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
            };

            case Result.Success<ApplicationEdge, Graph.EdgeRemovalFailure>(var e) -> {
                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    scope.fork(() -> {
                        switch (policiesManager.stateOf(ON_DISCONNECT_CHILD_POLICY, child)) {
                            case ACCEPT -> this.onDisconnectChild(child, graph);
                            case UNSET -> {
                                if (acceptOnUnsetPolicy())
                                    this.onDisconnectChild(child, graph);
                            }
                            case REJECT -> {}
                        }
                        return null;
                    });

                    scope.fork(() -> {
                        switch (child.policiesManager.stateOf(ON_DISCONNECT_PARENT_POLICY, this)) {
                            case ACCEPT -> child.onDisconnectParent(self(), graph);
                            case UNSET -> {
                                if (child.acceptOnUnsetPolicy())
                                    child.onDisconnectParent(self(), graph);
                            }
                            case REJECT -> {}
                        }
                        return null;
                    });
                    scope.join();

                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                yield success(e);
            }
        };
    }

    /// @param parent
    /// @param graph
    /// @return
    /// @throws IllegalStateException
    protected Result<ApplicationEdge, Vertex.ParentDisconnectionFailure> handleParentDisconnection(Self parent, Graph<? super Self, ApplicationEdge> graph)
            throws IllegalStateException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return switch (graph.removeEdge(parent, self())) {
            case Result.Failure<ApplicationEdge, Graph.EdgeRemovalFailure>(var erf) -> switch (erf) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case FailureResults.EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
            };

            case Result.Success<ApplicationEdge, Graph.EdgeRemovalFailure>(var e) -> {
                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    scope.fork(() -> {
                        switch (parent.policiesManager().stateOf(ON_DISCONNECT_CHILD_POLICY, this)) {
                            case ACCEPT -> parent.onDisconnectChild(self(), graph);
                            case UNSET -> {
                                if (parent.acceptOnUnsetPolicy())
                                    parent.onDisconnectChild(self(), graph);
                            }
                            case REJECT -> {}
                        }
                        return null;
                    });

                    scope.fork(() -> {
                        switch (policiesManager.stateOf(ON_DISCONNECT_PARENT_POLICY, parent)) {
                            case ACCEPT -> onDisconnectParent(parent, graph);
                            case UNSET -> {
                                if (acceptOnUnsetPolicy())
                                    onDisconnectParent(parent, graph);
                            }
                            case REJECT -> {}
                        }
                        return null;
                    });
                    scope.join();

                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                yield success(e);
            }
        };
    }

    /// @param child
    /// @param graph
    /// @return
    /// @throws IllegalStateException
    /// @throws InterruptedException
    @SuppressWarnings("unchecked")
    protected <T extends Vertex<T>> Result<Self, Vertex.ChildRemovalFailure> handleChildRemoval(Self child, Graph<? super Self, ApplicationEdge> graph)
            throws IllegalStateException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        if (!this.hasChild(child, graph))
            return fail(new FailureResults.EdgeNotPresent("The edge is not present in the graph"));

        switch (graph.removeVertex(child)) {
            case Result.Failure<?, Graph.VertexRemovalFailure>(var vrf) -> {
                return switch (vrf) {
                    case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                    case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                    case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                    case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                    case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                };
            }
            case Result.Success<? super Self, Graph.VertexRemovalFailure>(var v) -> {
                return success((Self) v);
            }
        }
    }

    /// @param parent
    /// @param graph
    /// @return
    /// @throws InterruptedException
    /// @throws IllegalStateException
    @SuppressWarnings("unchecked")
    private Result<Self, Vertex.ParentRemovalFailure> handleParentRemoval(Self parent, Graph<? super Self, ApplicationEdge> graph)
            throws IllegalStateException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        if (!graph.containsVertex(parent))
            return fail(new FailureResults.VertexNotPresent("The vertex is not present in the graph", parent));

        if (!this.hasParent(parent, graph))
            return fail(new FailureResults.EdgeNotPresent("The edge is not present in the graph"));

        switch (graph.removeVertex(parent)) {
            case Result.Failure<?, Graph.VertexRemovalFailure>(var vrf) -> {
                return switch (vrf) {
                    case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                    case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                    case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                    case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                    case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                };
            }
            case Result.Success<? super Self, Graph.VertexRemovalFailure>(var v) -> {
                return success((Self) v);
            }
        }
    }

    /// Creates an edge with the given weight in the graph with the current node as the source and the given node as the
    /// target.
    ///
    /// To create the edge, the graph must contain both the source and the target nodes.
    /// Also, the policies of the source and the target nodes are checked to determine if the edge can be created.
    ///
    /// If the edge is created successfully, the corresponding callbacks are executed.
    ///
    /// @param child The child node to disconnect.
    /// @param weight The weight of the edge connecting the current node to the child.
    /// @param graph The graph in which the operation is performed.
    /// @return A [Result] indicating the outcome of the connection attempt.
    /// If the connection is successful, the edge is returned enclosed in a [Result.Success] instance.
    /// If the connection fails, the reason for the failure is returned enclosed in a [Result.Failure] instance.
    public final Result<ApplicationEdge, Vertex.ChildConnectionFailure> connectChild(Self child, double weight, Graph<? super Self, ApplicationEdge> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (handleChildConnection(child, weight, graph)) {
            case Result.Failure<ApplicationEdge, Vertex.ChildConnectionFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                case FailureResults.SelfReference selfReference -> fail(selfReference);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
            };
            case Result.Success<ApplicationEdge, Vertex.ChildConnectionFailure>(var e) -> success(e);
        };
    }

    /// Creates an edge with the default weight in the graph with the current node as the source and the given node as
    /// the target.
    ///
    /// To create the edge, the graph must contain both the source and the target nodes.
    /// Also, the policies of the source and the target nodes are checked to determine if the edge can be created.
    ///
    /// If the edge is created successfully, the corresponding callbacks are executed.
    ///
    /// @param child The child node to connect.
    /// @param graph The graph in which the operation is performed.
    /// @return A [Result] indicating the outcome of the connection attempt.
    /// If the connection is successful, the edge is returned enclosed in a [Result.Success] instance.
    /// If the connection fails, the reason for the failure is returned enclosed in a [Result.Failure] instance.
    public final Result<ApplicationEdge, Vertex.ChildConnectionFailure> connectChild(Self child, Graph<? super Self, ApplicationEdge> graph) {
        return connectChild(child, 1.0, graph);
    }

    /// Adds the given vertex to the given graph and connects it as a child to this vertex.
    ///
    /// If the child is successfully added and connected, the corresponding callbacks are executed.
    ///
    /// If the vertex was already present in the graph, the edge is created and the corresponding callbacks are
    /// executed.
    ///
    /// @param child The child to add.
    /// @param weight The weight of the edge connecting the child to the current node.
    /// @param graph The graph in which the operation is performed.
    /// @return A [Result] indicating the outcome of the operation.
    /// If the operation is successful, the child is returned enclosed in a [Result.Success] instance.
    /// If the operation fails, the reason for the failure is returned enclosed in a [Result.Failure] instance.
    /// @throws NullPointerException if the child or the graph is null.
    public final Result<Self, Vertex.ChildAdditionFailure> addChild(Self child, double weight, Graph<Self, ApplicationEdge> graph)
            throws NullPointerException {
        return switch (handleChildAddition(child, weight, graph)) {
            case Result.Failure<Self, Vertex.ChildAdditionFailure> f -> f;
            case Result.Success<Self, ?>(var c) -> success(c);
        };
    }

    /// Adds the given vertex to the given graph and connects it as a child to this vertex.
    ///
    /// The weight of the edge connecting the child to the current node is set to 1.0.
    ///
    /// @param child The child to add.
    /// @param graph The graph in which the operation is performed.
    /// @return A [Result] indicating the outcome of the operation.
    /// If the operation is successful, the child is returned enclosed in a [Result.Success] instance.
    /// If the operation fails, the reason for the failure is returned enclosed in a [Result.Failure] instance.
    /// @throws NullPointerException if the child or the graph is null.
    public final Result<Self, Vertex.ChildAdditionFailure> addChild(Self child, Graph<Self, ApplicationEdge> graph)
            throws IllegalArgumentException {
        return addChild(child, 1.0, graph);
    }

    /// Connects a parent node to the current node. The parent node must be present in the graph.
    ///
    /// Policies are checked to determine if the parent can be connected.
    ///
    /// If connected successfully, callbacks are executed.
    ///
    /// @param parent The child to connect.
    /// @return A [Result.Success] with the edge connecting the parent to the current node if the operation was successful, an
    /// empty [Result] if nothing was connected, or a [Result.Failure] with the reason for the failure.
    public final Result<ApplicationEdge, Vertex.ParentConnectionFailure> connectParent(Self parent, double weight, Graph<? super Self, ApplicationEdge> graph) {
        return handleParentConnection(parent, weight, graph);
    }

    public final Result<ApplicationEdge, Vertex.ParentConnectionFailure> connectParent(Self parent, Graph<? super Self, ApplicationEdge> graph) {
        return connectParent(parent, 1.0, graph);
    }

    public final Result<Self, Vertex.ParentAdditionFailure> addParent(Self parent, double weight, Graph<Self, ApplicationEdge> graph)
            throws NullPointerException {
        return handleParentAddition(parent, weight, graph);
    }

    public final Result<Self, Vertex.ParentAdditionFailure> addParent(Self parent, Graph<Self, ApplicationEdge> graph) throws IllegalArgumentException {
        return addParent(parent, 1.0, graph);
    }

    public final Result<ApplicationEdge, Vertex.ChildDisconnectionFailure> disconnectChild(Self child, Graph<? super Self, ApplicationEdge> graph) {
        return handleChildDisconnection(child, graph);
    }

    public final Result<Self, Vertex.ChildRemovalFailure> removeChild(Self child, Graph<Self, ApplicationEdge> graph) {
        return handleChildRemoval(child, graph);
    }

    public final Result<ApplicationEdge, Vertex.ParentDisconnectionFailure> disconnectParent(Self parent, Graph<? super Self, ApplicationEdge> graph) {
        return handleParentDisconnection(parent, graph);
    }

    public final Result<Self, Set<Vertex.ChildDisconnectionFailure>> disconnectChildren(Graph<Self, ApplicationEdge> graph) {
        Set<Vertex.ChildDisconnectionFailure> failures = new HashSet<>();
        for (Self child : graph.childrenOf(self()))
            disconnectChild(child, graph).ifFailure(failures::add);

        if (failures.isEmpty())
            return success();

        return fail(failures);
    }

    public final Result<Void, Set<Vertex.ChildDisconnectionFailure>> disconnectChildren(Graph<Self, ApplicationEdge> graph, Predicate<Self> predicate) {
        Set<Vertex.ChildDisconnectionFailure> failures = new HashSet<>();
        Set<Self> pending = new HashSet<>();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (Self child : graph.childrenOf(self())) {
                scope.fork(() -> {
                    if (predicate.test(child))
                        switch (shouldDisconnectChild(child, graph)) {
                            case Result.Failure<Void, Vertex.ShouldDisconnectChildFailure>(
                                    Vertex.ShouldDisconnectChildFailure f) -> {
                                switch (f) {
                                    case FailureResults.EdgeNotPresent _, FailureResults.VertexNotPresent _ -> {}
                                    default -> failures.add(switch (f) {
                                        case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> rejectedByGraphPolicy;
                                        case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> rejectedByGraphValidation;
                                        case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> rejectedByVertexPolicy;
                                        case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> rejectedByVertexValidation;
                                        case FailureResults.SelfReference selfReference -> selfReference;
                                        default -> throw new IllegalStateException("Unexpected value: " + f);
                                    });
                                }
                            }
                            case Result.Success<Void, Vertex.ShouldDisconnectChildFailure> _ -> pending.add(child);
                        }
                    return null;
                });
            }
            scope.join();

            if (!failures.isEmpty())
                return fail(failures);

            for (Self child : pending) {
                scope.fork(() -> { // TODO: Check all edges removed?
                    graph.removeEdge(self(), child);
                    graph.processEdgeRemovalCallbacks(self(), child);
                    return null;
                });
            }
            scope.join();

            return success();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public final Result<Void, Set<Vertex.ParentDisconnectionFailure>> disconnectParents(Graph<Self, ApplicationEdge> graph) {
        return disconnectParents(graph, _ -> true);
    }

    /// Tries to disconnect all the parents of the current node that satisfy the given predicate.
    /// If any of the disconnections fail, a
    public final Result<Void, Set<Vertex.ParentDisconnectionFailure>> disconnectParents(Graph<Self, ApplicationEdge> graph, Predicate<Self> predicate) {
        Set<Vertex.ParentDisconnectionFailure> failures = new HashSet<>();
        Set<Self> pending = new HashSet<>();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (Self parent : graph.parentsOf(self())) {
                scope.fork(() -> {
                    if (predicate.test(parent))
                        switch (shouldDisconnectParent(parent, graph)) {
                            case Result.Failure<Void, Vertex.ShouldDisconnectParentFailure>(
                                    Vertex.ShouldDisconnectParentFailure f) -> {
                                switch (f) {
                                    case FailureResults.EdgeNotPresent _, FailureResults.VertexNotPresent _ -> {}
                                    default -> failures.add(switch (f) {
                                        case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> rejectedByGraphPolicy;
                                        case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> rejectedByGraphValidation;
                                        case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> rejectedByVertexPolicy;
                                        case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> rejectedByVertexValidation;
                                        case FailureResults.SelfReference selfReference -> selfReference;
                                        default -> throw new IllegalStateException("Unexpected value: " + f);
                                    });
                                }
                            }
                            case Result.Success<Void, Vertex.ShouldDisconnectParentFailure> _ -> pending.add(parent);
                        }
                    return null;
                });
            }
            scope.join();

            if (!failures.isEmpty())
                return fail(failures);

            for (Self parent : pending) {
                scope.fork(() -> { // TODO: Check all edges removed?
                    graph.removeEdge(parent, self());
                    graph.processEdgeRemovalCallbacks(parent, self());
                    return null;
                });
            }
            scope.join();

            return success();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public final Result<Self, Vertex.ParentRemovalFailure> removeParent(Self parent, Graph<Self, ApplicationEdge> graph) {
        return handleParentRemoval(parent, graph);
    }

    @SuppressWarnings("unchecked")
    private Result<Self, Vertex.RemoveFailure> handleRemoval(Graph<? super Self, ApplicationEdge> graph) {
        return switch (graph.removeVertex(self())) {
            case Result.Failure<?, Graph.VertexRemovalFailure>(var vrf) -> switch (vrf) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Result.Success<? super Self, Graph.VertexRemovalFailure>(var v) -> success((Self) v);
        };
    }

    /**
     * This method is used to remove the current node from the graph.
     * It first checks if the current node is part of a graph. If it is not, it returns an Absent Result.
     * If the current node is part of a graph, it attempts to remove the node from the graph.
     * If the removal is successful, it returns a Present Result containing the removed node.
     * If the removal is not successful, it returns an Absent Result with a message indicating that the vertex is not present in a graph.
     * If an InterruptedException is thrown during the removal process, it is caught and a RuntimeException is thrown.
     *
     * @return A Result indicating the outcome of the removal attempt. This can be:
     *         - A Present Result containing the removed node if the operation was successful.
     *         - An Absent Result if the node was not part of a graph or if the removal was not successful.
     *         - A Failure Result with a GraphModificationException if there was an issue with the removal.
     * @throws RuntimeException if an InterruptedException is thrown during the removal process.
     */
    protected final Result<Self, Vertex.RemoveFailure> removeFromGraph(Graph<? super Self, ApplicationEdge> graph) {
        return handleRemoval(graph);
    }

    public boolean shouldCallOnEnterGraphFor(Graph<?, ?> graph) {
        return policiesManager.stateOf(ON_ENTER_GRAPH_POLICY, graph) == ACCEPT
                || (acceptOnUnsetPolicy() && policiesManager.stateOf(ON_ENTER_GRAPH_POLICY, graph) == UNSET);
    }

    public boolean shouldCallOnLeaveGraphFor(Graph<?, ?> graph) {
        return policiesManager.stateOf(ON_LEAVE_GRAPH_POLICY, graph) == ACCEPT
                || (acceptOnUnsetPolicy() && policiesManager.stateOf(ON_LEAVE_GRAPH_POLICY, graph) == UNSET);
    }

    public <V2 extends Vertex<V2>> boolean shouldCallOnDisconnectChildFor(V2 v) {
        return switch (policiesManager().stateOf(ON_DISCONNECT_CHILD_POLICY, v)) {
            case REJECT -> false;
            case ACCEPT -> true;
            case UNSET -> acceptOnUnsetPolicy();
        };
    }

    public <V2 extends Vertex<V2>> boolean shouldCallOnDisconnectParentFor(V2 v) {
        return switch (policiesManager().stateOf(ON_DISCONNECT_PARENT_POLICY, v)) {
            case REJECT -> false;
            case ACCEPT -> true;
            case UNSET -> acceptOnUnsetPolicy();
        };
    }

    public <V2 extends Vertex<V2>> boolean shouldCallOnConnectChildFor(V2 v) {
        return switch (policiesManager().stateOf(ON_CONNECT_CHILD_POLICY, v)) {
            case REJECT -> false;
            case ACCEPT -> true;
            case UNSET -> acceptOnUnsetPolicy();
        };
    }

    public <V2 extends Vertex<V2>> boolean shouldCallOnConnectParentFor(V2 v) {
        return switch (policiesManager().stateOf(ON_CONNECT_PARENT_POLICY, v)) {
            case REJECT -> false;
            case ACCEPT -> true;
            case UNSET -> acceptOnUnsetPolicy();
        };
    }

    protected void onConnectChild(Self child, Graph<?, ApplicationEdge> graph) throws RuntimeException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var callback : enterChildCallbacks())
                scope.fork(() -> {
                    callback.accept(child, graph);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onConnectParent(Self parent, Graph<?, ApplicationEdge> graph) throws RuntimeException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var callback : enterParentCallbacks())
                scope.fork(() -> {
                    callback.accept(parent, graph);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onDisconnectChild(Self child, Graph<?, ? extends ApplicationEdge> graph) throws RuntimeException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var callback : leaveChildCallbacks())
                scope.fork(() -> {
                    callback.accept(child, graph);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onDisconnectParent(Self parent, Graph<?, ? extends ApplicationEdge> graph) throws RuntimeException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var callback : leaveParentCallbacks())
                scope.fork(() -> {
                    callback.accept(parent, graph);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected <E extends ApplicationEdge> void onEnterGraph(Graph<?, E> graph) throws RuntimeException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var callback : enterGraphCallbacks())
                scope.fork(() -> {
                    callback.accept(graph);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected <E extends ApplicationEdge> void onLeaveGraph(Graph<?, E> graph) throws RuntimeException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var callback : leaveGraphCallbacks())
                scope.fork(() -> {
                    callback.accept(graph);
                    return null;
                });

            scope.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /// The types of callbacks that can be added to a vertex.
    /// This is just a marker interface to group the callback types.
    public sealed interface CallbackType {}



    /// The types of callbacks that can be added to a vertex.
    public enum VertexCallbackType implements CallbackType {
        ON_CONNECT_CHILD,
        ON_DISCONNECT_CHILD,
        ON_CONNECT_PARENT,
        ON_DISCONNECT_PARENT;
    }


    /// The types of callbacks that can be added to a vertex.
    public enum GraphCallbackType implements CallbackType {
        ON_ENTER_GRAPH,
        ON_LEAVE_GRAPH;
    }

    /**
     * Returns the egress edges of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the egress edges of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> Set<E> egressEdgesIn(Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this))
            return Set.of();

        return graph.egressEdgesOf((Self) this);
    }

    /**
     * Returns the ingress edges of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the ingress edges of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> Set<E> ingressEdgesIn(Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this))
            return Set.of();

        return graph.ingressEdgesOf((Self) this);
    }

    /**
     * Returns the children of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the children of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> Set<Self> childrenIn(Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this))
            return Set.of();

        return graph.childrenOf((Self) this);
    }

    /**
     * Returns the parents of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the parents of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> Set<Self> parentsIn(Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this))
            return Set.of();

        return graph.parentsOf((Self) this);
    }

    /**
     * Returns the neighbors of the vertex on the given graph.
     * The neighbors of a vertex are the vertices that are connected to it.
     * @param graph the graph that contains the vertex.
     * @return the neighbors of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> Set<Self> neighborsIn(Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this))
            return Set.of();

        return graph.neighborsOf((Self) this);
    }

    /**
     * Collects the children, the children of the children, and so on, of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the descendants of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> Set<Self> descendantsIn(Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this))
            return Set.of();

        return graph.descendantsOf((Self) this);
    }

    /**
     * Collects the parents, the parents of the parents, and so on, of the vertex on the given graph.
     * @param graph the graph that contains the vertex.
     * @return the ancestors of the vertex.
     * @param <E> the type of the edges.
     */
    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> Set<Self> ancestorsIn(Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this))
            return Set.of();

        return graph.ancestorsOf((Self) this);
    }


    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> Set<E> edgePathTo(Self target, Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this) || !graph.containsVertex(target))
            return Set.of();

        return new HashSet<>(graph.shortestPathBetween((Self) this, target));
    }

    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> Set<E> edgePathFrom(Self source, Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this) || !graph.containsVertex(source))
            return Set.of();

        return new HashSet<>(graph.shortestPathBetween(source, (Self) this));
    }

    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> SequencedCollection<Self> shortestPathTo(Self target, Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this) || !graph.containsVertex(target))
            return List.of();

        return graph.shortestVertexPathBetween((Self) this, target);
    }

    @SuppressWarnings("unchecked")
    public final <E extends ApplicationEdge> SequencedCollection<Self> shortestPathFrom(Self source, Graph<Self, E> graph) {
        Objects.requireNonNull(graph);
        if (!graph.containsVertex((Self) this) || !graph.containsVertex(source))
            return List.of();

        return graph.shortestVertexPathBetween(source, (Self) this);
    }


    //    @SuppressWarnings("unchecked")
//    public final Result<Set<E>, GraphModificationException> disconnectChildren() {
//        Set<E> edges = new HashSet<>();
//        assert graph().isPresent();
//        for (Self child : children()) {
//            var e = graph().get().edgeOf((Self) this, child).orElse(null); // TODO: REFACTOR
//            disconnectChild(child).ifPresent(_ -> edges.add(e));
//        }
//        return Result.of(edges);
//    }
//
//    @SuppressWarnings("unchecked")
//    public final Result<Set<E>, GraphModificationException> disconnectParents() {
//        Set<E> edges = new HashSet<>();
//        assert graph().isPresent();
//        for (Self parent : parents()) {
//            var e = graph().get().edgeOf(parent, (Self) this).orElse(null); // TODO: REFACTOR
//            disconnectParent(parent).ifPresent(_ -> edges.add(e));
//        }
//        return Result.of(edges);
//    }


//    public final Result<Set<ApplicationEdge<Self>>, DisconnectFailureResult> disconnectAll() { // TODO: Refactor into smaller methods
//        Set<ApplicationEdge<Self>> edges = new HashSet<>();
//        var children = children();
//        var parents = parents();
//
//        Set<Self> shouldDisconnectChild = new HashSet<>();
//        Set<Self> shouldDisconnectParent = new HashSet<>();
//        for (Self child : children)
//            switch (shouldDisconnectChild(child)) {
//                case Failure<?, ShouldDisconnectChildFailure>(var cause) -> {
//                    return switch (cause) {
//                        case EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
//                        case GraphDoesNotExist graphDoesNotExist -> fail(graphDoesNotExist);
//                        case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
//                        case SelfReference selfReference -> fail(selfReference);
//                        case VertexDoesNotExist vertexDoesNotExist -> fail(vertexDoesNotExist);
//                        case ApplicationVertex.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
//                    };
//                }
//
//                case Success<Void, ?> _ -> shouldDisconnectChild.add(child);
//            }
//
//        for (Self parent : parents)
//            switch (shouldDisconnectParent(parent)) {
//                case Failure<?, ShouldDisconnectParentFailure>(var cause) -> {
//                    return switch (cause) {
//                        case EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
//                        case GraphDoesNotExist graphDoesNotExist -> fail(graphDoesNotExist);
//                        case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
//                        case SelfReference selfReference -> fail(selfReference);
//                        case VertexDoesNotExist vertexDoesNotExist -> fail(vertexDoesNotExist);
//                        case ApplicationVertex.RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
//                    };
//                }
//
//                case Success<Void, ?> _ -> shouldDisconnectParent.add(parent);
//            }
//
//        assert graph().isPresent();
//        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
//            Set<StructuredTaskScope.Subtask<ApplicationEdge<Self>>> tasks = new HashSet<>();
//            var thiz = (Self) this;
//            for (Self child : shouldDisconnectChild)
//                tasks.add(scope.fork(() -> {
//                    var e = graph().get().removeEdge(thiz, child);
//
//                    if (e != null)
//                        edges.add(e);
//
//                    scope.fork(() -> {
//                        if (policiesManager.stateOf(ON_DISCONNECT_CHILD_POLICY, child) == ACCEPT)
//                            this.onDisconnectChild(child);
//                        return null;
//                    });
//
//                    scope.fork(() -> {
//                        if(child.policiesManager.stateOf(ON_DISCONNECT_PARENT_POLICY, thiz) == ACCEPT)
//                            child.onDisconnectParent(thiz);
//                        return null;
//                    });
//                    scope.join();
//                    return e;
//                }));
//
//            for (Self parent : shouldDisconnectParent)
//                tasks.add(scope.fork(() -> {
//                    var e = graph().get().removeEdge(parent, thiz);
//
//                    if (e != null)
//                        edges.add(e);
//
//                    scope.fork(() -> {
////                        if (shouldCallOnLeaveParentFor(parent))
//                        if (policiesManager.stateOf(ON_DISCONNECT_PARENT_POLICY, parent) == ACCEPT)
//                            this.onDisconnectParent(parent);
//                        return null;
//                    });
//
//                    scope.fork(() -> {
////                        if (parent.shouldCallOnLeaveChildFor(thiz))
//                        if (parent.policiesManager.stateOf(ON_DISCONNECT_CHILD_POLICY, thiz) == ACCEPT)
//                            parent.onDisconnectChild(thiz);
//                        return null;
//                    });
//                    return e;
//                }));
//            scope.join();
//            tasks.stream().map(StructuredTaskScope.Subtask::get).forEach(edges::add);
//
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return success(edges);
//    }

    //    public sealed interface ShouldReplaceEdgeResult {}
//
//    private Result<ApplicationEdge, ShouldReplaceEdgeResult> shouldReplaceEdge(Self targetConnectedVertex, Self connectionReplacementVertex) {
//        return null; // TODO: Implement
//    }

//
//    private Result<E, ComponentOperationFailure> handleEdgeReplacement(Self to, Self with, double weightToReplacement, double weightReplacementToOther) {
//        return null;
//    }
//
//    /**
//     * Replaces the edge between the current node and the given child node with the given vertex.
//     * The connectivity of this vertex and the other vertex is preserved, with the replacing vertex taking the place of the
//     * existing edge and being connected to both vertices preserving directionality.
//     *
//     * @param to The connected vertex to replace the edge to.
//     * @param with The vertex to replace the edge with.
//     * @return A Result indicating the outcome of the replacement attempt. This can be:
//     *        - A Present Result containing the replacement vertex if the operation was successful.
//     *        - An Absent Result if the operation was not successful.
//     *        - A Failure Result with a GraphModificationException if there was an issue with the replacement.
//     */
//    public Result<Self, ComponentOperationFailure> replaceConnection(Self to, Self with, double weightToReplacement, double weightReplacementToOther) {
//        return null;
//    }
//
//
//    public Result<Self, ComponentOperationFailure> replaceConnection(Self to, Self with) {
//        return null;
//    }
//
//    /**
//     * Preserves this object and contracts the given child node to this node.
//     * If the child node has other parents, the operation is only successful if those parents can be added as parents of
//     * this node or are already parents of this node.
//     * If the child node has other children, the operation is only successful if those children can be added as children
//     * of this node.
//     * The weight of the edges between the new parents and this node is the same as the weight of the edge between that
//     * parent and the child node. Except if the parent is already a parent of this node, in which case the weight of the
//     * edge is not changed.
//     * The weight of the edges between this node and the new children is the same as the weight of the edge between this
//     * node and the new child node plus the weight of the edge between the child node and that child.
//     * @param child
//     * @return
//     */
//    public final Result<Self, ComponentOperationFailure> contractChild(Self child) {
//        // TODO: Implement
//        return null;
//    }
//
//    /**
//     * Preserves this object and yada yada TODO
//     * @param parent
//     * @return
//     */
//    public final Result<Self, ComponentOperationFailure> contractParent(Self parent) {
//        // TODO: Implement
//        return null;
//    }
//
////    public final Result<Self, ComponentOperationFailure> contract(Self other) {
////        return null;
////    }
//
//    public final Result<Self, ComponentOperationFailure> contractToChild(Self child) {
//        return child.contractParent((Self) this);
//    }
//
//    public final Result<Self, ComponentOperationFailure> contractToParent(Self parent) {
//        return parent.contractChild((Self) this);
//    }
//
////    public final Result<Self, ComponentOperationFailure> contractTo(Self other) {
////        return other.contract((Self) this);
////    }
//
//    /**
//     * Replaces the current node with the given replacement node.
//     * This node must be able to be removed from the graph and the replacement node must be able to be in the graph and
//     * have the parents and children of this node.
//     * This node is removed from the graph.
//     * @param replacement
//     * @return
//     */ // Precondition: Graph: remove this, add replacement; Parents: detach child this, attach child replacement; Children: detach parent this, attach parent replacement.
//    public final Result<Self, ComponentOperationFailure> replaceWith(Self replacement) {
//        // TODO: Implement
//        return null;
//    }
//
//    /**
//     * Adds a sibling to this GameObject. A sibling is a GameObject that has the same parents as this GameObject but not
//     * limited to the same parents.
//     * Returns an empty Optional if this GameObject has no parents.
//     * @param sibling The sibling to add
//     * @return An Optional containing the sibling if it was added, an empty Optional otherwise
//     */
//    @SuppressWarnings("unchecked")
//    public final Result<Self, ComponentOperationFailure> addSibling(Self sibling) {
//        Objects.requireNonNull(sibling);
//
//        if (parents().isEmpty())
////            return Resulted.absent("The vertex has no parents");
//            return null;
//
//        return null; // TODO: Implement
//    }

    /**
     * Failure result of shouldConnectChild.
     */
    public sealed interface ShouldConnectChildFailure extends Vertex.ShouldConnectFailure permits
            FailureResults.EdgeAlreadyExists,
            FailureResults.GraphCycleDetected,
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation,
            FailureResults.SelfReference,
            FailureResults.VertexNotPresent {}

    /**
     * Failure result of shouldConnectParent.
     */
    public sealed interface ShouldConnectParentFailure extends Vertex.ShouldConnectFailure permits FailureResults.EdgeAlreadyExists, FailureResults.GraphCycleDetected, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexNotPresent {}

    public sealed interface ShouldDisconnectChildFailure extends Vertex.ShouldDisconnectFailure permits FailureResults.VertexNotPresent, FailureResults.EdgeNotPresent, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference {}

    public sealed interface ShouldDisconnectParentFailure extends Vertex.ShouldDisconnectFailure permits
            FailureResults.EdgeNotPresent,
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation,
            FailureResults.SelfReference,
            FailureResults.VertexNotPresent {}

    public sealed interface ShouldAddChildFailure extends Vertex.ShouldAddFailure permits FailureResults.EdgeAlreadyExists, FailureResults.GraphCycleDetected, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexAlreadyPresent, FailureResults.VertexNotPresent {}

    public sealed interface ShouldAddParentFailure extends Vertex.ShouldAddFailure permits
            FailureResults.EdgeAlreadyExists,
            FailureResults.GraphCycleDetected,
            FailureResults.RejectedByGraphPolicy,
            FailureResults.RejectedByGraphValidation,
            FailureResults.RejectedByVertexPolicy,
            FailureResults.RejectedByVertexValidation,
            FailureResults.SelfReference,
            FailureResults.VertexAlreadyPresent,
            FailureResults.VertexNotPresent {}

    public sealed interface ShouldRemoveChildFailure extends Vertex.ShouldRemoveFailure permits FailureResults.EdgeNotPresent, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexNotPresent {}

    public sealed interface ShouldRemoveParentFailure extends Vertex.ShouldRemoveFailure permits FailureResults.EdgeNotPresent, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexNotPresent {}

    /// Child connection failure result.
    public sealed interface ChildConnectionFailure extends Vertex.AttachFailureResult permits FailureResults.EdgeAlreadyExists, FailureResults.GraphCycleDetected, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexNotPresent {}

    public sealed interface DisconnectFailureResult extends Vertex.OperationFailureResult {}

    public sealed interface ChildDisconnectionFailure extends DisconnectFailureResult permits FailureResults.VertexNotPresent, FailureResults.EdgeNotPresent, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference {}

    public sealed interface ParentDisconnectionFailure extends DisconnectFailureResult permits FailureResults.VertexNotPresent, FailureResults.EdgeNotPresent, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference {}

    public sealed interface ChildRemovalFailure extends Vertex.OperationFailureResult permits FailureResults.VertexNotPresent, FailureResults.EdgeNotPresent, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference {}

    public sealed interface ParentRemovalFailure extends Vertex.OperationFailureResult permits FailureResults.VertexNotPresent, FailureResults.EdgeNotPresent, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference {}

    public sealed interface RemoveFailure permits FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.VertexNotPresent {}

    public sealed interface OperationFailureResult {

        String message();
    }

    public sealed interface ShouldConnectFailure extends OperationFailureResult {}

    public sealed interface ShouldDisconnectFailure extends OperationFailureResult {}

    public sealed interface ShouldAddFailure extends OperationFailureResult {}

    public sealed interface ShouldRemoveFailure extends OperationFailureResult {}

    public sealed interface AttachFailureResult extends OperationFailureResult {}

    public sealed interface ParentConnectionFailure extends AttachFailureResult permits FailureResults.EdgeAlreadyExists, FailureResults.GraphCycleDetected, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexNotPresent {}

    public sealed interface AddFailureResult extends OperationFailureResult {}

    public sealed interface ChildAdditionFailure extends AddFailureResult permits FailureResults.EdgeAlreadyExists, FailureResults.GraphCycleDetected, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexAlreadyPresent, FailureResults.VertexNotPresent {}

    public sealed interface ParentAdditionFailure extends AddFailureResult permits FailureResults.EdgeAlreadyExists, FailureResults.GraphCycleDetected, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexAlreadyPresent, FailureResults.VertexNotPresent {}
}
