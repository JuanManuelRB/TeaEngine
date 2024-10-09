package juanmanuel.tea.components;

@FunctionalInterface
public interface UpdatedUpdater<
        Self extends Updater<Self, Upd, SC>,
        Upd extends Updated,
        SC extends StructuredComputation<Self, Upd, SC>> extends Updated {

    /// Updates the computation. Does not notify the children. Does not update on cascade.
    ///
    /// Equivalent to `updater.update(updated)` where updater is this updater and updated is the updated object
    /// contained in the computation.
    ///
    /// @param updaterComputation The computation to update
    default void update(SC updaterComputation) {
        update(updaterComputation.updated());
    }

    /// Updates the object
    ///
    /// @param updated The object to update
    void update(Upd updated);
}
