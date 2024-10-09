package juanmanuel.tea.physics;

import juanmanuel.tea.components.GameObject;
import juanmanuel.tea.physics.dynamics.Position;
import juanmanuel.tea.physics.dynamics.Velocity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhysicsUpdaterTest {
    static class PhysicUpdatedGameObject extends GameObject implements PhysicsUpdated {
        private boolean updated = false;

        public boolean isUpdated() {
            return updated;
        }

        @Override
        public void updatePhysics() {
            updated = true;
        }

        @Override
        public Position position() {
            return null;
        }

        @Override
        public void move() {

        }

        @Override
        public void position(Position position) {

        }

        @Override
        public void position(double e1, double e2, double e3) {

        }

        @Override
        public void velocity(Velocity velocity) {

        }

        @Override
        public void velocity(double e1, double e2, double e3) {

        }

        @Override
        public Velocity velocity() {
            return null;
        }

        @Override
        public boolean collides(PhysicsUpdated other) {
            return false;
        }
    }

    @Test
    void updatePhysicUpdated() {
        var physicsUpdater = new PhysicsUpdater();
        var gameObject = new PhysicUpdatedGameObject();

        assertFalse(gameObject.isUpdated());
        physicsUpdater.update(gameObject);
        assertTrue(gameObject.isUpdated());
    }

    @Test
    void updateComputation() {
        var physicsUpdater = new PhysicsUpdater();
        var gameObject = new PhysicUpdatedGameObject();
        var computation = physicsUpdater.computationOf(gameObject);
        var otherGameObject = new PhysicUpdatedGameObject();
        var otherComputation = physicsUpdater.computationOf(otherGameObject);

        assertFalse(gameObject.isUpdated());
        physicsUpdater.update(computation);
        assertTrue(gameObject.isUpdated());

        computation.addChild(otherComputation);
        assertFalse(otherGameObject.isUpdated());
        physicsUpdater.update(computation);
        assertTrue(otherGameObject.isUpdated());
    }

    @Test
    void matches() {
        var physicsUpdater = new PhysicsUpdater();
        var gameObject = new PhysicUpdatedGameObject();
        var otherGO = new GameObject() {};

        assertTrue(physicsUpdater.matches(gameObject));
        assertFalse(physicsUpdater.matches(otherGO));
    }

    @Test
    void updaterClass() {
        assertEquals(PhysicsUpdater.class, new PhysicsUpdater().updaterClass());
    }

    @Test
    void updatedClass() {
        assertEquals(PhysicsUpdated.class, new PhysicsUpdater().updatedClass());
    }

    @Test
    void computationClass() {
        assertEquals(PhysicsUpdater.StructuredPhysicComputation.class, new PhysicsUpdater().computationClass());
    }

    @Test
    void computationOf() {
        var physicsUpdater = new PhysicsUpdater();
        var gameObject = new PhysicUpdatedGameObject();
        var computation = physicsUpdater.computationOf(gameObject);

        assertNotNull(computation);
        assertInstanceOf(PhysicsUpdater.StructuredPhysicComputation.class, computation);
        assertInstanceOf(PhysicUpdatedGameObject.class, computation.updated());
        assertEquals(gameObject, computation.updated());
        assertEquals(computation, physicsUpdater.computationOf(gameObject));
    }

    @Test
    void updatePhysics() {
        var physicsUpdater = new PhysicsUpdater();
        var gameObject = new PhysicUpdatedGameObject();
        var computation = physicsUpdater.computationOf(gameObject);

        assertFalse(gameObject.isUpdated());
        physicsUpdater.updatePhysics();
        assertTrue(gameObject.isUpdated());
    }

    @Test
    void start() {
    }

    @Test
    void updates() {
        var physicsUpdater = new PhysicsUpdater();
        var gameObject = new PhysicUpdatedGameObject();

        assertFalse(physicsUpdater.updates(gameObject));
        physicsUpdater.computationOf(gameObject);
    }
}