//package aplication.update;
//
//import juanmanuel.tea.components.GameObject;
//
//import javax.management.Notification;
//import java.util.*;
//
///**
// * This class represents a structure of updated objects that should be updated.
// * Sibling objects should be updated in parallel and tail objects should be updated after their head objects finish
// * updating.
// *
// * @param <T> the type of the updated objects
// * @param <U> the type of the updated objects
// */
//public final class UpdaterComputation<T extends Updater<T, U>, U extends Updated> extends GameObject {
//    private final U updated;
//    private final Map<UpdaterComputation<T, U>, Boolean> finished = new HashMap<>(); // Filled on enterParent
//
//    public UpdaterComputation(U updated) {
//
//        this.updated = updated;
//    }
//
//    public U updated() {
//        return updated;
//    }
//
//    /**
//     * Notifies the object of the given notification.
//     * @param notification the notification that is received
//     * @param <N> the type of the notification
//     */
//    @Override
//    @SuppressWarnings("unchecked")
//    public <N extends Notification> void notify(N notification) {
//        super.notify(notification);
//
//        if (notification instanceof ComputeNotification computeNotification)
//            processComputeNotification(computeNotification);
//    }
//
//    /**
//     * Processes the given compute notification.
//     *
//     * @param computeNotification the compute notification to process
//     */
//    @SuppressWarnings("unchecked")
//    private void processComputeNotification(ComputeNotification<T, U> computeNotification) {
//        switch (computeNotification) {
//            case ComputeNotification.ComputeStartedNotification<T, U> computeStartedNotification
//                    when parents().isEmpty() && computeNotification.getSource() == this && computeNotification.getSource() instanceof UpdaterComputation<?,?> up && up.updated == null -> {
//                for (GameObject tail : children()) {
//                    Thread.ofVirtual().start(() -> {
//                        tail.notify(
//                                new ComputeNotification.ComputeFinishedNotification<>(
//                                        this,
//                                        (Updater<T, U>) computeNotification.updater()
//                                )
//                        );
//                    });
//                }
//
//            }
//
//            case ComputeNotification.ComputeFinishedNotification<T, U> computeFinishedNotification -> {
//                finished.put((UpdaterComputation<T, U>) computeFinishedNotification.getSource(), true);
//                if (finished.values().stream().allMatch(b -> b))
//                    compute((Updater<T, U>) computeFinishedNotification.updater());
//            }
//
//            default -> {
//
//            }
//        }
//    }
//
//    /**
//     * Starts the computation of the updated object.
//     * Notifies the children that the computation has started when the computation starts.
//     * Notifies the children that the computation has finished when the computation finishes.
//     * @param updater the updater to use to update the updated object
//     */
//    private void compute(Updater<T, U> updater) {
//        System.out.println("Compute started");
//        for (var tail : children())
//            Thread.ofVirtual().start(() -> tail.notify(new ComputeNotification.ComputeStartedNotification<>(this, updater)));
//
//        System.out.println("Compute");
//        // Run updater.update(updated) and wait for it to finish
//        updater.update(updated);
//
//        System.out.println("Compute finished");
//        for (var tail : children())
//            Thread.ofVirtual().start(() -> tail.notify(new ComputeNotification.ComputeFinishedNotification<>(this, updater)));
//    }
//
//    /**
//     * Sets the given updated object to be updated after this updated object.
//     * Removes any previous way of updating the updated object.
//     * To add without removing any previous way of updating the updated object, use {@link #addChild(GameObject)}
//     *
//     * @param updaterComputation the updated object to update after this updated object
//     * @return the given updater computation
//     */
//    public UpdaterComputation<T, U> andThen(UpdaterComputation<T, U> updaterComputation) {
//        for (GameObject head : updaterComputation.parents())
//            updaterComputation.removeParent(head);
//
//        addChild(updaterComputation);
//        return updaterComputation;
//    }
//
//    /**
//     * Sets the given updated objects to be updated after this updated object.
//     * Removes any previous way of updating the updated object.
//     * To add without removing any previous way of updating the updated object, use {@link #addChild(GameObject)}
//     *
//     * @param updaterComputations the updated objects to update after this updated object
//     * @return the given updater computations
//     */
//    @SafeVarargs
//    public final UpdaterComputation<T, U>[] andThen(UpdaterComputation<T, U>... updaterComputations) {
//        for (UpdaterComputation<T, U> updaterComputation : updaterComputations) {
//            for (GameObject head : updaterComputation.parents())
//                updaterComputation.removeParent(head);
//
//            addChild(updaterComputation);
//        }
//
//        return updaterComputations;
//    }
//
//    /**
//     * Sets the given updated object to be updated parallel to this updated object.
//     * Removes any previous way of updating the updated object.
//     * To add without removing any previous way of updating the updated object, use {@link #addSibling(GameObject)}
//     *
//     * @param updaterComputation the updated object to update parallel to this updated object
//     * @return the given updater computation
//     */
//    public UpdaterComputation<T, U> setParallel(UpdaterComputation<T, U> updaterComputation) {
//        for (GameObject head : updaterComputation.parents())
//            updaterComputation.removeParent(head);
//
//        addSibling(updaterComputation);
//        return updaterComputation;
//    }
//
//    /**
//     * Sets the given updated objects to be updated parallel to this updated object.
//     * Removes any previous way of updating the updated object.
//     * To add without removing any previous way of updating the updated object, use {@link #addSibling(GameObject)}
//     *
//     * @param updaterComputations the updated objects to update parallel to this updated object
//     * @return the given updater computations
//     */
//    @SafeVarargs
//    public final UpdaterComputation<T, U>[] setParallel(UpdaterComputation<T, U>... updaterComputations) {
//        for (UpdaterComputation<T, U> updaterComputation : updaterComputations) {
//            for (GameObject head : updaterComputation.parents())
//                updaterComputation.removeParent(head);
//
//            addSibling(updaterComputation);
//        }
//
//        return updaterComputations;
//    }
//
//    @Override
//    protected void enterChild(GameObject tail) {
//        if (!(tail instanceof UpdaterComputation))
//            removeChild(tail);
//
//        System.out.println(STR."\{tail} enters as tail of \{this}");
//    }
//
//    @Override
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    protected void enterParent(GameObject head) {
//        if (head instanceof UpdaterComputation uc)
//            finished.put(uc, false);
//
//        removeParent(head);
//    }
//
//    @Override
//    public String toString() {
//        return STR."""
//                Next in sequence: {
//                    \{ children().stream().map(tail -> STR."| \{tail}\n").reduce(Objects::toString).orElse("") }
//                }""";
//    }
//
//    public static sealed abstract class ComputeNotification<T extends Updater<T, U>, U extends Updated> extends Notification {
//        private final Updater<?, ?> updater;
//
//        public ComputeNotification(String type, Object source, long sequenceNumber, Updater<?, ?> updater) {
//            super(type, source, sequenceNumber);
//            this.updater = updater;
//        }
//
//        public ComputeNotification(String type, Object source, long sequenceNumber, String message, Updater<?, ?> updater) {
//            super(type, source, sequenceNumber, message);
//            this.updater = updater;
//        }
//
//        public ComputeNotification(String type, Object source, long sequenceNumber, long timeStamp, Updater<?, ?> updater) {
//            super(type, source, sequenceNumber, timeStamp);
//            this.updater = updater;
//        }
//
//        public ComputeNotification(String type, Object source, long sequenceNumber, long timeStamp, String message, Updater<?, ?> updater) {
//            super(type, source, sequenceNumber, timeStamp, message);
//            this.updater = updater;
//        }
//
//        public Updater<?, ?> updater() {
//            return updater;
//        }
//
//        public static final class ComputeFinishedNotification<T extends Updater<T, U>, U extends Updated> extends ComputeNotification<T, U> {
//            public ComputeFinishedNotification(UpdaterComputation<T, U> updaterComputation, Updater<T, U> updater) {
//                super("ComputeFinishedNotification", updaterComputation, 0, System.nanoTime(), "Compute finished", updater);
//
//            }
//        }
//
//        public static final class ComputeStartedNotification<T extends Updater<T, U>, U extends Updated> extends ComputeNotification<T, U> {
//            public ComputeStartedNotification(UpdaterComputation<T, U> updaterComputation, Updater<T, U> updater) {
//                super("ComputeStartedNotification", updaterComputation, 0, System.nanoTime(), "Compute started", updater);
//
//            }
//        }
//    }
//
//
//}
