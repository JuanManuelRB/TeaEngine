package graphic.render;

import graphic.scene.View;
import physics.dynamics.Position;


/**
 * View consumer. Consumes a view and displays it.
 */
@FunctionalInterface
public interface Viewer {

    /**
     * Gets views and displays them.
     * The views are composed in a single view in the process.
     *
     * @param view to display.
     */
    void display(View view, Position position, Size size);

    /**
     *
     * @return the GLFW context
     */
    default long getContext() {
        return 0;
    }
}
