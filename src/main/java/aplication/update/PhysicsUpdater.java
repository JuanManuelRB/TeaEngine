package aplication.update;

import aplication.objects.GameObject;

import java.util.HashSet;
import java.util.Set;

public class PhysicsUpdater extends GameObject implements Updater<PhysicsUpdater, PhysicsUpdater.PhysicsUpdated>, AutoCloseable {
    private final Set<PhysicsUpdated> physicsUpdated = new HashSet<>();

    public PhysicsUpdater() {
        super();
        addUpdater(this);
    }

    @Override
    public void add(PhysicsUpdated physicsUpdated) {
        this.physicsUpdated.add(physicsUpdated);
        if (physicsUpdated instanceof GameObject gameObject)
            gameObject.addUpdater(this);
    }

    @Override
    public Class<PhysicsUpdater> updaterClass() {
        return PhysicsUpdater.class;
    }

    @Override
    public Class<PhysicsUpdated> updatedClass() {
        return PhysicsUpdated.class;
    }

    public void update() {
        physicsUpdated.parallelStream().forEach(PhysicsUpdated::updatePhysics);
    }

    @Override
    public void close() throws Exception {

    }

    @FunctionalInterface
    public interface PhysicsUpdated {
        void updatePhysics();

    }
}
