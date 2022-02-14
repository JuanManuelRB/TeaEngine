package graphic.window;

/**
 * This class holds callbacks for the {@code Window} class.
 *
 * The methods in this class should be used to check when something changes in the {@code Window} class.
 */
public abstract class AbstractWindowListener {
    /**
     * It's called when the windows position changes
     * @param window the GLFW window pointer.
     * @param xpos the x position in the monitor.
     * @param ypos the y position in the monitor.
     */
    public abstract void positionCallback(long window, int xpos, int ypos);

    /**
     * It's called when the windows size changes
     * @param window the GLFW window pointer.
     * @param width the window width.
     * @param height the window height.
     */
    public abstract void sizeCallback(long window, int width, int height);

    public abstract void frameBufferSizeCallback(long window, int width, int height);

    public abstract void focusCallback(long window, boolean focus);

    public abstract void closeCallback(long window);




}
