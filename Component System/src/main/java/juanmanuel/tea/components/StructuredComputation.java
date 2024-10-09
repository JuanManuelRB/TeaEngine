package juanmanuel.tea.components;

import juanmanuel.tea.graph.ApplicationEdge;
import juanmanuel.tea.graph.Graph;
import juanmanuel.tea.graph.Vertex;
import juanmanuel.tea.graph.operation_failures.FailureResults;
import juanmanuel.tea.graph.operation_failures.vertex.ChildAdditionFailure;
import juanmanuel.tea.graph.operation_failures.vertex.ChildDisconnectionFailure;
import juanmanuel.tea.graph.operation_failures.vertex.ParentAdditionFailure;
import juanmanuel.tea.graph.operation_failures.vertex.ParentDisconnectionFailure;
import juanmanuel.tea.utils.Result;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/// Represents a computation that can be executed by an Updater.
///
/// This class is a vertex that an associated Updater can handle. The updater is responsible for providing a graph that
/// stores the vertices.
///
/// This class does not have any logic to update the updated object. The logic to update the object is contained in the Updater
/// class.
/// @param <Upr> The type of the Updater that can execute the updated object.
/// @param <Upd> The type of the Updated object that is computed.
/// @param <Self> The type of the UpdaterComputation.
@NullMarked
public abstract class StructuredComputation <
        Upr extends Updater<Upr, Upd, Self>,
        Upd extends Updated,
        Self extends StructuredComputation<Upr, Upd, Self>
        > extends Vertex<Self> {
    protected final Map<StructuredComputation<Upr, Upd, Self>, Boolean> previousComputations = new HashMap<>();
    private final Upd updated;
    private final Class<Upr> updaterClass;

    private final Semaphore semaphore;

    public StructuredComputation(Class<Upr> updaterClass, Upd updated, int concurrentComputations) {
        Objects.requireNonNull(updaterClass);
        Objects.requireNonNull(updated);
        super();

        this.updaterClass = updaterClass;
        this.updated = updated;
        this.semaphore = new Semaphore(concurrentComputations);

//        validationsManager() // TODO
//                .addOperationValidation(
//                        VertexOperationValidator.VerticesOperationValidation.CONNECT_CHILD_VALIDATION,
//                        other -> isEquivalent(other) ? Result.success(null) : Result.fail(null) // FIXME: failure cannot be null
//                );
    }

    public StructuredComputation(Class<Upr> updaterClass, Upd updated) {
        this(updaterClass, updated, 1);
    }

    protected Map<StructuredComputation<Upr, Upd, Self>, Boolean> previousComputations() {
        return previousComputations;
    }

    public Upd updated() {
        return updated;
    }

    public final Class<Upr> updaterClass() {
        return updaterClass;
    }

    @SuppressWarnings("unchecked")
    public final Class<Upd> updatedClass() {
        return (Class<Upd>) updated.getClass();
    }

    @SuppressWarnings("unchecked")
    public final Class<Self> computationClass() {
        return (Class<Self>) this.getClass();
    }

    /**
     * Gets a Supplier of a new StructuredComputation object with the given Updater and Updated objects.
     * @param updated The Updated object
     * @return A Supplier of a new UpdaterComputation object with the given Updater and Updated objects
     */
    protected abstract Supplier<Self> computationSupplier(Upd updated);

    /**
     * Gets a new StructuredComputation with the same Updater as this StructuredComputation and the given Updated object.
     * @param updated The Updated object
     * @return A new UpdaterComputation object with the same Updater as this UpdaterComputation and the given Updated object
     */
    public final Self newComputationOf(Upd updated) {
        Objects.requireNonNull(updated);
        return computationSupplier(updated).get();
    }

    /**
     * This method is used to find a computation in the graph that matches the given updated object.
     * If such a computation is found, it is returned wrapped in an Optional.
     * If no such computation is found, a new computation is created with the given updated object and returned wrapped in an Optional.
     *
     * @param updated The updated object to find the computation of.
     * @return An Optional containing the computation that matches the given updated object, or a new computation if no such computation is found.
     */
    protected final Self findOrNewComputation(Upd updated, Upr updater) {
        return findComputation(updated, updater).orElse(newComputationOf(updated));
    }

    /**
     * This method is used to find a computation in the graph that matches the given updated object.
     * It first checks if the updated object of this computation is the same as the given updated object.
     * If it is, it returns an Optional containing this computation.
     * If it is not, it goes through all the vertices in the graph and filters out the computations that have the same updater class and updated object as this computation.
     * It then returns the first computation that matches these conditions, or an empty Optional if no such computation is found.
     *
     * @param updated The updated object to find the computation of.
     * @return An Optional containing the computation that matches the given updated object, or an empty Optional if no such computation is found.
     */
    public final Optional<Self> findComputation(Upd updated, Upr updater) {
        if (this.updated == updated)
            return Optional.of(self());

        return updater.graph().vertexSet()
                .stream()
                .filter(c -> Objects.equals(c.updated(), updated))
                .findFirst();
    }
//
//    /**
//     * This method is used to find a computation in the descendants of this computation that matches the given updated object.
//     * It first checks if the updated object of this computation is the same as the given updated object.
//     * If it is, it returns an Optional containing this computation.
//     * If it is not, it goes through all the descendants in the graph and filters out the computations that have the same updated object as this computation.
//     * It then returns the first computation that matches these conditions, or an empty Optional if no such computation is found.
//     *
//     * @param updated The updated object to find the computation of.
//     * @return An Optional containing the computation that matches the given updated object, or an empty Optional if no such computation is found.
//     */
//    public final Optional<Self> findSuccessorComputation(Upd updated) {
//        Objects.requireNonNull(updated);
//        if (this.updated == updated)
//            return Optional.of(self());
//
//        return descendants().stream()
//                .filter(c -> Objects.equals(c.updated(), updated))
//                .findFirst();
//    }
//
//    /**
//     * This method is used to find a computation in the ancestors of this computation that matches the given updated object.
//     * It first checks if the updated object of this computation is the same as the given updated object.
//     * If it is, it returns an Optional containing this computation.
//     * If it is not, it goes through all the ancestors in the graph and filters out the computations that have the same updated object as this computation.
//     * It then returns the first computation that matches these conditions, or an empty Optional if no such computation is found.
//     *
//     * @param updated The updated object to find the computation of.
//     * @return An Optional containing the computation that matches the given updated object, or an empty Optional if no such computation is found.
//     */
//    @SuppressWarnings("unchecked")
//    public final Optional<Self> findPredecessorComputation(Upd updated) {
//        Objects.requireNonNull(updated);
//        if (this.updated == updated)
//            return Optional.of((Self) this);
//
//        return ancestors().stream()
//                .filter(c -> c.updaterClass() == updaterClass() && c.updated() == updated)
//                .findFirst();
//    }

    /// This method is used to start the computation of this StructuredComputation's Updated object, with the given Updater.
    ///
    /// This method is intended to be the entry point to compute the graph.
    ///
    ///
    ///
    /// @param updater The Updater object that is executing this StructuredComputation's computation.
    /// @throws InterruptedException If the computation is interrupted while waiting for the semaphore to be released.
    public final void startComputationBy(Upr updater) throws InterruptedException {
        // TODO: Add a way to cancel the computation
        // TODO: Add a way to check if the computation is already running

        semaphore.acquire();
        onStartCompute(updater);
        computeBy(updater);
        onFinishCompute(updater);
        semaphore.release();
    }

    /// Performs the computation of this StructuredComputation's Updated, with the given Updater.
    /// @param updater The Updater object that is executing this StructuredComputation's computation.
    public void computeBy(Upr updater) {
        updater.update(self());
    }

    /// This method is called before the computation starts. It performs these tasks:
    /// 1. It resets the previous computations by setting their values to false.
    /// 2. It notifies the children that the computation has started by calling their onParentComputeStarts method.
    /// 3. It notifies the parents that the computation has started by calling their onChildComputeStarts method.
    ///
    /// The method uses a StructuredTaskScope to ensure that all tasks are completed, even if an exception is thrown.
    /// If any task throws an exception, the StructuredTaskScope will be shut down and the exception will be rethrown.
    ///
    /// @param updater The Updater object that is executing this StructuredComputation's computation.
    /// @throws InterruptedException If the computation is interrupted while waiting for the tasks to complete.
    protected void onStartCompute(Upr updater) throws InterruptedException {
        Objects.requireNonNull(updater);
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Reset the previous computations
            for (var previousComputation : previousComputations.entrySet())
                scope.fork(() -> {
                    previousComputation.setValue(false);
                    return null;
                });

            // Notify the children that the computation has started
            for (Self child : updater.graph().childrenOf(self()))
                scope.fork(() -> {
                    child.onParentComputeStarts(self(), updater);
                    return null;
                });

            // Notify the parents that the computation has started
            for (Self parent : updater.graph().parentsOf(self()))
                scope.fork(() -> {
                    parent.onChildComputeStarts(self(), updater);
                    return null;
                });
            scope.join();
        }
    }

    /// This method is called after the computation finishes. It performs several tasks:
    /// 1. It notifies the children that the computation has finished by calling their onParentComputeFinished method.
    /// 2. It notifies the parents that the computation has finished by calling their onChildComputeFinished method.
    ///
    /// The method uses a StructuredTaskScope to ensure that all tasks are completed, even if an exception is thrown.
    /// If any task throws an exception, the StructuredTaskScope will be shut down and the exception will be rethrown.
    ///
    /// @param updater The Updater object that is executing this StructuredComputation's computation.
    /// @throws InterruptedException If the computation is interrupted while waiting for the tasks to complete.
    protected void onFinishCompute(Upr updater) throws InterruptedException {
        Objects.requireNonNull(updater);
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Notify the children that the computation has finished
            for (var child : updater.graph().childrenOf(self()))
                scope.fork(() -> {
                    child.onParentComputeFinished(self(), updater);
                    return null;
                });

            // Notify the parents that the computation has finished
            for (var parent : updater.graph().parentsOf(self()))
                scope.fork(() -> {
                    parent.onChildComputeFinished(self(), updater);
                    return null;
                });
            scope.join();
        }
    }

    // TODO: Create proper failure types for StructuredComputation instead of reusing the ones from the graph package?
    // RejectedByGraph, RejectedByVertex, ComputationNotPresent, ComputationAlreadyPresent, SelfReference, ComputationNotConnected, ComputationAlreadyConnected
    public sealed interface SetAfterFailure extends FailureResults permits RejectedByGraph, FailureResults.EdgeAlreadyExists, FailureResults.EdgeNotPresent, FailureResults.GraphCycleDetected, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexAlreadyPresent, FailureResults.VertexNotPresent {}

    record RejectedByGraph(String message) implements SetAfterFailure {}

    /// Sets a computation containing the given updated object as a child of this computation.
    ///
    /// If the graph already contains a computation with the given updated object, no new computation is created.
    /// The computation is added as a child of this computation and the previous parents of the computation are removed.
    ///
    /// If the graph does not contain a computation with the given updated object, a new computation is created.
    ///
    /// @param updated The updated object that will execute after this computation.
    /// @return A [Result] containing the computation that was added as a child, or a failure if the computation was not
    /// added.
    /// @throws NullPointerException If the updated object is null or the updater is null.
    public Result<Self, SetAfterFailure> setAfter(Upd updated, Upr updater) throws NullPointerException {
        Objects.requireNonNull(updated);
        Objects.requireNonNull(updater);

        Self sComp = findOrNewComputation(updated, updater);
        var graph = updater.graph();

        return switch (addChild(sComp, graph)) { // Add the computation as a child
            case Result.Failure<Self, ChildAdditionFailure>(var f) -> switch (f) {
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> Result.fail(rejectedByGraphPolicy);
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> Result.fail(rejectedByGraphValidation);
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> Result.fail(rejectedByVertexPolicy);
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> Result.fail(rejectedByVertexValidation);
                case FailureResults.VertexNotPresent vertexNotPresent -> Result.fail(vertexNotPresent);
                case FailureResults.VertexAlreadyPresent vertexAlreadyPresent -> Result.fail(vertexAlreadyPresent);
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> Result.fail(edgeAlreadyExists);
                case FailureResults.SelfReference selfReference -> Result.fail(selfReference);
                case FailureResults.GraphCycleDetected graphCycleDetected -> Result.fail(graphCycleDetected);
            };
            case Result.Success<Self, ChildAdditionFailure> _ -> switch (sComp.disconnectParents(graph, p -> p != this)) { // Disconnect the previous parents
                case Result.Failure<Void, Set<ParentDisconnectionFailure>>(var f) -> {
                    for (var failure : f) {
                        yield switch (failure) {
                            case FailureResults.EdgeNotPresent edgeNotPresent -> Result.fail(edgeNotPresent);
                            case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> Result.fail(rejectedByGraphPolicy);
                            case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> Result.fail(rejectedByGraphValidation);
                            case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> Result.fail(rejectedByVertexPolicy);
                            case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> Result.fail(rejectedByVertexValidation);
                            case FailureResults.VertexNotPresent vertexNotPresent -> Result.fail(vertexNotPresent);
                            case FailureResults.SelfReference selfReference -> Result.fail(selfReference);
                        };
                    }
                    throw new IllegalStateException("Failure state, but no failure found");
                }
                case Result.Success<Void, ?> _ -> Result.success(sComp);
            };
        };
    }

    /// Adds a computation containing the given updated object as a child of this computation.
    ///
    /// If the graph already contains a computation with the given updated object, no new computation is created.
    /// The computation is added as a child of this computation and the previous parents of the computation are not
    /// removed.
    ///
    /// If the graph does not contain a computation with the given updated object, a new computation is created.
    ///
    /// @param updated The updated object that will execute after this computation.
    /// @param updater The updater that contains the graph.
    /// @return A [Result] containing the computation that was added as a child, or a failure if the computation was not
    /// added.
    public Result<Self, ChildAdditionFailure> addAfter(Upd updated, Upr updater) {
        Objects.requireNonNull(updated);
        Self sComp = findOrNewComputation(updated, updater);
        return addChild(sComp, updater.graph());
    }


    public Result<Self, ParentAdditionFailure> setBefore(Upd updated, Upr updater) {
        Objects.requireNonNull(updated);
        Objects.requireNonNull(updater);

        Self sComp = findOrNewComputation(updated, updater);
        var graph = updater.graph();

        return switch (addParent(sComp, graph)) {
            case Result.Failure<Self, ParentAdditionFailure>(var f) -> switch (f) { // TODO
                case FailureResults.EdgeAlreadyExists edgeAlreadyExists -> null;
                case FailureResults.GraphCycleDetected graphCycleDetected -> null;
                case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> null;
                case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> null;
                case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> null;
                case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> null;
                case FailureResults.SelfReference selfReference -> null;
                case FailureResults.VertexAlreadyPresent vertexAlreadyPresent -> null;
                case FailureResults.VertexNotPresent vertexNotPresent -> null;
            };
            case Result.Success<Self, ParentAdditionFailure> v -> switch (sComp.disconnectChildren(graph, c -> c != this)) {
                case Result.Failure<Void, Set<ChildDisconnectionFailure>>(var f) -> { // TODO
                    for (var failure : f) {
                        yield switch (failure) {
                            case FailureResults.EdgeNotPresent edgeNotPresent -> null;
                            case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> null;
                            case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> null;
                            case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> null;
                            case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> null;
                            case FailureResults.VertexNotPresent vertexNotPresent -> null;
                            case FailureResults.SelfReference selfReference -> null;
                        };
                    }
                    throw new IllegalStateException("Failure state, but no failure found");
                }
                case Result.Success<Void, ?> _ -> Result.success(sComp);
            };
        };
    }

    /**
     * This method is used to add a computation as a parent of this computation.
     * It first checks if the updated object is null, and if it is, it throws a NullPointerException.
     * Then, it tries to find a computation in the graph that matches the given updated object.
     * If no such computation is found, a new computation is created with the given updated object.
     * The found or created computation is then added as a parent of this computation.
     * If the parent was added successfully, the method returns an Optional containing the added parent.
     * If the parent was not added (because it was already a parent), the method returns an empty Optional.
     *
     * @param updated The updated object to add as a parent of this computation.
     * @return An Optional containing the computation that was added as a parent, or an empty Optional if the computation was not added.
     */
    public Result<Self, ParentAdditionFailure> addBefore(Upd updated, Upr updater) {
        Objects.requireNonNull(updated);
        Objects.requireNonNull(updater);

        Self sComp = findOrNewComputation(updated, updater);

        return addParent(sComp, updater.graph());
    }

    public Result<Void, Void> setParallel(Upd updated, Upr updater) {
        Objects.requireNonNull(updated);
        Objects.requireNonNull(updater);

        Self sComp = findOrNewComputation(updated, updater);
        var graph = updater.graph();

        return null;

        // TODO
        /*return switch (addSibling(sComp, graph)) {
            case Result.Failure<Self, SiblingAdditionFailure>(var f) -> switch (f) {
            };

            case Result.Success<Self, SiblingAdditionFailure> v -> switch (sComp.disconnectParents(graph, p -> p != this)) {
                case Result.Failure<Void, Set<ParentDisconnectionFailure>>(var f) -> {
                    for (var failure : f) {
                        yield switch (failure) {
                            case FailureResults.EdgeNotPresent edgeNotPresent -> null;
                            case FailureResults.RejectedByGraphPolicy rejectedByGraphPolicy -> null;
                            case FailureResults.RejectedByGraphValidation rejectedByGraphValidation -> null;
                            case FailureResults.RejectedByVertexPolicy rejectedByVertexPolicy -> null;
                            case FailureResults.RejectedByVertexValidation rejectedByVertexValidation -> null;
                            case FailureResults.VertexNotPresent vertexNotPresent -> null;
                            case FailureResults.SelfReference selfReference -> null;
                        };
                    }
                    throw new IllegalStateException("Failure state, but no failure found");
                }
                case Result.Success<Void, ?> _ -> Result.success(null);
            };
        };*/
    }

    /**
     * @return A set containing the updated objects that are children of this UpdaterComputation.
     */
    public Set<Upd> updatedChildren(Upr updater) {
        return updater.graph().childrenOf(self())
                .stream()
                .map(StructuredComputation::updated)
                .collect(Collectors.toSet());
    }

    /**
     * @return A set containing the Updated objects that are parents of this UpdaterComputation.
     */
    public Set<Upd> updatedParents(Upr updater) {
        return updater.graph().parentsOf(self())
                .stream()
                .map(StructuredComputation::updated)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @return A set containing the Updated objects that are descendants of this UpdaterComputation.
     */
    public Set<Upd> updatedDescendants(Upr updater) {
        return updater.graph().descendantsOf(self())
                .stream()
                .map(StructuredComputation::updated)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @return A set containing the Updated objects that are ancestors of this UpdaterComputation.
     */
    public Set<Upd> updatedAncestors(Upr updater) {
        return updater.graph().ancestorsOf(self())
                .stream()
                .map(StructuredComputation::updated)
                .collect(Collectors.toSet());
    }

    /**
     * Returns whether the given Updated object is going to be executed at some point after this UpdaterComputation finishes.
     * @param updated The Updated object to check.
     * @return Whether the given Updated object is contained in a descendant of this UpdaterComputation.
     */
    public final boolean isUpdatedAfter(Upd updated, Upr updater) {
        return updatedDescendants(updater).contains(updated);
    }

    public final boolean isUpdatedBefore(Upd updated, Upr updater) {
        return updatedAncestors(updater).contains(updated);
    }

    public final boolean isChildComputation(Upd updated, Upr updater) {
        return updatedChildren(updater).contains(updated);
    }

    public final boolean isParentComputation(Upd updated, Upr updater) {
        return updatedParents(updater).contains(updated);
    }

    /**
     * Returns whether the given Updated object is contained in a sibling of this UpdaterComputation, and therefore will
     * be executed at the same time as this UpdaterComputation.
     * @param updated The Updated object to check.
     * @return Whether the given Updated object is contained in a sibling of this UpdaterComputation.
     */
    public boolean isUpdatedParallel(Upd updated, Upr updater) {
        return updater.graph().fullSiblingsOf(self())
                .stream()
                .map(this.computationClass()::cast)
                .anyMatch(c -> c.updated() == updated);
    }

    protected void onChildComputeStarts(Self child, Upr updater) {

    }

    protected void onParentComputeStarts(Self parent, Upr updater) {

    }

    protected void onChildComputeFinished(Self child, Upr updater) {

    }

    protected void onParentComputeFinished(Self parent, Upr updater) {

    }

    /**
     * Checks if this UpdaterComputation is equivalent to the given UpdaterComputation. Two UpdaterComputation objects
     * are equivalent if they have the same Updater class and the same Updated class.
     * @param other The UpdaterComputation to check
     * @return True if this UpdaterComputation is equivalent to the given UpdaterComputation, false otherwise
     */
    public final <SC extends StructuredComputation<Upr, Upd, Self>> boolean isEquivalent(SC other) { // FIXME: ?
        return this.updaterClass().equals(other.updaterClass()) && this.updatedClass().equals(other.updatedClass());
    }

    @Override
    protected void onConnectChild(Self child, Graph<?, ApplicationEdge> graph) throws RuntimeException {
        super.onConnectChild(child, graph);

    }

    @Override
    protected void onDisconnectChild(Self child, Graph<?, ? extends ApplicationEdge> graph) throws RuntimeException {
        super.onDisconnectChild(child, graph);
    }

    @Override
    protected void onConnectParent(Self parent, Graph<?, ApplicationEdge> graph) throws RuntimeException {
        super.onConnectParent(parent, graph);
        previousComputations.put(parent, false);
    }

    @Override
    protected void onDisconnectParent(Self parent, Graph<?, ? extends ApplicationEdge> graph) throws RuntimeException {
        super.onDisconnectParent(parent, graph);
        previousComputations.remove(parent);
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        StructuredComputation<?, ?, ?> that = (StructuredComputation<?, ?, ?>) obj;
        return Objects.equals(updated, that.updated) && updaterClass.equals(that.updaterClass);
    }
}
