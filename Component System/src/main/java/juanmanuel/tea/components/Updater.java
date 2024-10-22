package juanmanuel.tea.components;

import juanmanuel.tea.graph.ApplicationEdge;
import juanmanuel.tea.graph.Graph;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;

/// Holds the logic to update an object contained in a computation. And organizes the computations of the updated objects
/// in a graph.
/// @param <Self> The updater class
/// @param <Upd> The updated class
/// @param <SC> The structured computation class
@NullMarked
public interface Updater<
        Self extends Updater<Self, Upd, SC>,
        Upd extends Updated,
        SC extends StructuredComputation<Self, Upd, SC>> {
    Class<Self> updaterClass();
    Class<Upd> updatedClass();
    Class<SC> computationClass();

    /// Updates the computation. Does not notify the children. Does not update on cascade.
    ///
    /// Equivalent to `updater.update(updated)` where updater is this updater and updated is the updated object
    /// contained in the computation.
    /// @param updaterComputation The computation to update
    default void update(SC updaterComputation) {
        update(updaterComputation.updated());
    }

    /// Updates the object
    /// @param updated The object to update
    void update(Upd updated);

    /// Gets the computation of the updated object from the graph or creates a new one if it does not exist.
    ///
    /// If the computation does not exist, it is not added to the graph.
    ///
    /// @param updated The updated object
    /// @return The computation of the updated object
    SC computationOf(Upd updated);

    SC createComputation(Upd updated);

    /// Associated graph that contains the computations of the updated objects
    /// @return The graph
    Graph<SC, ApplicationEdge> graph();

    /// Checks if the graph contains the computation of the updated object
    /// @param updated The updated object
    /// @return True if the graph contains the computation
    default boolean contains(Upd updated) {
        return graph().vertexSet().stream().anyMatch(computation -> computation.updated().equals(updated));
    }

    /**
     * Checks if the object matches the updater and therefore can be added and updated by it
     * @param object The object to check
     * @return True if the object matches the updater
     */
    default <O> boolean matches(O object) {
        return updatedClass().isAssignableFrom(object.getClass());
    }

    /// Computes the computation and notifies the children and parents when the computation starts and finishes.
    /// @param computation The computation to compute
    /// @throws InterruptedException If the computation is interrupted
    default void compute(SC computation) throws InterruptedException {
        Objects.requireNonNull(computation);
        if (!graph().containsVertex(computation))
            throw new IllegalArgumentException("The computation is not part of this updater's graph.");

        onStartCompute(computation);
        update(computation);
        onFinishCompute(computation);
    }

    /// Computes the computation if the previous computations have notified that they have finished.
    /// @param computation The computation to compute
    /// @throws InterruptedException If the computation is interrupted
    default void computeIfReady(SC computation) throws InterruptedException {
        if (computation.previousComputations().values().stream().allMatch(b -> b)) compute(computation);
    }

    /// Resets the state of the previous computations and notifies the children and parents that the computation has started.
    /// @param computation The computation that has started
    /// @throws InterruptedException
    @SuppressWarnings("unchecked")
    default void onStartCompute(SC computation) throws InterruptedException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Reset the previous computations
            for (var previousComputation : computation.previousComputations().entrySet())
                scope.fork(() -> {
                    previousComputation.setValue(false);
                    return null;
                });

            // Notify the children that the computation has started
            for (var child : graph().childrenOf(computation))
                scope.fork(() -> {
                    child.previousComputations().computeIfPresent(computation, (_, _) -> false);
                    child.onParentComputeStarts(computation, (Self) this);
                    return null;
                });

            // Notify the parents that the computation has started
            for (var parent : graph().parentsOf(computation))
                scope.fork(() -> {
                    parent.onChildComputeStarts(computation, (Self) this);
                    return null;
                });
            scope.join();
        }
    }

    /// Notifies the children that the computation has finished and notifies the parents that the computation has finished.
    /// @param computation The computation that has finished
    /// @throws InterruptedException
    @SuppressWarnings("unchecked")
    default void onFinishCompute(SC computation) throws InterruptedException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Notify the children that the computation has finished
            for (var child : graph().childrenOf(computation))
                scope.fork(() -> {
                    child.previousComputations().computeIfPresent(computation, (_, _) -> true);
                    child.onParentComputeFinished(computation, (Self) this);
                    Thread.ofVirtual().start(() -> {
                        try {
                            computeIfReady(child);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return null;
                });

            // Notify the parents that the computation has finished
            for (var parent : graph().parentsOf(computation))
                scope.fork(() -> {
                    parent.onChildComputeFinished(computation, (Self) this);
                    return null;
                });
            scope.join();
        }
    }

    default void start() {
        try (ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1)) {
            executor.scheduleAtFixedRate(() -> {
                try (StructuredTaskScope.ShutdownOnFailure scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    for (SC source : graph().roots()) {
                        scope.fork(() -> {
                            compute(source);
                            return null;
                        });
                    }
                    scope.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, 0, 1000 / 60, TimeUnit.MILLISECONDS);
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS); // TODO: Implement a way to stop the computation
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
