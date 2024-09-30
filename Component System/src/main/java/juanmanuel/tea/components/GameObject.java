package juanmanuel.tea.components;

import juanmanuel.tea.graph.ApplicationEdge;
import juanmanuel.tea.graph.Graph;

import java.util.Objects;

/**
 * A GameObject is an object that can have children and parents, this allows for the creation of graphs of objects.
 */
public abstract class GameObject<GO extends GameObject<GO>> extends juanmanuel.tea.graph.Vertex<GO> {

//    /**
//     * Called when a child is added to this GameObject.
//     * @param child The child that was added.
//     */
//    @Override
//    protected void onConnectChild(GO child) {
//    }
//
//    /**
//     * Called when a child is removed from this GameObject.
//     * @param child The child that was removed.
//     */
//    @Override
//    protected void onDisconnectChild(GO child) {
//    }
//
//    /**
//     * Called when a parent is added to this GameObject.
//     * @param parent The parent that was added.
//     */
//    @Override
//    protected void onConnectParent(GO parent) {
//    }
//
//    /**
//     * Called when a parent is removed from this GameObject.
//     * @param parent The parent that was removed.
//     */
//    @Override
//    protected void onDisconnectParent(GO parent) {
//    }
//
//    /**
//     * Called when this GameObject enters a graph.
//     */
//    @Override
//    protected void onEnterGraph(ApplicationGraph<GO> graph) {
//    }
//
//    /**
//     * Called when this GameObject leaves a graph.
//     */
//    @Override
//    protected void onLeaveGraph(ApplicationGraph<GO> graph) {
//    }

    /**
     * Called when a parent is subscribed to a computation.
     * @param computation The computation to which the parent was subscribed.
     * @param parent The parent that was subscribed.
     * @param <Upr> The updater class.
     * @param <Upd> The updated class.
     * @param <SC> The computation class.
     */
    protected <Upr extends Updater<Upr, Upd, SC>,
            Upd extends Updated,
            SC extends StructuredComputation<Upr, Upd, SC>> void onParentSubscribe(SC computation, GO parent) {}

    /**
     * Called when a child is subscribed to a computation.
     * @param computation The computation to which the child was subscribed.
     * @param child The child that was subscribed.
     * @param <Upr> The updater class.
     * @param <Upd> The updated class.
     * @param <SC> The computation class.
     */
    protected <Upr extends Updater<Upr, Upd, SC>,
            Upd extends Updated,
            SC extends StructuredComputation<Upr, Upd, SC>> void onChildSubscribe(SC computation, GO child) {}

    /**
     * Notifies the parents and children of this GameObject that this GameObject has been subscribed to a computation.
     * This method is called when this GameObject is subscribed to a computation.
     * @param computation The computation to which this GameObject has been subscribed.
     * @param <Upr> The updater class.
     * @param <Upd> The updated class.
     * @param <SC> The computation class.
     * @throws RuntimeException If the computation throws an exception.
     */
    @SuppressWarnings("unchecked")
    protected <Upr extends Updater<Upr, Upd, SC>,
            Upd extends Updated,
            SC extends StructuredComputation<Upr, Upd, SC>> void onSubscribe(SC computation, Graph<SC, ? extends ApplicationEdge> graph) throws RuntimeException {

//        // Notify parents and children of the subscription
//        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
//            for (var parent : parentsIn()) { // TODO to be able to implement this, game objects need to track the graph they are in
//                scope.fork(() -> {
//                    parent.onChildSubscribe(computation, (GO) this);
//                    return null;
//                });
//            }
//            for (var child : children()) {
//                scope.fork(() -> {
//                    child.onParentSubscribe(computation, (GO) this);
//                    return null;
//                });
//            }
//            scope.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * Returns whether this GameObject is updated in a computation.
     * @param updater
     * @return
     * @param <Upr>
     * @param <Upd>
     * @param <SC>
     */
    public <Upr extends Updater<Upr, Upd, SC>,
            Upd extends Updated,
            SC extends StructuredComputation<Upr, Upd, SC>> boolean isUpdatedIn(Upr updater) {
        Objects.requireNonNull(updater);
        try {
            return updater.matches(this) && updater.contains((Upd) this);
        } catch (ClassCastException _) {
            return false;
        }
    }

    public <Upr extends Updater<Upr, Upd, SC>,
            Upd extends Updated,
            SC extends StructuredComputation<Upr, Upd, SC>> boolean isUpdatedBy(SC computation) {
        Objects.requireNonNull(computation);
        return this.equals(computation.updated());
    }

    /**
     * Returns whether this GameObject is updated after a computation.
     * @param computation
     * @return
     * @param <Upr>
     * @param <Upd>
     * @param <SC>
     */
    public <Upr extends Updater<Upr, Upd, SC>,
            Upd extends Updated,
            SC extends StructuredComputation<Upr, Upd, SC>> boolean isUpdatedAfter(SC computation) {
        Objects.requireNonNull(computation);
        if (!computation.updatedClass().isAssignableFrom(this.getClass()))
            return false;

//        return computation.findSuccessorComputation(computation.updatedClass().cast(this)).isPresent();
        return false;
    }

    /**
     * Returns whether this GameObject is updated before a computation.
     * @param computation
     * @return
     * @param <Upr>
     * @param <Upd>
     * @param <SC>
     */
    public <Upr extends Updater<Upr, Upd, SC>,
            Upd extends Updated,
            SC extends StructuredComputation<Upr, Upd, SC>> boolean isUpdatedBefore(SC computation) {
        Objects.requireNonNull(computation);
        computation.updatedClass().isAssignableFrom(this.getClass());

//        return computation.findPredecessorComputation(computation.updatedClass().cast(this)).isPresent();
        return false;
    }
}












