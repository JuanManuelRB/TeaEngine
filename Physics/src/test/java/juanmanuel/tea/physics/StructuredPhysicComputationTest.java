package juanmanuel.tea.physics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructuredPhysicComputationTest {

    @Test
    void updater() {
        var updater = new PhysicsUpdater();
        var computation = new PhysicsUpdater.StructuredPhysicComputation(() -> {
        });

        assertEquals(updater.getClass(), computation.updaterClass());
    }

    @Test
    void updated() {
        var updated = (PhysicsUpdated) () -> {};
        var updater = new PhysicsUpdater();
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);

        assertEquals(updated, computation.updated());
    }

    @Test
    void updaterClass() {
        var updater = new PhysicsUpdater();
        assertEquals(PhysicsUpdater.class, new PhysicsUpdater.StructuredPhysicComputation(() -> {
        }).updaterClass());
    }

    @Test
    void updatedClass() {
        var updater = new PhysicsUpdater();
        assertTrue(PhysicsUpdated.class.isAssignableFrom(new PhysicsUpdater.StructuredPhysicComputation(() -> {
        }).updatedClass()));
    }

    @Test
    void computationClass() {
        var updater = new PhysicsUpdater();
        assertEquals(PhysicsUpdater.StructuredPhysicComputation.class, new PhysicsUpdater.StructuredPhysicComputation(() -> {
        }).computationClass());
    }

    @Test
    void getComputation() {
        var updater = new PhysicsUpdater();
        var updated = (PhysicsUpdated) () -> {};
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);
        var otherUpdated = (PhysicsUpdated) () -> {};

        assertEquals(updated, computation.findOrNewComputation(updated).updated());
        assertNotNull(computation.findOrNewComputation(otherUpdated));
    }

    @Test
    void compute() throws InterruptedException {
        AtomicBoolean up1 = new AtomicBoolean(false);
        var updater = new PhysicsUpdater();
        var updated = (PhysicsUpdated) () -> up1.set(!up1.get());
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);

        computation.startComputationBy(updater);
        assertTrue(up1.get());

        var up2 = new AtomicBoolean(false);
        var otherUpdated = (PhysicsUpdated) () -> up2.set(!up2.get());
        computation.andThen(otherUpdated);

        assertTrue(computation.isUpdatedAfter(otherUpdated));

//        computation.compute(updater);

        // Wait for the computation to finish
        sleep(1);

//        assertFalse(up1.get());
//        assertTrue(up2.get());
    }

    @Test
    void onStartCompute() {
    }

    @Test
    void onFinishCompute() {
    }

    @Test
    void andThen() {
        var updater = new PhysicsUpdater();
        var updated = (PhysicsUpdated) () -> {};
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);
        var otherUpdated = (PhysicsUpdated) () -> {};
        var otherComputation = computation.andThen(otherUpdated);

        assertTrue(computation.isUpdatedAfter(otherUpdated));

    }

    @Test
    void onParallel() {
        var updater = new PhysicsUpdater();
        var updated = (PhysicsUpdated) () -> {};
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);
        var otherUpdated = (PhysicsUpdated) () -> {};
        var otherComputation = computation.onParallel(otherUpdated).orElseThrow(() -> new AssertionError("Should be present"));

//        System.out.println(computation);
//        System.out.println(otherComputation);
//        computation.siblings().forEach(System.out::println);
//        computation.fullSiblings().forEach(System.out::println);
//
//        assertTrue(computation.isUpdatedParallel(otherUpdated));
//        assertTrue(otherComputation.isUpdatedParallel(updated));
    }

    @Test
    void updatedChildren() {
        var updated = (PhysicsUpdated) () -> {};
        var updater = new PhysicsUpdater();
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);
        assertFalse(computation.updatedChildren().contains(updated));

        var otherUpdated = (PhysicsUpdated) () -> {};
        computation.andThen(otherUpdated);

    }

    @Test
    void updatedParents() {
        var updated = (PhysicsUpdated) () -> {};
        var updater = new PhysicsUpdater();
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);
        assertTrue(computation.updatedParents().isEmpty());
    }

    @Test
    void updatedDescendants() {
        var updated = (PhysicsUpdated) () -> {};
        var updater = new PhysicsUpdater();
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);
        assertTrue(computation.updatedDescendants().isEmpty());
    }

    @Test
    void updatedAncestors() {
        var updated = (PhysicsUpdated) () -> {};
        var updater = new PhysicsUpdater();
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);
        assertTrue(computation.updatedAncestors().isEmpty());
    }

    @Test
    void isUpdatedSequential() {
        var updated = (PhysicsUpdated) () -> {};
        var updater = new PhysicsUpdater();
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);
        assertFalse(computation.isUpdatedAfter(updated));
    }

    @Test
    void updatedGraph() {
        var updated = (PhysicsUpdated) () -> {};
        var updater = new PhysicsUpdater();
        var computation = new PhysicsUpdater.StructuredPhysicComputation(updated);
        assertTrue(computation.updatedGraph().contains(updated));
    }

    @Test
    void testComputationOf() {
    }

    @Test
    void removeComputation() {
    }

    @Test
    void graphUpdates() {
    }

    @Test
    void updates() {
    }

    @Test
    void isEquivalent() {
    }

    @Test
    void onEnterParent() {
    }
}