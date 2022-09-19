package graphic.scene;

import graphic.GraphicElement;
import graphic.render.Renderable;
import graphic.render.Renderer;
import org.jetbrains.annotations.NotNull;

import java.util.TreeSet;
import java.util.Vector;

/**
 * A Scene represents a state of the application, and holds the logic about it. The scene is also in charge of managing
 * where the elements are going to be displayed, this is done by holding View instances.
 */
public interface Scene {
    // TODO: Callbacks so other scenes can know the scene state (Executing, Shown, Hidden, etc).
    // Provide access to views to allow them to be rendered?
    Renderer renderer = new Renderer(); // TODO: Provided? The Scene is not rendered, the view is?

    default void onStart(){}
    default void onEnd(){}

    default void start() {
        onStart();
        onEnd();
    }



}
