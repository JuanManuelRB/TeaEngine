package graphic.scene;

import graphic.render.Viewer;
import graphic.window.AbstractWindow;
import org.jetbrains.annotations.NotNull;

public abstract class View implements Comparable<View> {
    //TODO: Is a View a Renderable?
    // Views have position3 in the scene, size2 on the scene, Scene pertenence, position3 in a viewer?
    // size2 on a viewer?
    private final int depth, width, height;
    private final Scene scene;

    private int sceneX, sceneY, sceneZ;
    private long fov;

    /**
     *
     * @param scene to view.
     * @param width of the view.
     * @param height of the view.
     * @param depth of the view.
     */
    public View(Scene scene, int width, int height, int depth) {
        this.scene = scene;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     *
     * @param window to render the view on.
     * @param x position on the window.
     * @param y position on the window.
     * @param updates per second. Limit of updates to render.
     */
    public final void render(AbstractWindow window, int x, int y, short updates) {
        window.display(updates, new Viewer.ViewDisplay(this, x, y));
    }

    /**
     * A view is bigger the more depht it has.
     * Used to compose views.
     *
     * @param view to be compared with.
     * @return
     */
    @Override
    public final int compareTo(@NotNull View view) {
        return Integer.compare(this.depth, view.depth);
    }

    public int depth() {
        return this.depth;
    }

    /**
     * Width of the viewer.
     *
     * @return width size, an int.
     */
    public abstract int getWidth();

    /**
     * Height of the viewer.
     *
     * @return height size, an int
     */
    public abstract int getHeight();
}
