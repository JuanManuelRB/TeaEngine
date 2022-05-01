package graphic.window;

public class WindowListener extends AbstractWindowListener {
    int posX, posY, width, height;
    private int frameBufferHeight;
    private int frameBufferWidth;
    private boolean focus, closing = false;

    private static WindowListener windowListenerInstance;

    private WindowListener() {}


    /**
     * Singleton.
     * @return WindowListener
     */
    public static WindowListener get() {
        if (windowListenerInstance == null)
            windowListenerInstance = new WindowListener();

        return windowListenerInstance;
    }

    @Override
    final public void positionCallback(long window, int xpos, int ypos) {
        get().posX = xpos;
        get().posY = ypos;

    }

    @Override
    final public void sizeCallback(long window, int width, int height) {
        get().width = width;
        get().height = height;

    }

    @Override
    final public void frameBufferSizeCallback(long window, int width, int height) {
        get().frameBufferWidth = width;
        get().frameBufferHeight = height;
    }

    @Override
    final public void focusCallback(long window, boolean focus) {
        get().focus = focus;
    }

    @Override
    final public void closeCallback(long window) {
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
