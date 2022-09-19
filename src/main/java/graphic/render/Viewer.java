package graphic.render;

import graphic.scene.View;
import graphic.window.AbstractWindow;


/**
 * View consumer. Consumes a view and displays it.
 */
public interface Viewer {

    //TODO: Viewer consumes one or more views and displays them.
    // Views can be resized, and moved on the Viewer. So the Viewer has coordinates
    /**
     * Gets views and displays them.
     * The views are composed in a single view in the process.
     *
     * @param view to display.
     */
    abstract void display(ViewDisplay... view);
    
    default void display(short updates, ViewDisplay view) {
        var v = new ViewDisplay[] {view};
        display(v);
    }
    /**
     *
     * @return the GLFW context
     */
    abstract long getContext();


    int getWidth();

    int getHeight();

    record ViewDisplay(View view, int windowPosX, int windowPosY) {}

}
