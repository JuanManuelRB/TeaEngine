package graphic.scene;

import graphic.window.AbstractWindow;
import physics.dynamics.Position;

/**
 *
 */
public interface View {
    //TODO: Is a View a Renderable?
    // Views have position3 in the scene, size2 on the scene, Scene pertenence, position3 in a viewer?
    // size2 on a viewer?

    Camera camera();
    Scene scene();

    /**
     *
     * @param window to render the view on.
     * @param position
     */
    default void renderInto(AbstractWindow window, Position position, Size scale) {
        window.display(this, position, scale);
    }

}
