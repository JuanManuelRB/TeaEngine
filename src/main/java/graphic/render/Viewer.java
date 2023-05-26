package graphic.render;

import graphic.scene.View;
import physics.dynamics.Position;
import physics.dynamics.Size;


/**
 * View consumer. Consumes a view and displays it.
 */
public interface Viewer {

    /**
     * Display a view in a position.
     * @param view to display.
     * @param position of the view.
     * @param size of the view.
     */
    void display(View view, Position position, Size size);

    /**
     *
     * @return the renderer in use.
     */
    Renderer renderer();

    /**
     *
     * @return the GLFW context
     */
    default long getContext() {
        return 0;
    }

}
