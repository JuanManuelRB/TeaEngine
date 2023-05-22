package graphic.window;

/**
 * This class holds callbacks for the {@code Window} class.
 *
 * The methods in this class should be used to check when something changes in the {@code Window} class.
 */
public abstract class WindowListener {
    /**
     * Called when the windows position changes.
     *
     * @param window GLFW window pointer.
     * @param xpos i position in the monitor.
     * @param ypos j position in the monitor.
     */
    public abstract void positionCallback(long window, int xpos, int ypos);

    /**
     * Called when the windows size changes.
     *
     * @param window GLFW window pointer.
     * @param width window width.
     * @param height window height.
     */
    public abstract void sizeCallback(long window, int width, int height);

    /**
     * Called when the window framebuffer size changes.
     *
     * @param window GLFW window pointer.
     * @param width the window framebuffer width.
     * @param height the window framebuffer height.
     */
    public abstract void frameBufferSizeCallback(long window, int width, int height);

    /**
     * Called when the window focus changes.
     *
     * @param window GLFW window pointer.
     * @param focus
     */
    public abstract void focusCallback(long window, boolean focus);

    /**
     * Called whe the window is closed.
     *
     * @param window GLFW window pointer.
     */
    public abstract void closeCallback(long window);




}
