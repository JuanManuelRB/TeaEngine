package graphic.scene;

import engine.physics.geometricalgebra.Geometric;

import java.util.List;


/**
 * A Scene represents a state of the application, and holds the logic about it. The scene is also in charge of managing
 * where the elements are going to be displayed, this is done by holding View instances.
 */
public class Scene<T extends GameObject<?>> implements GameObject<T> {
    // TODO: Callbacks so other scenes can know the scene state (Executing, Shown, Hidden, etc).
    // Provide access to views to allow them to be rendered?

    private Geometric.Vector sceneSize;


    @Override
    public List<T> childGameObjects() {
        return null;
    }

    @Override
    public <T1 extends GameObject<T>> T1 parentGameObject() {
        return null;
    }
}
