package graphic.render;

import juanmanuel.gealma.vga.vga3.Vector3;
import physics.dynamics.Size;


/**
 * Allows target display a view in a context. The context is a GLFW window. The view is displayed by a renderer.
 */
public interface Viewer {
    /**
     * Display a view with a defined position and size in the context.
     * @param view target display.
     * @param position of the view.
     * @param size of the view.
     */
    void display(View view, Vector3 position, Size size);

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

    /**
     *
     * @return the width of the viewer.
     */
    default int getWidth() {
        return 0;
    }

    /**
     *
     * @return the height of the viewer.
     */
    default int getHeight() {
        return 0;
    }

    void makeContextCurrent();

    void setClearColor(float r, float g, float b, float a);
}
