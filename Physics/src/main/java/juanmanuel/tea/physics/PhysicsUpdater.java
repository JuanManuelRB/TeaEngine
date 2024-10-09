package juanmanuel.tea.physics;

import juanmanuel.tea.components.StructuredComputation;
import juanmanuel.tea.components.UpdatedUpdater;
import juanmanuel.tea.components.Updater;
import juanmanuel.tea.graph.ApplicationEdge;
import juanmanuel.tea.graph.Graph;
import juanmanuel.tea.graph.callbacks.VertexCallbackManager;
import juanmanuel.tea.graph.policy.GraphPolicy;
import juanmanuel.tea.graph.policy.VertexOperationsPolicies;
import juanmanuel.tea.graph.policy.VertexPolicy;
import juanmanuel.tea.graph.validation.VertexOperationValidator;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@NullMarked
public class PhysicsUpdater
        implements Updater<PhysicsUpdater, PhysicsUpdated, PhysicsUpdater.StructuredPhysicComputation>,
            UpdatedUpdater<PhysicsUpdater, PhysicsUpdated, PhysicsUpdater.StructuredPhysicComputation> {
    private final String name;
    private final Semaphore concurrentComputeSemaphore = new Semaphore(1);
    private final Graph<PhysicsUpdater.StructuredPhysicComputation, ApplicationEdge> computationGraph;
    private boolean running = false;

    public PhysicsUpdater() {
        this("");
    }

    public PhysicsUpdater(String name) {
        super();
        this.name = name;
        computationGraph = new Graph<>(ApplicationEdge.class);
    }


    @Override
    public Class<PhysicsUpdater> updaterClass() {
        return PhysicsUpdater.class;
    }

    @Override
    public Class<PhysicsUpdated> updatedClass() {
        return PhysicsUpdated.class;
    }

    @Override
    public Class<StructuredPhysicComputation> computationClass() {
        return StructuredPhysicComputation.class;
    }

    @Override
    public void update(PhysicsUpdater.StructuredPhysicComputation updaterComputation) {
        update(updaterComputation.updated());
    }

    @Override
    public final void update(PhysicsUpdated physicsUpdated) {
        physicsUpdated.updatePhysics();
    }

    @Override
    public StructuredPhysicComputation createComputation(PhysicsUpdated updated) {
        var c = new StructuredPhysicComputation(updated);
        graph().policiesManager().accept(GraphPolicy.VertexModificationGraphPolicy.ADD_VERTEX_POLICY, c);
        graph().policiesManager().accept(GraphPolicy.EdgeModificationGraphPolicy.CREATE_EDGE_POLICY, StructuredPhysicComputation.class, StructuredPhysicComputation.class);
        c.policiesManager().accept(VertexPolicy.GraphModificationVertexPolicy.ADD_TO_GRAPH_POLICY, graph());
        c.policiesManager().accept(VertexPolicy.EdgeModificationVertexPolicy.CONNECT_CHILD_POLICY, StructuredPhysicComputation.class);
        c.policiesManager().accept(VertexPolicy.EdgeModificationVertexPolicy.CONNECT_PARENT_POLICY, StructuredPhysicComputation.class);
        return c;
    }

    @Override
    public PhysicsUpdater.StructuredPhysicComputation computationOf(PhysicsUpdated updated) {
        return graph().vertexSet().stream()
                .filter(computation -> computation.updated().equals(updated))
                .findAny()
                .orElseGet(() -> createComputation(updated));
    }

    @Override
    public Graph<PhysicsUpdater.StructuredPhysicComputation, ApplicationEdge> graph() {
        return computationGraph;
    }

    private void compute(StructuredPhysicComputation computation) throws InterruptedException {
        Objects.requireNonNull(computation);
        if (!graph().containsVertex(computation))
            throw new IllegalArgumentException("The computation is not part of this updater's graph.");

        onStartCompute(computation);
        update(computation);
        onFinishCompute(computation);
    }

    private void computeIfReady(StructuredPhysicComputation computation) throws InterruptedException {
        if (computation.previousComputations().values().stream().allMatch(b -> b)) compute(computation);
    }

    protected void onStartCompute(StructuredPhysicComputation computation) throws InterruptedException {
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
                    child.onParentComputeStarts(computation, this);
                    return null;
                });

            // Notify the parents that the computation has started
            for (var parent : graph().parentsOf(computation))
                scope.fork(() -> {
                    parent.onChildComputeStarts(computation, this);
                    return null;
                });
            scope.join();
        }
    }

    protected void onFinishCompute(StructuredPhysicComputation computation) throws InterruptedException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Notify the children that the computation has finished
            for (var child : graph().childrenOf(computation))
                scope.fork(() -> {
                    child.previousComputations().computeIfPresent(computation, (_, _) -> true);
                    child.onParentComputeFinished(computation, this);
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
                    parent.onChildComputeFinished(computation, this);
                    return null;
                });
            scope.join();
        }
    }

    /**
     * Starts the cycle of computation.
     */
    public void start() {
        if (running)
            return;

        running = true;
//        AtomicLong start = new AtomicLong(System.nanoTime());
        try (ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1)) {
            executor.scheduleAtFixedRate(() -> {
                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
//                    var elapsed = System.nanoTime() - start.get();
//                    System.out.println("Start: " + elapsed);
//                    start.set(System.nanoTime());
                    for (PhysicsUpdater.StructuredPhysicComputation source : graph().roots()) {
                        scope.fork(() -> {
                            compute(source);
                            return null;
                        });
                    }
                    scope.join();
//                    elapsed = System.nanoTime() - start.get();
//                    System.out.println("End: " + elapsed);
//                    start.set(System.nanoTime());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, 0, 1000 / 60, TimeUnit.MILLISECONDS);
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS); // TODO: Implement a way to stop the computation
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if this updater can update the object
     * @param updated The object to check
     * @return True if this updater would update the object on one of its cycles
     */
    public boolean updates(PhysicsUpdated updated) {
        return graph().vertexSet().stream()
                .anyMatch(computation -> computation.updated().equals(updated));
    }

    @Override
    public String toString() {
        return STR."PhysicsUpdater{\{name}}";
    }

    @NullMarked
    public static class StructuredPhysicComputation extends StructuredComputation<PhysicsUpdater, PhysicsUpdated, StructuredPhysicComputation> {
        public StructuredPhysicComputation(PhysicsUpdated updated) {
            super(PhysicsUpdater.class, updated);
            //PhysicsUpdater.this.computationGraph.addVertex(this);
        }

        @Override
        protected Map<StructuredComputation<PhysicsUpdater, PhysicsUpdated, StructuredPhysicComputation>, Boolean> previousComputations() {
            return super.previousComputations();
        }

        @Override
        protected void onStartCompute(PhysicsUpdater updater) throws InterruptedException {
            super.onStartCompute(updater);
        }

        @Override
        protected void onFinishCompute(PhysicsUpdater updater) throws InterruptedException {
            super.onFinishCompute(updater);
        }

        @Override
        protected void onChildComputeStarts(StructuredPhysicComputation child, PhysicsUpdater updater) {
            super.onChildComputeStarts(child, updater);
        }

        @Override
        protected void onParentComputeStarts(StructuredPhysicComputation parent, PhysicsUpdater updater) {
            super.onParentComputeStarts(parent, updater);
        }

        @Override
        protected void onChildComputeFinished(StructuredPhysicComputation child, PhysicsUpdater updater) {
            super.onChildComputeFinished(child, updater);
        }

        @Override
        protected void onParentComputeFinished(StructuredPhysicComputation parent, PhysicsUpdater updater) {
            super.onParentComputeFinished(parent, updater);
        }

        @Override
        protected Supplier<StructuredPhysicComputation> computationSupplier(PhysicsUpdated updated) {
            Objects.requireNonNull(updated);

            return () -> new StructuredPhysicComputation(updated);
        }

        @Override
        protected VertexOperationsPolicies policiesManager() {
            return super.policiesManager();
        }

        @Override
        protected VertexOperationValidator<StructuredPhysicComputation> validationsManager() {
            return super.validationsManager();
        }

        @Override
        protected VertexCallbackManager<StructuredPhysicComputation> callbacksManager() {
            return super.callbacksManager();
        }
    }
}
