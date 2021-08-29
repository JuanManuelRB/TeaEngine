package engine.io.inputs;

/**
 * This class holds callbacks for the {@code Window} class.
 *
 * The methods in this class should be used to check when something changes in the {@code Window} class.
 */
public class WindowListener {
    private static WindowListener windowListenerInstance;

    int posX, posY, width, height;
    private int frameBufferHeight;
    private int frameBufferWidth;
    private boolean focus, closing = false;

    private WindowListener(){}

    /**
     * Singleton pattern.
     * @return WindowListener
     */
    public static WindowListener get() {
        if (windowListenerInstance == null)
            windowListenerInstance = new WindowListener();

        return windowListenerInstance;
    }

    /**
     * It's called when the windows position changes
     * @param window the GLFW window pointer.
     * @param xpos the x position in the monitor.
     * @param ypos the y position in the monitor.
     */
    public static void positionCallback(long window, int xpos, int ypos) {
        get().posX = xpos;
        get().posY = ypos;

    }

    /**
     * It's called when the windows size changes
     * @param window the GLFW window pointer.
     * @param width the window width.
     * @param height the window height.
     */
    public static void sizeCallback(long window, int width, int height) {
        get().width = width;
        get().height = height;

    }



    public static void frameBufferSizeCallback(long window, int width, int height) {
        get().frameBufferWidth = width;
        get().frameBufferHeight = height;
    }

    public static void focusCallback(long window, boolean focus) {
        get().focus = focus;
    }

    public static void closeCallback(long window) {
        get().closing = true;
    }

    /**
     * @return the X position in the monitor.
     */
    public static int getPosX() {
        return get().posX;
    }

    /**
     *
     * @return the Y position in the monitor.
     */
    public static int getPosY() {
        return get().posY;
    }

    /**
     *
     * @return the width of the window.
     */
    public static int getWidth() {
        return get().width;
    }

    /**
     *
     * @return the height of the window.
     */
    public static int getHeight() {
        return get().height;
    }

    /**
     * @return the FrameBuffer width in pixels of the window.
     *
     * This can or not, depending on the system, coincide with {@link #getWidth()}.
     */
    public static int getFrameBufferWidth() { return get().frameBufferWidth; }

    /**
     * @return the FrameBuffer height in pixels of the window.
     *
     * This can or not, depending on the system, coincide with {@link #getHeight()}.
     */
    public static int getFrameBufferHeight() { return get().frameBufferHeight; }

}
