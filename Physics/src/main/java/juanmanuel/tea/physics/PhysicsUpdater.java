package juanmanuel.tea.physics;

import juanmanuel.tea.components.GameObject;
import juanmanuel.tea.components.StructuredComputation;
import juanmanuel.tea.components.Updater;
import juanmanuel.tea.graph.ApplicationGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

public class PhysicsUpdater extends GameObject<PhysicsUpdater> implements Updater<PhysicsUpdater, PhysicsUpdated, PhysicsUpdater.StructuredPhysicComputation>, PhysicsUpdated {
//    private final Set<PhysicsUpdated> physicsUpdatedCollection = new HashSet<>();
    private final String name;
    private final Semaphore concurrentComputeSemaphore = new Semaphore(1);

    protected StructuredPhysicComputationGraph computationGraph = new StructuredPhysicComputationGraph();

    private boolean running = false;

    public PhysicsUpdater() {
        this("");
    }

    public PhysicsUpdater(String name) {
        super();
        this.name = name;
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
    public void update(PhysicsUpdated physicsUpdated) {
        physicsUpdated.updatePhysics();
    }

    @Override
    public StructuredPhysicComputation computationOf(PhysicsUpdated updated) {
        return computationGraph.vertexSet().stream()
                .filter(computation -> computation.updated().equals(updated))
                .findFirst()
                .orElseGet(() -> {
                    var sc = new StructuredPhysicComputation(updated);
                    computationGraph.addVertex(sc);
                    return sc;
                });
    }

    @Override
    public void updatePhysics() {
        // TODO: Start computation on the graph's heads
    }

    public void start() {
        if (running)
            return;

        running = true;
        Timer timer = new Timer();
//        final long[] startTime = {System.nanoTime()};
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    concurrentComputeSemaphore.acquire();
                    updatePhysics();
//                    double elapsedTime = System.nanoTime() - startTime[0];
//                    startTime[0] = System.nanoTime();
//                    System.out.println(STR."\{elapsedTime / 1000000} ms");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    concurrentComputeSemaphore.release();
                }
            }
        }, 0, 1000 / 30);
    }

    /**
     * Checks if this updater can update the object
     * @param updated The object to check
     * @return True if this updater would update the object on one of its cycles
     */
    public boolean updates(PhysicsUpdated updated) {
        return computationGraph.vertexSet().stream()
                .anyMatch(computation -> computation.updated().equals(updated));
    }

    @Override
    public String toString() {
//        return STR."PhysicsUpdater{physicsUpdated = \{ physicsUpdatedCollection }}";
//        return STR."PhysicsUpdater{\{ updaterComputationGraph }}";
        return STR."PhysicsUpdater{\{name}}";
    }

    public class StructuredPhysicComputation extends StructuredComputation<PhysicsUpdater, PhysicsUpdated, StructuredPhysicComputation> {
        public StructuredPhysicComputation(PhysicsUpdated updated) {
            super(PhysicsUpdater.class, updated, computationGraph);
            //PhysicsUpdater.this.computationGraph.addVertex(this);
        }

        @Override
        protected void onEnterChild(StructuredPhysicComputation child) throws RuntimeException {

        }

        @Override
        protected void onLeaveChild(StructuredPhysicComputation child) throws RuntimeException {

        }

        @Override
        protected void onEnterGraph(ApplicationGraph<StructuredPhysicComputation, DefaultWeightedEdge> graph) throws RuntimeException {
            super.onEnterGraph(graph);
        }

        @Override
        protected void onLeaveGraph(ApplicationGraph<StructuredPhysicComputation, DefaultWeightedEdge> graph) throws RuntimeException {
            super.onLeaveGraph(graph);
        }

        @Override
        protected Supplier<StructuredPhysicComputation> computationSupplier(PhysicsUpdated updated) {
            Objects.requireNonNull(updated);

            return () -> new StructuredPhysicComputation(updated);
        }
    }

    public static final class StructuredPhysicComputationGraph extends ApplicationGraph<StructuredPhysicComputation, DefaultWeightedEdge> {
        public StructuredPhysicComputationGraph() {
            super(DefaultWeightedEdge.class);
        }
    }
}
