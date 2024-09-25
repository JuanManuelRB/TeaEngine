package juanmanuel.tea.components;

import java.util.HashSet;
import java.util.Set;

public class DummyUpdater extends GameObject implements Updater<DummyUpdater, DummyUpdated, DummyComputation>, DummyUpdated {
    private final Set<DummyUpdated> dummyUpdatedSet = new HashSet<>();

    public DummyUpdater() {
        super();
    }

    public void update() {
        dummyUpdatedSet.forEach(DummyUpdated::dummyUpdate);
    }

    @Override
    public void dummyUpdate() {
        update();
    }

    @Override
    public void update(DummyUpdated updated) {
        updated.dummyUpdate();
    }

    @Override
    public DummyComputation computationOf(DummyUpdated updated) {
        return new DummyComputation((Class<DummyUpdater>) this.getClass(), updated);
    }

    @Override
    public Class<DummyUpdater> updaterClass() {
        return DummyUpdater.class;
    }

    @Override
    public Class<DummyUpdated> updatedClass() {
        return DummyUpdated.class;
    }

    @Override
    public Class<DummyComputation> computationClass() {
        return DummyComputation.class;
    }
}
