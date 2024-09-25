package juanmanuel.tea.components;

import juanmanuel.tea.graph.ApplicationGraph;

/**
 * Holds the logic to update an object contained in a computation. And organizes the computations of the updated objects
 * in a graph.
 * @param <Self> The updater class
 * @param <Upd> The updated class
 * @param <SC> The structured computation class
 */
public interface Updater<Self extends Updater<Self, Upd, SC>, Upd extends Updated, SC extends StructuredComputation<Self, Upd, SC>> {
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
    /// TODO: Adds the computation to the graph if it does not exist?
    /// @param updated The updated object
    /// @return The computation of the updated object
    SC computationOf(Upd updated);

    /// Associated graph that contains the computations of the updated objects
    /// @return The graph
    ApplicationGraph<SC> graph();

    /**
     * Checks if the object matches the updater and therefore can be added and updated by it
     * @param object The object to check
     * @return True if the object matches the updater
     */
    default <O> boolean matches(O object) {
        return updatedClass().isAssignableFrom(object.getClass());
    }
}
