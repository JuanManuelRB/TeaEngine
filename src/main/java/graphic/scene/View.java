package graphic.scene;

import graphic.render.Camera;
import graphic.render.Viewer;
import physics.dynamics.Position;
import physics.dynamics.Size;

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
     * @param viewer to render the view on.
     * @param position
     */
    default void renderInto(Viewer viewer, Position position, Size scale) {
        viewer.display(this, position, scale);
    }

}
