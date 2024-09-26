package juanmanuel.tea.graph;

import juanmanuel.tea.graph.callbacks.VertexCallbackManager;
import juanmanuel.tea.graph.operation_failures.FailureResults.*;
import juanmanuel.tea.graph.policy.VertexOperationsPolicies;
import juanmanuel.tea.graph.validation.VertexOperationValidator;
import juanmanuel.tea.utils.Result;
import juanmanuel.tea.utils.Result.Failure;
import juanmanuel.tea.utils.Result.Success;
import org.jgrapht.graph.GraphCycleProhibitedException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static juanmanuel.tea.graph.policy.PolicyState.ACCEPT;
import static juanmanuel.tea.graph.policy.PolicyState.UNSET;
import static juanmanuel.tea.graph.policy.VertexPolicy.EdgeModificationVertexPolicy.*;
import static juanmanuel.tea.graph.policy.VertexPolicy.EffectVertexPolicy.*;
import static juanmanuel.tea.graph.policy.VertexPolicy.GraphModificationVertexPolicy.ON_ENTER_GRAPH_POLICY;
import static juanmanuel.tea.graph.policy.VertexPolicy.GraphModificationVertexPolicy.ON_LEAVE_GRAPH_POLICY;
import static juanmanuel.tea.graph.validation.VertexOperationValidator.VerticesOperationValidation.*;
import static juanmanuel.tea.utils.Result.fail;
import static juanmanuel.tea.utils.Result.success;

/// Class representing a vertex in a graph.
///
/// The instances of this class can have a graph, in which case they should be part of the graph.
///
/// This class is meant to be extended by the user to create a vertex with custom behavior.
/// @param <Self> The type of the vertex. This is used to make the class more flexible and to allow for more specific
///              behavior in the methods.
public abstract non-sealed class ApplicationVertex<Self extends ApplicationVertex<Self>> extends Vertex<Self> implements GraphElement {

    /// The policies manager of this vertex.
    /// This is used to manage the operations policies of the vertex.
    /// The policies manager is created lazily.
    VertexOperationsPolicies policiesManager;

    /// The validations manager of this vertex.
    /// This is used to manage the operations validations of the vertex.
    /// The validations manager is created lazily.
    protected VertexOperationValidator<Self> validationsManager;

    /// The callbacks manager of this vertex.
    /// This is used to manage the operations callbacks of the vertex.
    /// The callbacks manager is created lazily.
    protected VertexCallbackManager<Self> callbackManager;

    /// Whether the policy should accept the operation when its state is UNSET.
    private boolean acceptOnUnsetPolicy;

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
    /// Creates a new vertex with the default policies.
    public ApplicationVertex() {
        super();
    }

    /// Gets the policies manager of this vertex or creates it if it does not exist.
    /// @return The policies manager of this vertex.
    VertexOperationsPolicies policiesManager() {
        if (policiesManager == null)
            policiesManager = new VertexOperationsPolicies();

        return policiesManager;
    }

    /// @return True if the policy should accept the operation when it is not set, false otherwise.
    public boolean acceptOnUnsetPolicy() {
        return acceptOnUnsetPolicy;
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

    /// @return A set containing the children of this vertex.
    protected final Set<? super Self> children(ApplicationGraph<Self> graph) {
        Objects.requireNonNull(graph);
        return graph.getChildren(self());
    }

    /// @return A set containing the parents of this vertex.
    protected final Set<? super Self> parents(ApplicationGraph<Self> graph) {
        Objects.requireNonNull(graph);
        return graph.getParents(self());
    }

    /// @return A set containing the neighbors of this vertex.
    protected final Set<? super Self> neighbors(ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(graph);
        return graph.getNeighbors(self());
    }

    /// @return A set containing the descendants of this vertex.
    protected final Set<? super Self> descendants(ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(graph);
        return graph.getDescendants(self());
    }

    /// @return A set containing the ancestors of this GameObject
    protected final Set<? super Self> ancestors(ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(graph);

        return graph.getAncestors(self());
    }

    /// Returns a set containing other vertices that share at least one parent with this vertex
    /// @return A set containing the siblings of this vertex
    protected final Set<Self> siblings(ApplicationGraph<Self> graph) {
        Objects.requireNonNull(graph);

        Set<Self> siblings = new HashSet<>();

        for (Self parent : graph.getParents(self()))
            siblings.addAll(graph.getChildren(parent));

        siblings.remove(self());

        return siblings;
    }

    /// Returns a set containing other vertices that share all parents with this vertex
    /// @return A set containing the full siblings of this vertex
    public final Set<Self> fullSiblings(ApplicationGraph<Self> graph) {
        Objects.requireNonNull(graph);
        var siblings = siblings(graph);
        for (ApplicationVertex<Self> parent : graph.getParents(self()))
            siblings.removeIf(sibling -> !parent.hasChild(sibling, graph));

        return siblings;
    }

    /// Returns a set containing other vertices that share at least one parent and at most all minus one parents with
    /// this vertex.
    /// @return A set containing the half siblings of this vertex
    public final Set<Self> halfSiblings(ApplicationGraph<Self> graph) {
        Objects.requireNonNull(graph);
        var siblings = siblings(graph);
        siblings.removeAll(fullSiblings(graph));
        return siblings;
    }

    /// Checks if this vertex has the given vertex as a child.
    /// @param child The vertex to check
    /// @return true if the other vertex is a child of this vertex, false otherwise
    public final boolean hasChild(Self child, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return children(graph).contains(child);
    }

    /// Checks if this vertex has a child of the given type.
    /// @param childClass The type of the vertex to check.
    /// @return true if this vertex has a child of the given type, false otherwise
    public final boolean hasChild(Class<? extends Self> childClass, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(childClass);
        Objects.requireNonNull(graph);

        return children(graph).stream().anyMatch(childClass::isInstance);
    }

    /// Checks how many children of the given class this GameObject has.
    /// @param childClass The class of the children to check
    /// @return The number of children of the given class this GameObject has
    public final long numberOfChildren(Class<? extends Self> childClass, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(childClass);
        Objects.requireNonNull(graph);

        return children(graph).stream().filter(childClass::isInstance).count();
    }

    /**
     *
     * @param parent The head to check
     * @return true if this GameObject has the head, false otherwise
     */
    public final boolean hasParent(Self parent, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return parents(graph).contains(parent);
    }

    /**
     * Checks if this GameObject has a head of the given class.
     * @param parentClass The class of the head to check
     * @return true if this GameObject has a head of the given class, false otherwise
     */
    @SuppressWarnings("unused")
    public final boolean hasParent(Class<? extends Self> parentClass, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(parentClass);
        Objects.requireNonNull(graph);

        return parents(graph).stream().anyMatch(parentClass::isInstance);
    }

    /**
     * Checks how many parents of the given class this GameObject has.
     * @param parentClass The class of the parents to check
     * @return The number of parents of the given class this GameObject has
     */
    @SuppressWarnings("unused")
    public final long numberOfParents(Class<? extends Self> parentClass, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(parentClass);
        Objects.requireNonNull(graph);

        return parents(graph).stream().filter(parentClass::isInstance).count();
    }

    /**
     *
     * @param descendant The descendant to check
     * @return true if this GameObject has the descendant, false otherwise
     */
    @SuppressWarnings("unused")
    public final boolean hasDescendant(Self descendant, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(descendant);
        Objects.requireNonNull(graph);

        return descendants(graph).contains(descendant);
    }

    /**
     * Checks how many descendants of the given class this GameObject has.
     * @param descendantClass The class of the descendants to check
     * @return The number of descendants of the given class this GameObject has
     */
    @SuppressWarnings("unused")
    public final boolean hasDescendant(Class<? extends Self> descendantClass, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(descendantClass);
        Objects.requireNonNull(graph);

        return descendants(graph).stream().anyMatch(descendantClass::isInstance);
    }

    /**
     * Checks how many descendants of the given class this GameObject has.
     * @param descendantClass The class of the descendants to check
     * @return The number of descendants of the given class this GameObject has
     */
    @SuppressWarnings("unused")
    public final long numberOfDescendants(Class<? extends Self> descendantClass, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(descendantClass);
        Objects.requireNonNull(graph);

        return descendants(graph).stream().filter(descendantClass::isInstance).count();
    }

    /**
     *
     * @param ancestor The ancestor to check
     * @return true if this GameObject has the ancestor, false otherwise
     */
    @SuppressWarnings("unused")
    public final boolean hasAncestor(Self ancestor, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(ancestor);
        Objects.requireNonNull(graph);

        return ancestors(graph).contains(ancestor);
    }

    /**
     * Checks if this GameObject has an ancestor of the given class.
     * @param ancestorClass The class of the ancestor to check
     * @return true if this GameObject has an ancestor of the given class, false otherwise
     */
    @SuppressWarnings("unused")
    public final boolean hasAncestor(Class<? extends Self> ancestorClass, ApplicationGraph<Self> graph) {
        Objects.requireNonNull(ancestorClass);
        Objects.requireNonNull(graph);

        return ancestors(graph).stream().anyMatch(ancestorClass::isInstance);
    }

    /**
     * Checks how many ancestors of the given class this GameObject has.
     * @param ancestorClass The class of the ancestors to check
     * @return The number of ancestors of the given class this GameObject has
     */
    @SuppressWarnings("unused")
    public final long numberOfAncestors(Class<? extends ApplicationVertex<?>> ancestorClass, ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(ancestorClass);
        Objects.requireNonNull(graph);

        return ancestors(graph).stream().filter(ancestorClass::isInstance).count();
    }

    /**
     *
     * @return true if this GameObject has children, false otherwise
     */
    @SuppressWarnings("unused")
    public final boolean hasChildren(ApplicationGraph<Self> graph) {
        return !children(graph).isEmpty();
    }

    /**
     *
     * @return true if this GameObject has parents, false otherwise
     */
    @SuppressWarnings("unused")
    public final boolean hasParents(ApplicationGraph<Self> graph) {
        return !parents(graph).isEmpty();
    }

    protected Set<BiConsumer<Self, ApplicationGraph<? super Self>>> enterChildCallbacks() {
        return callbackManager.getCallbacksFor(VertexCallbackType.ON_CONNECT_CHILD);
    }

    protected Set<BiConsumer<Self, ApplicationGraph<? super Self>>> enterParentCallbacks() {
        return callbackManager.getCallbacksFor(VertexCallbackType.ON_CONNECT_PARENT);
    }

    protected Set<BiConsumer<Self, ApplicationGraph<? super Self>>> leaveChildCallbacks() {
        return callbackManager.getCallbacksFor(VertexCallbackType.ON_DISCONNECT_CHILD);
    }

    protected Set<BiConsumer<Self, ApplicationGraph<? super Self>>> leaveParentCallbacks() {
        return callbackManager.getCallbacksFor(VertexCallbackType.ON_DISCONNECT_PARENT);
    }

    protected Set<Consumer<ApplicationGraph<? super Self>>> enterGraphCallbacks() {
        return callbackManager.getCallbacksFor(GraphCallbackType.ON_ENTER_GRAPH);
    }

    protected Set<Consumer<ApplicationGraph<? super Self>>> leaveGraphCallbacks() {
        return callbackManager.getCallbacksFor(GraphCallbackType.ON_LEAVE_GRAPH);
    }

    /**
     * Adds a callback to be called when a tail is added to this GameObject.
     * @param callback A Consumer that takes the tail that was added as a parameter.
     */
    public final void addOnConnectChildCallback(BiConsumer<Self, ApplicationGraph<? super Self>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, VertexCallbackType.ON_CONNECT_CHILD);
    }

    /**
     * Adds a callback to be called when a head is added to this GameObject.
     * @param callback A Consumer that takes the head that this GameObject was added to as a parameter.
     */
    public final void addOnConnectParentCallback(BiConsumer<Self, ApplicationGraph<? super Self>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, VertexCallbackType.ON_CONNECT_PARENT);
    }

    /**
     * Adds a callback to be called when a tail is removed from this GameObject.
     * @param callback A Consumer that takes the tail that was removed as a parameter.
     */
    public final void addOnLeaveChildCallback(BiConsumer<Self, ApplicationGraph<? super Self>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, VertexCallbackType.ON_DISCONNECT_CHILD);
    }

    /**
     * Adds a callback to be called when a head is removed from this GameObject.
     * @param callback A Consumer that takes the head that this GameObject was removed from as a parameter.
     */
    public final void addOnLeaveParentCallback(BiConsumer<Self, ApplicationGraph<? super Self>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, VertexCallbackType.ON_DISCONNECT_PARENT);
    }

    public final void addOnEnterGraphCallback(Consumer<ApplicationGraph<? super Self>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, GraphCallbackType.ON_ENTER_GRAPH);
    }

    public final void addOnExitGraphCallback(Consumer<ApplicationGraph<? super Self>> callback) {
        Objects.requireNonNull(callback);
        callbackManager.addCallback(callback, GraphCallbackType.ON_LEAVE_GRAPH);
    }

    /**
     * Failure result of shouldConnectChild.
     */
    public sealed interface ShouldConnectChildFailure extends ShouldConnectFailure permits
            EdgeAlreadyExists,
            GraphCycleDetected,
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation,
            SelfReference,
            VertexNotPresent {}

    /**
     * Determines if a child node can be connected to the current node in the specified graph.
     * This vertex and the child vertex should be present in the graph
     * The policies of this node and the child node are checked to determine if the operation could be performed.
     *
     * @param child The child node to check.
     * @return A {@link Result} indicating whether the edge can be created, and if not, the rejection reason.
     */
    public final Result<Void, ShouldConnectChildFailure> shouldConnectChild(Self child, ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        if (child == this)
            return fail(new SelfReference("Cannot add itself as a child"));

        return switch (graph.shouldAddEdge(self(), child)) {
            case Failure<Void, ApplicationGraph.ShouldAddEdgeFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Success<Void, ApplicationGraph.ShouldAddEdgeFailure> _ -> success();
        };
    }

    /**
     * Failure result of shouldConnectParent.
     */
    public sealed interface ShouldConnectParentFailure extends ShouldConnectFailure permits EdgeAlreadyExists, GraphCycleDetected, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference, VertexNotPresent {}

    /**
     * Determines if a parent node can be connected to the current node in the specified graph.
     * This vertex and the parent vertex should be present in the graph.
     * The policies of this node and the parent node are checked to determine if the operation could be performed.
     *
     * @param parent The parent node to check.
     * @return A {@link Result} indicating whether the edge can be created, and if not, the rejection reason.
     * @throws NullPointerException if the parent is null.
     */
    public final Result<Void, ShouldConnectParentFailure> shouldConnectParent(Self parent, ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        if (parent == this)
            return fail(new SelfReference("Cannot add itself as a child"));

        return switch (graph.shouldAddEdge(parent, self())) {
            case Failure<Void, ApplicationGraph.ShouldAddEdgeFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Success<Void, ApplicationGraph.ShouldAddEdgeFailure> _ -> success();
        };
    }

    public sealed interface ShouldDisconnectChildFailure extends ShouldDisconnectFailure permits VertexNotPresent, EdgeNotPresent, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference {}

    /**
     * Determines if a child node can be disconnected from the current node.
     * The child node must be present in the graph and have the same graph as the current node.
     * The policies of the current node and the child node are checked to determine if the edge can be removed.
     *
     * @param child The child node to check.
     * @return A {@link Result} indicating whether the edge can be removed, and if not, the reason for rejection.
     * @throws NullPointerException if the child is null.
     */
    public final Result<Void, ShouldDisconnectChildFailure> shouldDisconnectChild(Self child, ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        if (child == this)
            return fail(new SelfReference("Cannot disconnect itself"));

        return switch (graph.shouldRemoveEdge(self(), child)) {
            case Failure<Void, ApplicationGraph.ShouldRemoveEdgeFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Success<Void, ApplicationGraph.ShouldRemoveEdgeFailure> _ -> success();
        };
    }

    public sealed interface ShouldDisconnectParentFailure extends ShouldDisconnectFailure permits
            EdgeNotPresent,
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation,
            SelfReference,
            VertexNotPresent {}

    /**
     * Determines if a parent node can be disconnected from the current node.
     * The parent node must be present in the graph and have the same graph as the current node.
     * The policies of the current node and the parent node are checked to determine if the edge can be removed.
     *
     * @param parent The parent node to check.
     * @return A {@link Result} indicating whether the edge can be removed, and if not, the reason for rejection.
     */
    public final Result<Void, ShouldDisconnectParentFailure> shouldDisconnectParent(Self parent, ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        if (parent == this)
            return fail(new SelfReference("Cannot remove itself"));

        return switch (graph.shouldRemoveEdge(parent, self())) {
            case Failure<Void, ApplicationGraph.ShouldRemoveEdgeFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Success<Void, ApplicationGraph.ShouldRemoveEdgeFailure> _ -> success();
        };
    }

    public sealed interface ShouldAddChildFailure extends ShouldAddFailure permits EdgeAlreadyExists, GraphCycleDetected, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference, VertexAlreadyPresent, VertexNotPresent {}

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
    public final Result<Void, ShouldAddChildFailure> shouldAddChild(Self child, ApplicationGraph<? super Self> graph)
            throws NullPointerException ,GraphOperationException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (shouldConnectChild(child, graph)) {
            case Failure<?, ShouldConnectChildFailure>(ApplicationVertex.ShouldConnectChildFailure cause) -> switch (cause) {
                case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case SelfReference selfReference -> fail(selfReference);
                case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                case VertexNotPresent _ -> switch (graph.shouldAddVertex(child)) {
                    case Failure<Void, ApplicationGraph.ShouldAddVertexFailure>(var f) -> switch (f) {
                        case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                        case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                        case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                        case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                        case VertexAlreadyPresent _ ->
                                throw new GraphOperationException("The vertex is already in the graph. Graph is inconsistent");
                    };
                    case Success<Void, ApplicationGraph.ShouldAddVertexFailure> _ -> success();
                };
            };
            case Success<Void, ?> _ -> fail(new VertexAlreadyPresent("The vertex is already in the graph"));
        };
    }

    public sealed interface ShouldAddParentFailure extends ShouldAddFailure permits
            EdgeAlreadyExists,
            GraphCycleDetected,
            RejectedByGraphPolicy,
            RejectedByGraphValidation,
            RejectedByVertexPolicy,
            RejectedByVertexValidation,
            SelfReference,
            VertexAlreadyPresent,
            VertexNotPresent {}

    /**
     * Determines if a parent node should be added as a parent to the current node.
     * If the parent has no graph, the addition is allowed. A result of {@link Success} will be returned.
     * Checks the policies of the current node and the parent node to determine if the parent can be added.
     *
     * @param parent The parent node to be added.
     * @return A {@link Result} indicating whether the parent should be added, and if not, the reason for rejection.
     * @throws NullPointerException if the parent is null.
     */
    public final Result<Void, ShouldAddParentFailure> shouldAddParent(Self parent, ApplicationGraph<? super Self> graph)
            throws NullPointerException, GraphOperationException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return switch (shouldConnectParent(parent, graph)) {
            case Failure<?, ShouldConnectParentFailure>(ApplicationVertex.ShouldConnectParentFailure cause) -> switch (cause) {
                case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case SelfReference selfReference -> fail(selfReference);
                case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);

                case VertexNotPresent _ -> switch (graph.shouldAddVertex(parent)) {
                    case Failure<Void, ApplicationGraph.ShouldAddVertexFailure>(var f) -> switch (f) {
                        case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                        case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                        case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                        case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                        case VertexAlreadyPresent _ ->
                                throw new GraphOperationException("The vertex is already in the graph. Graph is inconsistent");
                    };
                    case Success<Void, ApplicationGraph.ShouldAddVertexFailure> _ -> success();
                };
            };

            case Success<Void, ?> _ -> fail(new VertexAlreadyPresent("The vertex is already in the graph"));
        };
    }

    public sealed interface ShouldRemoveChildFailure extends ShouldRemoveFailure permits EdgeNotPresent, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference, VertexNotPresent {}

    /// Determines if a child node could be warframe removed from the current node.
    /// If the child has no graph, the removal is allowed. A result of [Success] will be returned.
    /// Checks the policies of the current node and the child node to determine if the child can be removed.
    ///
    /// @param child The child node to be removed.
    /// @return A [Result] indicating whether the child should be removed, and if not, the reason for rejection.
    /// @throws NullPointerException if the child is null.
    public final Result<Void, ShouldRemoveChildFailure> shouldRemoveChild(Self child, ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        if (child == this)
            return fail(new SelfReference("Cannot remove itself"));

        if (!graph.containsVertex(self()))
            return fail(new VertexNotPresent("The vertex is not present in the graph", self()));

        if (!graph.containsEdge(self(), child))
            return fail(new EdgeNotPresent("The edge is not present in the graph"));

        // ApplicationGraph::shouldRemoveVertex checks if the parents and children of the vertex allow the removal and
        // disconnection of the vertex. So, no need to check if the disconnect is allowed here.
        return switch (graph.shouldRemoveVertex(child)) {
            case Failure<Void, ApplicationGraph.ShouldRemoveVertexFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Success<Void, ApplicationGraph.ShouldRemoveVertexFailure> _ -> success();
        };

//        return switch (shouldDisconnectChild(child, graph)) {
//            case Failure<?, ShouldDisconnectChildFailure>(ApplicationVertex.ShouldDisconnectChildFailure cause) -> switch (cause) {
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
//                case Failure<Void, ApplicationGraph.ShouldRemoveVertexFailure>(var f) -> switch (f) {
//                    case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
//                    case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
//                    case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
//                    case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
//                    case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
//                };
//                case Result.Success<Void, ApplicationGraph.ShouldRemoveVertexFailure> _ -> success();
//            };
//        };
    }

    public sealed interface ShouldRemoveParentFailure extends ShouldRemoveFailure permits EdgeNotPresent, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference, VertexNotPresent {}

    /**
     * Determines if a parent node should be removed as a parent to the current node.
     * If the parent has no graph, the removal is allowed. A result of {@link Success} will be returned.
     * Checks the policies of the current node and the parent node to determine if the parent can be removed.
     *
     * @param parent The parent node to be removed.
     * @return A {@link Result} indicating whether the parent should be removed, and if not, the reason for rejection.
     */
    private Result<Void, ShouldRemoveParentFailure> shouldRemoveParent(Self parent, ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        if (parent == this)
            return fail(new SelfReference("Cannot remove itself"));

        if (!graph.containsVertex(self()))
            return fail(new VertexNotPresent("The vertex is not present in the graph", self()));

        if (!graph.containsEdge(parent, self()))
            return fail(new EdgeNotPresent("The edge is not present in the graph"));

        // ApplicationGraph::shouldRemoveVertex checks if the parents and children of the vertex allow the removal and
        // disconnection of the vertex. So, no need to check if the disconnect is allowed here.
        return switch (graph.shouldRemoveVertex(parent)) {
            case Failure<Void, ApplicationGraph.ShouldRemoveVertexFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Success<Void, ApplicationGraph.ShouldRemoveVertexFailure> _ -> success();
        };
    }

    /// Child connection failure result.
    public sealed interface ChildConnectionFailure extends AttachFailureResult permits EdgeAlreadyExists, GraphCycleDetected, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference, VertexNotPresent {}

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
    private Result<ApplicationEdge, ChildConnectionFailure> handleChildConnection(Self child, double weight, ApplicationGraph<? super Self> graph)
            throws NullPointerException, UnsupportedOperationException, GraphOperationException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (graph.processEdgeAddition(self(), child, weight)) {
            case Failure<ApplicationEdge, ApplicationGraph.EdgeAdditionFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
            };

            case Success<ApplicationEdge, ApplicationGraph.EdgeAdditionFailure>(var e) -> {
                if (e == null)
                    yield  fail(new EdgeAlreadyExists("The vertex is already a child of this vertex"));

                postConnectChildProcess(child, graph);
                yield success(e);
            }
        };
    }

    /// Processes the post-connection callbacks for the child node.
    private void postConnectChildProcess(Self child, ApplicationGraph<? super Self> graph) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> {
                // Call ApplicationVertex::onConnectChild callback if the policy permits it
                switch (policiesManager().stateOf(ON_CONNECT_CHILD_POLICY, child)) {
                    case ACCEPT -> this.onConnectChild(child, graph);
                    case REJECT, UNSET -> {}
                }
                return null;
            });

            scope.fork(() -> {
                // Call ApplicationVertex::onConnectParent callback on the child if the policy permits it
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
    private Result<ApplicationEdge, ParentConnectionFailure> handleParentConnection(Self parent, double weight, ApplicationGraph<? super Self> graph)
            throws NullPointerException, UnsupportedOperationException, GraphOperationException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return switch (graph.processEdgeAddition(parent, self(), weight)) {
            case Failure<ApplicationEdge, ApplicationGraph.EdgeAdditionFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
            };

            case Success<ApplicationEdge, ApplicationGraph.EdgeAdditionFailure>(var e) -> {
                if (e == null)
                    yield  fail(new EdgeAlreadyExists("The vertex is already a child of this vertex"));

                postConnectParentProcess(parent, graph);
                yield success(e);
            }
        };
    }

    private void postConnectParentProcess(Self parent, ApplicationGraph<? super Self> graph) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> {
                // Call ApplicationVertex::onConnectChild callback on the parent if the policy permits it
                switch (parent.policiesManager().stateOf(ON_CONNECT_CHILD_POLICY, self())) {
                    case ACCEPT -> this.onConnectChild(self(), graph);
                    case REJECT, UNSET -> {}
                }
                return null;
            });

            scope.fork(() -> {
                // Call ApplicationVertex::onConnectParent callback if the policy permits it
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


    // TODO: Is adding an already present vertex a failure?
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
    private Result<Self, ChildAdditionFailure> handleChildAddition(Self child, double weight, ApplicationGraph<Self> graph)
            throws NullPointerException, GraphCycleProhibitedException, UnsupportedOperationException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (graph.addVertexValidated(child)) {
            case Success<Self, ApplicationGraph.VertexAdditionFailure>(Self v) -> {
                if (v == null)
                    throw new GraphOperationException("The vertex is already in the graph. Graph is inconsistent"); // TODO: Improve error message

                yield switch (graph.executeValidatedEdgeAdditionWithoutCallbacks(self(), child, weight)) {
                    case Failure<ApplicationEdge, ApplicationGraph.EdgeAdditionFailure>(var eaf) -> {
                        graph.removeVertex(child);
                        yield switch (eaf) {
                            case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                            case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                            case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                            case RejectedByVertexValidation rejectedByVertexValidation ->
                                    fail(rejectedByVertexValidation);
                            case VertexNotPresent _ ->
                                    throw new GraphOperationException("The vertex is not present in the graph. Graph is inconsistent");
                            case EdgeAlreadyExists _ ->
                                    throw new GraphOperationException("The edge already exists. Graph is inconsistent");
                            case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                        };
                    }

                    case Result.Success<ApplicationEdge, ApplicationGraph.EdgeAdditionFailure> _ -> {
                        graph.processVertexAdditionCallbacks(child);
                        graph.processEdgeAdditionCallbacks(self(), child);
                        postConnectChildProcess(child, graph);
                        yield success(child);
                    }
                };
            }

            case Failure<Self, ApplicationGraph.VertexAdditionFailure>(var vaf) -> switch (vaf) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexAlreadyPresent _ -> switch (handleChildConnection(child, weight, graph)) {
                    case Failure<ApplicationEdge, ChildConnectionFailure>(var ccf) -> switch (ccf) {
                        case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                        case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                        case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                        case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                        case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                        case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                        case SelfReference selfReference -> fail(selfReference);
                        case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                    };
                    case Result.Success<ApplicationEdge, ChildConnectionFailure> _ -> success();
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
    private Result<Self, ParentAdditionFailure> handleParentAddition(Self parent, double weight, ApplicationGraph<Self> graph)
            throws NullPointerException, GraphCycleProhibitedException, UnsupportedOperationException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return switch (graph.addVertexValidated(parent)) {
            case Success<Self, ApplicationGraph.VertexAdditionFailure>(Self v) -> {
                if (v == null)
                    throw new GraphOperationException("The vertex is already in the graph. Graph is inconsistent"); // TODO: Improve error message

                yield switch (graph.executeValidatedEdgeAdditionWithoutCallbacks(parent, self(), weight)) {
                    case Failure<ApplicationEdge, ApplicationGraph.EdgeAdditionFailure>(var eaf) -> {
                        graph.removeVertex(parent);
                        yield switch (eaf) {
                            case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                            case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                            case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                            case RejectedByVertexValidation rejectedByVertexValidation ->
                                    fail(rejectedByVertexValidation);
                            case VertexNotPresent _ ->
                                    throw new GraphOperationException("The vertex is not present in the graph. Graph is inconsistent");
                            case EdgeAlreadyExists _ ->
                                    throw new GraphOperationException("The edge already exists. Graph is inconsistent");
                            case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                        };
                    }

                    case Result.Success<ApplicationEdge, ApplicationGraph.EdgeAdditionFailure> _ -> {
                        graph.processVertexAdditionCallbacks(parent);
                        graph.processEdgeAdditionCallbacks(parent, self());
                        postConnectParentProcess(parent, graph);
                        yield success(parent);
                    }
                };
            }

            case Failure<Self, ApplicationGraph.VertexAdditionFailure>(var vaf) -> switch (vaf) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexAlreadyPresent _ -> switch (handleParentConnection(parent, weight, graph)) {
                    case Failure<ApplicationEdge, ParentConnectionFailure>(var pcf) -> switch (pcf) {
                        case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
                        case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                        case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                        case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                        case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                        case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                        case SelfReference selfReference -> fail(selfReference);
                        case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                    };
                    case Result.Success<ApplicationEdge, ParentConnectionFailure> _ -> success();
                };
            };
        };
    }

    public sealed interface DisconnectFailureResult extends OperationFailureResult {}

    public sealed interface ChildDisconnectionFailure extends DisconnectFailureResult permits VertexNotPresent, EdgeNotPresent, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference {}

    /// @param child
    /// @param graph
    /// @return
    /// @throws NullPointerException
    /// @throws UnsupportedOperationException
    /// @throws GraphOperationException
    protected Result<ApplicationEdge, ChildDisconnectionFailure> handleChildDisconnection(Self child, ApplicationGraph<? super Self> graph)
            throws NullPointerException, UnsupportedOperationException, GraphOperationException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (graph.processEdgeRemoval(self(), child)) {
            case Failure<ApplicationEdge, ApplicationGraph.EdgeRemovalFailure>(var erf) -> switch (erf) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
            };

            case Success<ApplicationEdge, ApplicationGraph.EdgeRemovalFailure>(var e) -> {
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

    public sealed interface ParentDisconnectionFailure extends DisconnectFailureResult permits VertexNotPresent, EdgeNotPresent, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference {}

    /// @param parent
    /// @param graph
    /// @return
    /// @throws IllegalStateException
    protected Result<ApplicationEdge, ParentDisconnectionFailure> handleParentDisconnection(Self parent, ApplicationGraph<? super Self> graph)
            throws IllegalStateException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        return switch (graph.processEdgeRemoval(parent, self())) {
            case Failure<ApplicationEdge, ApplicationGraph.EdgeRemovalFailure>(var erf) -> switch (erf) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case EdgeNotPresent edgeNotPresent -> fail(edgeNotPresent);
            };

            case Success<ApplicationEdge, ApplicationGraph.EdgeRemovalFailure>(var e) -> {
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

    public sealed interface ChildRemovalFailure extends OperationFailureResult permits VertexNotPresent, EdgeNotPresent, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference {}

    /// @param child
    /// @param graph
    /// @return
    /// @throws IllegalStateException
    /// @throws InterruptedException
    protected Result<Self, ChildRemovalFailure> handleChildRemoval(Self child, ApplicationGraph<Self> graph)
            throws IllegalStateException {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        if (!this.hasChild(child, graph))
            return fail(new EdgeNotPresent("The edge is not present in the graph"));

        switch (graph.processVertexRemoval(child)) {
            case Failure<Self, ApplicationGraph.VertexRemovalFailure>(var vrf) -> {
                return switch (vrf) {
                    case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                    case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                    case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                    case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                    case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                };
            }
            case Success<Self, ApplicationGraph.VertexRemovalFailure>(var v) -> {
                return success(v);
            }
        }
    }

    public sealed interface ParentRemovalFailure extends OperationFailureResult permits VertexNotPresent, EdgeNotPresent, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference {}

    /// @param parent
    /// @param graph
    /// @return
    /// @throws InterruptedException
    /// @throws IllegalStateException
    private Result<Self, ParentRemovalFailure> handleParentRemoval(Self parent, ApplicationGraph<Self> graph)
            throws IllegalStateException {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(graph);

        if (!this.hasParent(parent, graph))
            return fail(new EdgeNotPresent("The edge is not present in the graph"));

        switch (graph.processVertexRemoval(parent)) {
            case Failure<Self, ApplicationGraph.VertexRemovalFailure>(var vrf) -> {
                return switch (vrf) {
                    case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                    case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                    case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                    case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                    case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                };
            }
            case Success<Self, ApplicationGraph.VertexRemovalFailure>(var v) -> {
                return success(v);
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
    /// If the connection is successful, the edge is returned enclosed in a [Success] instance.
    /// If the connection fails, the reason for the failure is returned enclosed in a [Failure] instance.
    public final Result<ApplicationEdge, ChildConnectionFailure> connectChild(Self child, double weight, ApplicationGraph<? super Self> graph) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(graph);

        return switch (handleChildConnection(child, weight, graph)) {
            case Failure<ApplicationEdge, ChildConnectionFailure>(var f) -> switch (f) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case GraphCycleDetected graphCycleDetected -> fail(graphCycleDetected);
                case SelfReference selfReference -> fail(selfReference);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
                case EdgeAlreadyExists edgeAlreadyExists -> fail(edgeAlreadyExists);
            };
            case Success<ApplicationEdge, ChildConnectionFailure>(var e) -> success(e);
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
    /// If the connection is successful, the edge is returned enclosed in a [Success] instance.
    /// If the connection fails, the reason for the failure is returned enclosed in a [Failure] instance.
    public final Result<ApplicationEdge, ChildConnectionFailure> connectChild(Self child, ApplicationGraph<? super Self> graph) {
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
    /// If the operation is successful, the child is returned enclosed in a [Success] instance.
    /// If the operation fails, the reason for the failure is returned enclosed in a [Failure] instance.
    /// @throws NullPointerException if the child or the graph is null.
    public final Result<Self, ChildAdditionFailure> addChild(Self child, double weight, ApplicationGraph<Self> graph)
            throws NullPointerException {
        return switch (handleChildAddition(child, weight, graph)) {
            case Failure<Self, ChildAdditionFailure> f -> f;
            case Success<Self, ?>(var c) -> success(c);
        };
    }

    /// Adds the given vertex to the given graph and connects it as a child to this vertex.
    ///
    /// The weight of the edge connecting the child to the current node is set to 1.0.
    ///
    /// @param child The child to add.
    /// @param graph The graph in which the operation is performed.
    /// @return A [Result] indicating the outcome of the operation.
    /// If the operation is successful, the child is returned enclosed in a [Success] instance.
    /// If the operation fails, the reason for the failure is returned enclosed in a [Failure] instance.
    /// @throws NullPointerException if the child or the graph is null.
    public final Result<Self, ChildAdditionFailure> addChild(Self child, ApplicationGraph<Self> graph)
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
    /// @return A [Success] with the edge connecting the parent to the current node if the operation was successful, an
    /// empty [Result] if nothing was connected, or a [Failure] with the reason for the failure.
    public final Result<ApplicationEdge, ParentConnectionFailure> connectParent(Self parent, double weight, ApplicationGraph<? super Self> graph) {
        return handleParentConnection(parent, weight, graph);
    }

    public final Result<ApplicationEdge, ParentConnectionFailure> connectParent(Self parent, ApplicationGraph<? super Self> graph) {
        return connectParent(parent, 1.0, graph);
    }

    public final Result<Self, ParentAdditionFailure> addParent(Self parent, double weight, ApplicationGraph<Self> graph)
            throws NullPointerException {
        return handleParentAddition(parent, weight, graph);
    }

    public final Result<Self, ParentAdditionFailure> addParent(Self parent, ApplicationGraph<Self> graph) throws IllegalArgumentException {
        return addParent(parent, 1.0, graph);
    }


    public final Result<ApplicationEdge, ChildDisconnectionFailure> disconnectChild(Self child, ApplicationGraph<? super Self> graph) {
        return handleChildDisconnection(child, graph);
    }


    public final Result<Self, ChildRemovalFailure> removeChild(Self child, ApplicationGraph<Self> graph) {
        return handleChildRemoval(child, graph);
    }

    public final Result<ApplicationEdge, ParentDisconnectionFailure> disconnectParent(Self parent, ApplicationGraph<? super Self> graph) {
        return handleParentDisconnection(parent, graph);
    }

    public final Result<Self, Set<ChildDisconnectionFailure>> disconnectChildren(ApplicationGraph<Self> graph) {
        Set<ChildDisconnectionFailure> failures = new HashSet<>();
        for (Self child : graph.getChildren(self()))
            disconnectChild(child, graph).ifFailure(failures::add);

        if (failures.isEmpty())
            return success();

        return fail(failures);
    }

    public final Result<Self, Set<ParentDisconnectionFailure>> disconnectParents(ApplicationGraph<Self> graph) {
        return disconnectParents(graph, _ -> true);
    }

    /// Tries to disconnect all the parents of the current node that satisfy the given predicate.
    /// If any of the disconnections fail, a
    public final Result<Self, Set<ParentDisconnectionFailure>> disconnectParents(ApplicationGraph<Self> graph, Predicate<Self> predicate) {
        Set<ParentDisconnectionFailure> failures = new HashSet<>();
        Set<Self> pending = new HashSet<>();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (Self parent : graph.getParents(self())) {
                scope.fork(() -> {
                    if (predicate.test(parent))
                        switch (shouldDisconnectParent(parent, graph)) {
                            case Failure<Void, ShouldDisconnectParentFailure>(ShouldDisconnectParentFailure f) -> {
                                switch (f) {
                                    case EdgeNotPresent _, VertexNotPresent _ -> {}
                                    default -> failures.add(switch (f) {
                                        case RejectedByGraphPolicy rejectedByGraphPolicy -> rejectedByGraphPolicy;
                                        case RejectedByGraphValidation rejectedByGraphValidation -> rejectedByGraphValidation;
                                        case RejectedByVertexPolicy rejectedByVertexPolicy -> rejectedByVertexPolicy;
                                        case RejectedByVertexValidation rejectedByVertexValidation -> rejectedByVertexValidation;
                                        case SelfReference selfReference -> selfReference;
                                        default -> throw new IllegalStateException("Unexpected value: " + f);
                                    });
                                }
                            }
                            case Result.Success<Void, ShouldDisconnectParentFailure> _ -> pending.add(parent);
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

    public final Result<Self, ParentRemovalFailure> removeParent(Self parent, ApplicationGraph<Self> graph) {
        return handleParentRemoval(parent, graph);
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

    public sealed interface RemoveFailure permits RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, VertexNotPresent {}

    private Result<Self, RemoveFailure> handleRemoval(ApplicationGraph<Self> graph) {
        return switch (graph.processVertexRemoval(self())) {
            case Failure<Self, ApplicationGraph.VertexRemovalFailure>(var vrf) -> switch (vrf) {
                case RejectedByGraphPolicy rejectedByGraphPolicy -> fail(rejectedByGraphPolicy);
                case RejectedByGraphValidation rejectedByGraphValidation -> fail(rejectedByGraphValidation);
                case RejectedByVertexPolicy rejectedByVertexPolicy -> fail(rejectedByVertexPolicy);
                case RejectedByVertexValidation rejectedByVertexValidation -> fail(rejectedByVertexValidation);
                case VertexNotPresent vertexNotPresent -> fail(vertexNotPresent);
            };

            case Success<Self, ApplicationGraph.VertexRemovalFailure>(Self v) -> success(v);
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
    protected final Result<Self, RemoveFailure> removeFromGraph(ApplicationGraph<Self> graph) {
        return handleRemoval(graph);
    }

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
    public boolean shouldCallOnEnterGraphFor(ApplicationGraph<?> graph) {
        return policiesManager.stateOf(ON_ENTER_GRAPH_POLICY, graph) == ACCEPT
                || (acceptOnUnsetPolicy() && policiesManager.stateOf(ON_ENTER_GRAPH_POLICY, graph) == UNSET);
    }

    public boolean shouldCallOnLeaveGraphFor(ApplicationGraph<?> graph) {
        return policiesManager.stateOf(ON_LEAVE_GRAPH_POLICY, graph) == ACCEPT
                || (acceptOnUnsetPolicy() && policiesManager.stateOf(ON_LEAVE_GRAPH_POLICY, graph) == UNSET);
    }

    public <V extends ApplicationVertex<V>> boolean shouldCallOnDisconnectChildFor(V v) {
        return switch (policiesManager().stateOf(ON_DISCONNECT_CHILD_POLICY, v)) {
            case REJECT -> false;
            case ACCEPT -> true;
            case UNSET -> acceptOnUnsetPolicy();
        };
    }

    public <V extends ApplicationVertex<V>> boolean shouldCallOnDisconnectParentFor(V v) {
        return switch (policiesManager().stateOf(ON_DISCONNECT_PARENT_POLICY, v)) {
            case REJECT -> false;
            case ACCEPT -> true;
            case UNSET -> acceptOnUnsetPolicy();
        };
    }

    public <V extends ApplicationVertex<V>> boolean shouldCallOnConnectChildFor(V v) {
        return switch (policiesManager().stateOf(ON_CONNECT_CHILD_POLICY, v)) {
            case REJECT -> false;
            case ACCEPT -> true;
            case UNSET -> acceptOnUnsetPolicy();
        };
    }

    public <V extends ApplicationVertex<V>> boolean shouldCallOnConnectParentFor(V v) {
        return switch (policiesManager().stateOf(ON_CONNECT_PARENT_POLICY, v)) {
            case REJECT -> false;
            case ACCEPT -> true;
            case UNSET -> acceptOnUnsetPolicy();
        };
    }

    protected void onConnectChild(Self child, ApplicationGraph<? super Self> graph) throws RuntimeException {
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

    protected void onConnectParent(Self parent, ApplicationGraph<? super Self> graph) throws RuntimeException {
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

    protected void onDisconnectChild(Self child, ApplicationGraph<? super Self> graph) throws RuntimeException {
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

    protected void onDisconnectParent(Self parent, ApplicationGraph<? super Self> graph) throws RuntimeException {
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

    protected void onEnterGraph(ApplicationGraph<? super Self> graph) throws RuntimeException {
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

    protected void onLeaveGraph(ApplicationGraph<? super Self> graph) throws RuntimeException {
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
    public sealed interface OperationFailureResult {

        String message();
    }

    public sealed interface ShouldConnectFailure extends OperationFailureResult {}

    public sealed interface ShouldDisconnectFailure extends OperationFailureResult {}

    public sealed interface ShouldAddFailure extends OperationFailureResult {}

    public sealed interface ShouldRemoveFailure extends OperationFailureResult {}

    public sealed interface AttachFailureResult extends OperationFailureResult {}
    public sealed interface ParentConnectionFailure extends AttachFailureResult permits EdgeAlreadyExists, GraphCycleDetected, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference, VertexNotPresent {}

    public sealed interface AddFailureResult extends OperationFailureResult {}
    public sealed interface ChildAdditionFailure extends AddFailureResult permits EdgeAlreadyExists, GraphCycleDetected, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference, VertexAlreadyPresent, VertexNotPresent {}
    public sealed interface ParentAdditionFailure extends AddFailureResult permits EdgeAlreadyExists, GraphCycleDetected, RejectedByGraphPolicy, RejectedByGraphValidation, RejectedByVertexPolicy, RejectedByVertexValidation, SelfReference, VertexAlreadyPresent, VertexNotPresent {}

}
