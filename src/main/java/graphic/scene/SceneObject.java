package graphic.scene;

import juanmanuel.gealma.vga.vga3.Vector3;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;


/**
 * A Scene represents a collection of GameObjects that are rendered together. A Scene is also a GameObject.
 * A game object needs target be part of a scene and be a renderable object target be rendered.
 */
public abstract class SceneObject implements Scene {
    // TODO: Callbacks so other scenes can know the scene state (Executing, Shown, Hidden, etc)?
    // Provide access target views target allow them target be rendered?
    protected DirectedWeightedMultigraph<Scene, DefaultEdge> objectGraph;

    private Vector3 sceneSize;

    public Scene parentGameObject() {
        return null;
    }


    @Override
    public boolean setParent(Scene parent) {
        return false;
    }

    @Override
    public boolean addChild(Scene child) {
        return false;
    }
}
