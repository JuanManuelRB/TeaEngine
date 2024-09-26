package juanmanuel.tea.components;

import juanmanuel.tea.graph.ApplicationGraph;
import juanmanuel.tea.graph.ApplicationVertex;
import juanmanuel.tea.graph.Graph;
import juanmanuel.tea.graph.operation_failures.FailureResults;
import juanmanuel.tea.graph.validation.VertexOperationValidator;
import juanmanuel.tea.utils.Result;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents a computation that can be executed by an Updater. It is a graph of UpdaterComputation objects.
 * @param <Upr> The type of the Updater that can execute the updated object.
 * @param <Upd> The type of the Updated object that is computed.
 * @param <Self> The type of the UpdaterComputation.
 */
public abstract class StructuredComputation <
        Upr extends Updater<Upr, Upd, Self>,
        Upd extends Updated,
        Self extends StructuredComputation<Upr, Upd, Self>
        > extends ApplicationVertex<Self> {
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

        validationsManager()
                .addOperationValidation(
                        VertexOperationValidator.VerticesOperationValidation.CONNECT_CHILD_VALIDATION,
                        other -> isEquivalent(other) ? Result.success(null) : Result.fail(null)
                );

        if (updated instanceof GameObject<?> go)
            go.onSubscribe(self());
    }

    public StructuredComputation(Class<Upr> updaterClass, Upd updated) {
        this(updaterClass, updated, 1);
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

    /**
     * This method is used to start the computation of this StructuredComputation's Updated object, with the given Updater.
     * It first calls the onStartCompute method, which prepares the computation and checks if it can be started.
     * If the onStartCompute method throws an InterruptedException and there are queued threads, the method returns without starting the computation. TBD
     * If the onStartCompute method throws an InterruptedException and there are no queued threads, the InterruptedException is rethrown.
     * If the onStartCompute method does not throw an InterruptedException, the computation is started by calling the computeBy method with the given Updater.
     * After the computation is finished, the onFinishCompute method is called to clean up and notify other computations that this computation has finished.
     *
     * @param updater The Updater object that is executing this StructuredComputation's computation.
     * @throws InterruptedException If the computation is interrupted while waiting for the semaphore to be released.
     */
    public final void startComputationBy(Upr updater) throws InterruptedException {
        // TODO: Add a way to cancel the computation
        // TODO: Add a way to check if the computation is already running

        semaphore.acquire();
        onStartCompute(updater);
        computeBy(updater);
        onFinishCompute(updater);
        semaphore.release();
    }

    /**
     * Realizes the computation of this StructuredComputation's Updated, with the given Updater.
     * @param updater The Updater object that is executing this StructuredComputation's computation.
     */
    public void computeBy(Upr updater) {
        updater.update(updated);
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
            for (Self child : updater.graph().getChildren(self()))
                scope.fork(() -> {
                    child.onParentComputeStarts(self(), updater);
                    return null;
                });

            // Notify the parents that the computation has started
            for (Self parent : updater.graph().getParents(self()))
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
            for (var child : updater.graph().getChildren(self()))
                scope.fork(() -> {
                    child.onParentComputeFinished(self(), updater);
                    return null;
                });

            // Notify the parents that the computation has finished
            for (var parent : updater.graph().getParents(self()))
                scope.fork(() -> {
                    parent.onChildComputeFinished(self(), updater);
                    return null;
                });
            scope.join();
        }
    }

    // TODO: Create proper failure types for StructuredComputation instead of reusing the ones from the graph package
    // RejectedByGraph, RejectedByVertex, ComputationNotPresent, ComputationAlreadyPresent, SelfReference, ComputationNotConnected, ComputationAlreadyConnected
    public sealed interface SetAfterFailure extends FailureResults permits FailureResults.EdgeAlreadyExists, FailureResults.EdgeNotPresent, FailureResults.GraphCycleDetected, FailureResults.RejectedByGraphPolicy, FailureResults.RejectedByGraphValidation, FailureResults.RejectedByVertexPolicy, FailureResults.RejectedByVertexValidation, FailureResults.SelfReference, FailureResults.VertexAlreadyPresent, FailureResults.VertexNotPresent {}

    /// This method is used to set a computation as a child of this computation.
    ///
    /// It first checks if the updated object is null, and if it is, it throws a NullPointerException.
    /// Then, it tries to find a computation in the graph that matches the given updated object.
    ///
    /// If no such computation is found, a new computation is created with the given updated object.
    ///
    /// If the found or created computation is the same as this computation or is already a child of this computation,
    /// the method returns a failure result.
    /// Otherwise, it removes the found or created computation from its current parents and adds it as a child of this computation.
    ///
    /// If the child was added successfully, the method returns a success result.
    ///
    /// @param updated The updated object to add as a child of this computation.
    /// @return A Result object containing the computation that was added as a child, or a failure if the computation was not added.
    /// @throws NullPointerException If the updated object is null or the updater is null.
    public Result<Self, SetAfterFailure> setAfter(Upd updated, Upr updater) throws NullPointerException {
        Objects.requireNonNull(updated);
        Objects.requireNonNull(updater);

        Self sComp = findOrNewComputation(updated, updater);
        var graph = updater.graph();

        return switch (addChild(sComp, graph)) {
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
            case Result.Success<Self, ChildAdditionFailure> _ -> switch (sComp.disconnectParents(graph, p -> p != this)) {
                case Result.Failure<Self, Set<ParentDisconnectionFailure>>(var f) -> {
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

                    throw new IllegalStateException("Unreachable code");

                }
                case Result.Success<Self, ?>(var v) -> Result.success(v);
            };
        };
    }

    /**
     * This method is used to add a computation as a child of this computation.
     * It first checks if the updated object is null, and if it is, it throws a NullPointerException.
     * Then, it tries to find a computation in the graph that matches the given updated object.
     * If no such computation is found, a new computation is created with the given updated object.
     * The found or created computation is then added as a child of this computation.
     * If the child was added successfully, the method returns a Result object containing the added child.
     * If the child was not added (because it was already a child), the method returns a failure Result.
     *
     * @param updated The updated object to add as a child of this computation.
     * @return A Result object containing the computation that was added as a child, or a failure if the computation was not added.
     */
    public Result<Self, ChildAdditionFailure> addAfter(Upd updated) {
        Objects.requireNonNull(updated);

        Self sComp = findOrNewComputation(updated);

        return addChild(sComp);
    }

    /**
     * This method is used to set a computation as a parent of this computation.
     * It first checks if the updated object is null, and if it is, it throws a NullPointerException.
     * Then, it tries to find a computation in the graph that matches the given updated object.
     * If no such computation is found, a new computation is created with the given updated object.
     * If the found or created computation is the same as this computation or is already a parent of this computation, the method returns an empty Optional.
     * Otherwise, it removes the found or created computation from its current children and adds it as a parent of this computation.
     * If the parent was added successfully, the method returns an Optional containing the added parent.
     * If the parent was not added (because it was already a parent), the method returns an empty Optional.
     *
     * @param updated The updated object to add as a parent of this computation.
     * @return An Optional containing the computation that was added as a parent, or an empty Optional if the computation was not added.
     */
    public Result<Self, ParentAdditionFailure> setBefore(Upd updated) {
        Objects.requireNonNull(updated);
        assert graph().isPresent();

        Self sComp = findOrNewComputation(updated);

        var res = addParent(sComp);
        if (res.isFailure()) {
            graph().get().removeVertex(sComp);
            return res;
        }

        for (var child : sComp.children()) {
            if (child == this) continue;
            graph().get().removeEdge(sComp, child);
        }

        return res;
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
    public Result<Self, ParentAdditionFailure> addBefore(Upd updated) {
        Objects.requireNonNull(updated);

        Self sComp = findOrNewComputation(updated);

        return addParent(sComp);
    }

    public Result<Void, Void> setParallel(Upd updated) {
        Objects.requireNonNull(updated);

//        Self sComp = findOrNewComputation(updated);
        // var sComp = switch(findComputation(updated)) {
        //     case Success<Self, ?> s -> s.value();
        //     case Failure<?, NoComputationPresent> f -> newComputationOf(updated);
        Optional<Self> osc = findComputation(updated);
        Self sComp;
        if (osc.isPresent()) {
            sComp = osc.get();
            if (osc.get() == this)
                return null; // TODO: Change Result type

            for (var parent : osc.get().parents())
                parent.removeChild(sComp);

            for (var parent : this.parents())
                parent.addChild(sComp);

//        return addSibling(sComp)TODO
        }

        throw new UnsupportedOperationException();
    }

    public Optional<Self> addParallel(Upd updated) {
        Objects.requireNonNull(updated);

        Self sComp = findOrNewComputation(updated);

        if (sComp == this)
            return Optional.empty();

//        return addSibling(sComp); TODO
        throw new UnsupportedOperationException();
    }

    /**
     * @return A set containing the updated objects that are children of this UpdaterComputation.
     */
    public Set<Upd> updatedChildren() {
        return children().stream()
                .map(StructuredComputation::updated)
                .collect(Collectors.toSet());
    }

    /**
     * @return A set containing the Updated objects that are parents of this UpdaterComputation.
     */
    public Set<Upd> updatedParents() {
        return parents().stream()
                .map(StructuredComputation::updated)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @return A set containing the Updated objects that are descendants of this UpdaterComputation.
     */
    public Set<Upd> updatedDescendants() {
        return descendants().stream()
                .map(StructuredComputation::updated)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @return A set containing the Updated objects that are ancestors of this UpdaterComputation.
     */
    public Set<Upd> updatedAncestors() {
        return ancestors().stream()
                .map(StructuredComputation::updated)
                .collect(Collectors.toSet());
    }

    public Set<Upd> updatedSet() {
        if (graph().isEmpty())
            return Set.of(updated());

        return graph().get().vertexSet().stream()
                .map(StructuredComputation::updated)
                .collect(Collectors.toSet());
    }

    /**
     * Removes the UpdaterComputation that contains the given Updated object as a descendant of this UpdaterComputation.
     * @param updated The Updated object to remove.
     * @return An Optional containing the UpdaterComputation that contains the given Updated object if it was removed,
     * an empty Optional otherwise. If there is no graph, an empty Optional is returned.
     */
    public Result<Self, Void> removeComputationOf(Upd updated) throws Throwable { // TODO: result failure RemoveComputationResult
        Objects.requireNonNull(updated);

        if (graph().isEmpty())
            return null;

        var opC = findComputation(updated);

        if (opC.isEmpty())
            return null;

        return switch (opC.get().removeFromGraph()) {
            case Result.Failure<?, RemoveFailure> v -> null;
            case Result.Success<Self, ?>(var v) -> Result.success(v);
        };
    }

    /**
     * Returns whether the given Updated object is contained in a descendant of this UpdaterComputation, and therefore
     * will be executed after this UpdaterComputation finishes.
     * @param updated The Updated object to check.
     * @return Whether the given Updated object is contained in a descendant of this UpdaterComputation.
     */
    public final boolean graphUpdates(Upd updated) {
        return updated == this.updated || children()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(child -> child.graphUpdates(updated));
    }

    /**
     * Returns whether the given Updated object is going to be executed at some point after this UpdaterComputation finishes.
     * @param updated The Updated object to check.
     * @return Whether the given Updated object is contained in a descendant of this UpdaterComputation.
     */
    public final boolean isUpdatedAfter(Upd updated) {
        return updatedDescendants().contains(updated);
    }

    public final boolean isUpdatedBefore(Upd updated) {
        return updatedAncestors().contains(updated);
    }

    public final boolean isChildComputation(Upd updated) {
        return updatedChildren().contains(updated);
    }

    public final boolean isParentComputation(Upd updated) {
        return updatedParents().contains(updated);
    }

    /**
     * Returns whether the given Updated object is contained in a sibling of this UpdaterComputation, and therefore will
     * be executed at the same time as this UpdaterComputation.
     * @param updated The Updated object to check.
     * @return Whether the given Updated object is contained in a sibling of this UpdaterComputation.
     */
    public boolean isUpdatedParallel(Upd updated, Upr updater) {
        return fullSiblings(updater.graph()).stream()
                .map(this.computationClass()::cast)
                .anyMatch(c -> c.updated() == updated);
    }

    protected void onChildComputeStarts(Self child, Upr updater) {
        if (!this.hasChild(child, updater.graph()))
            throw new IllegalArgumentException("Child not found");
    }

    protected void onParentComputeStarts(Self parent, Upr updater) {
        if (!this.hasParent(parent, updater.graph()))
            throw new IllegalArgumentException("Parent not found");

        previousComputations.put(parent, false);
    }

    protected void onChildComputeFinished(Self child, Upr updater) {
        if (!this.hasChild(child, updater.graph()))
            throw new IllegalArgumentException("Child not found");
    }

    protected void onParentComputeFinished(Self parent, Upr updater) {
        if (!this.hasParent(parent, updater.graph()))
            throw new IllegalArgumentException("Parent not found");

        previousComputations.put(parent, true);
        startComputeIfReady(self(), updater);
    }

    protected final void startComputeIfReady(Self computation, Upr updater) {
        if (computation.previousComputations.values().stream().allMatch(Boolean::booleanValue)) {
            Thread.ofVirtual().start(() -> {
                try {
                    computation.startComputationBy(updater);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e); // TODO
                }
            });
        }
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
    protected void onConnectChild(Self child, ApplicationGraph<? super Self> graph) throws RuntimeException {
        Objects.requireNonNull(child);
        previousComputations.put(child, false);
    }

    @Override
    protected void onConnectParent(Self parent, ApplicationGraph<? super Self> graph) throws RuntimeException {
        Objects.requireNonNull(parent);
        previousComputations.put(parent, false);
    }

    @Override
    protected void onDisconnectChild(Self child, ApplicationGraph<? super Self> graph) throws RuntimeException {
        Objects.requireNonNull(child);
        child.previousComputations.remove(this);
    }

    @Override
    protected void onDisconnectParent(Self parent, ApplicationGraph<? super Self> graph) throws RuntimeException {
        Objects.requireNonNull(parent);
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

    public static class ComputationGraph<
            Upr extends Updater<Upr, Upd, SC>,
            Upd extends Updated,
            SC extends StructuredComputation<Upr, Upd, SC>> extends Graph<SC, DefaultWeightedEdge> {

        public ComputationGraph() {
            super(DefaultWeightedEdge.class);
        }
    }
}
