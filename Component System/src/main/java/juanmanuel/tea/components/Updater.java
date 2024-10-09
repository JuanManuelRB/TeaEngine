package juanmanuel.tea.components;

import juanmanuel.tea.graph.ApplicationEdge;
import juanmanuel.tea.graph.Graph;
import org.jspecify.annotations.NullMarked;

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
}
