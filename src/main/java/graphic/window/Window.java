package graphic.window;

import graphic.scene.View;
import io.inputs.GamepadListener;
import io.inputs.KeyListener;
import io.inputs.MonitorListener;
import io.inputs.MouseListener;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Main Window of the application.
 * Holds a reference to the GLFW window and implements methods to change its behavior
 *
 * {@snippet lang=java:
 *      Window window = Window.builder().build();
 *      window.create();
 *
 * }
 * TODO
 *
 */
public final class Window extends AbstractWindow {
    private final long glfwWindow;

    /**
     * Creates a new window, and initialises GLFW.
     *
     * OpenGL is not initialised yet, needs to be with {@code create()} before any atempt to acces the api.
     * 
     * @param title of the window.
     * @param width of the window.
     * @param height of the window.
     * @param posX coordinate X of the window in OpenGL coordinates.
     * @param posY coordinate Y of the window in OpenGL coordinates.
     * @param mode of the window.
     * @param alignment of the window.
     * 
     */
    private Window(String title, int width, int height, int posX, int posY, WindowMode mode, Alignment alignment,
                   ApplicationListeners applicationListeners) throws IllegalStateException {
        super(applicationListeners);


        // Configurar GLFW
        glfwDefaultWindowHints();                  // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);  // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // TODO: create function that associates the window with a monitor or puts its value to NULL
        //  if the window should be fullscreen.

        // Crear la ventana
        this.glfwWindow = glfwCreateWindow(width, height, title, NULL, NULL);

        if (glfwWindow == NULL)
            throw new IllegalStateException("Error: Window could not be created");

        // Center the window on the primary monitor. // TODO: alinear en vez de centrar.
        {
            // Get the window size.
            var size = getSize();

            // Get primary monitor resolution.
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            align(Alignment.CENTER);
            // Center the window.
            assert videoMode != null;
            glfwSetWindowPos(
                    getContext(),
                    ((int) (videoMode.width() - size.width()) / 2),
                    ((int) (videoMode.height() - size.height()) / 2)
            );
        }
    }

    /**
     *
     * @return a builder for the window.
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void display(View view, physics.dynamics.Position position, Size size) {

    }

    @Override
    public long getContext() {
        return glfwWindow;
    }

    /**
     * Creates the window.
     * Creates a GLFW window and initializes OpenGL. This step must be done before any calling to OpenGL.
     */
    @Override
    protected void create() {
        // Set Callbacks
        {
            // Window Callbacks
            glfwSetWindowSizeCallback(getContext(), windowListener::sizeCallback);                // Tamaño de ventana
            glfwSetWindowPosCallback(getContext(), windowListener::positionCallback);             // Posición de ventana
            glfwSetFramebufferSizeCallback(getContext(), windowListener::frameBufferSizeCallback);// Tamaño en pixeles para OpenGL
            glfwSetWindowFocusCallback(getContext(), windowListener::focusCallback);              // Foco
            glfwSetWindowCloseCallback(getContext(), windowListener::closeCallback);              // Cierre

            // Monitor
            glfwSetMonitorCallback(monitorListener::monitorCallback);

            // Mouse Callbacks
            glfwSetCursorPosCallback(getContext(), mouseListener::mousePosCallback);        // Posición de ratón
            glfwSetMouseButtonCallback(getContext(), mouseListener::mouseButtonCallback);   // Botones de ratón
            glfwSetScrollCallback(getContext(), mouseListener::mouseScrollCallback);        // Desplazamiento de la rueda

            // Keys Callback
            var pkcb = glfwSetKeyCallback(getContext(), keyListener::keyCallback); // Keyboard

            // TODO: No existe callback de gamepad, GLFW no lo considera dependiente de la ventana.
            // glfwSetJoystickCallback();
            // glfwSetJoystickUserPointer();
        }

        // Sets the window as the actual context.
        glfwMakeContextCurrent(getContext());

        // Equivalente a usar VSync, se actualiza según la tasa de refresco de la ventana TODO
        glfwSwapInterval(1);

        // Show the window
        glfwShowWindow(getContext());

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set to a default color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public String getTitle() {
        return null;
    }// TODO: get the title from glfw

    @Override
    public void setClearColor(float red, float green, float blue, float alpha) {
        if (getContext() != NULL)
            glClearColor(red, green, blue, alpha);
    }

    private int[] getPositionArr() {
        try(var stack = MemoryStack.stackPush()) {
            var pPosX = stack.mallocInt(1);
            var pPosY = stack.mallocInt(1);

            glfwGetWindowPos(getContext(), pPosX, pPosY);

            return new int[] {pPosX.get(), pPosY.get()};
        }
    }


    /**
     *
     * @return An {@code int} with the value of the X position of the window.
     */
    @Override
    public int getPosX() {
        return getPositionArr()[0];
    }

    /**
     *
     * @return An {@code int} with the value of the Y position of the window.
     */
    @Override
    public int getPosY() {
        return getPositionArr()[1];
    }

    /**
     *
     * @return A {@code Position} with the values of the X and Y position of the window.
     */
    @Override
    public Position getPosition() {
        return new Position(getPositionArr()[0], getPositionArr()[1], 0);
    }

    private int[] getSizeArr() {
        try ( var stack = MemoryStack.stackPush() ) {
            var pWidth = stack.mallocInt(1);  // int*
            var pHeight = stack.mallocInt(1); // int*

            // Get the window size
            glfwGetWindowSize(getContext(), pWidth, pHeight);

            return new int[]{pWidth.get(), pHeight.get()};
        }
    }

    /**
     *
     * @return An {@code int} with the value of the width of the window.
     */
    @Override
    public int getWidth() {
        return getSizeArr()[0];
    }

    /**
     *
     * @return An {@code int} with the value of the height of the window.
     */
    @Override
    public int getHeight() {
        return getSizeArr()[1];
    }

    /**
     *
     * @return A {@code Size} with the values of the width and height of the window.
     */
    @Override
    public Size getSize() {
        return new Size(getSizeArr()[0], getSizeArr()[1], 0);

    }

    @Override
    public void setSize(Size size) {
        glfwSetWindowSize(getContext(), (int) size.width(), (int) size.height());
    }

    @Override
    public void setPosition(Position position) {
        // TODO
    }

    @Override
    public void align(Alignment alignment) {
        // TODO
    }

    public static class Listener extends WindowListener {
        int posX, posY, width, height;
        private int frameBufferHeight;
        private int frameBufferWidth;
        private boolean focus, closing = false;

        private static Listener defaultWindowListenerInstance;

        private Listener() {}


        /**
         * Singleton.
         * @return WindowListener
         */
        public static Listener get() {
            if (defaultWindowListenerInstance == null)
                defaultWindowListenerInstance = new Listener();

            return defaultWindowListenerInstance;
        }

        @Override
        final public void positionCallback(long window, int xpos, int ypos) {
            this.posX = xpos;
            this.posY = ypos;

        }

        @Override
        final public void sizeCallback(long window, int width, int height) {
            this.width = width;
            this.height = height;

        }

        @Override
        final public void frameBufferSizeCallback(long window, int width, int height) {
            this.frameBufferWidth = width;
            this.frameBufferHeight = height;
        }

        @Override
        final public void focusCallback(long window, boolean focus) {
            this.focus = focus;
        }

        @Override
        final public void closeCallback(long window) {
            this.closing = true;
        }

        /**
         *
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
         * This may or not, depending on the system, coincide with {@link #getWidth()}.
         */
        public static int getFrameBufferWidth() { return get().frameBufferWidth; }

        /**
         * @return the FrameBuffer height in pixels of the window.
         *
         * This may or not, depending on the system, coincide with {@link #getHeight()}.
         */
        public static int getFrameBufferHeight() { return get().frameBufferHeight; }

    }

    public static class Builder implements WindowBuilder<Window> {
        private String title = "Ventana";
        private int width = 480, height = 360;
        private int xPos = 0, yPos = 0;
        private WindowMode windowMode = WindowMode.WINDOWED;
        private Alignment alignment = Alignment.CENTER;
        private WindowListener windowListener;
        private MouseListener mouseListener;
        private KeyListener keyListener;
        private GamepadListener gamepadListener;
        private MonitorListener monitorListener;

        @Override
        public Window build() {
            return new Window(
                    title, width, height, xPos, yPos, windowMode, alignment,
                    new ApplicationListeners(
                            Optional.ofNullable(windowListener),
                            Optional.ofNullable(mouseListener),
                            Optional.ofNullable(keyListener),
                            Optional.ofNullable(gamepadListener),
                            Optional.ofNullable(monitorListener)
                    )
            );
        }

        @Override
        public WindowBuilder<Window> title(String title) {
            this.title = title;
            return this;
        }

        @Override
        public WindowBuilder<Window> width(int width) {
            this.width = width;
            return this;
        }

        @Override
        public WindowBuilder<Window> height(int height) {
            this.height = height;
            return this;
        }

        @Override
        public WindowBuilder<Window> positionX(int xPos) {
            this.xPos = xPos;
            return this;
        }

        @Override
        public WindowBuilder<Window> positionY(int yPos) {
            this.yPos = yPos;
            return this;
        }

        @Override
        public WindowBuilder<Window> screenMode(WindowMode windowMode) {
            this.windowMode = windowMode;
            return this;
        }

        @Override
        public WindowBuilder<Window> alignment(AbstractWindow.Alignment alignment) {
            this.alignment = alignment;
            return this;
        }


        @Override
        public WindowBuilder<Window> windowListener(WindowListener listener) {
            this.windowListener = listener;
            return this;
        }

        @Override
        public WindowBuilder<Window> mouseListener(MouseListener listener) {
            this.mouseListener = listener;
            return this;
        }

        @Override
        public WindowBuilder<Window> keyListener(KeyListener listener) {
            this.keyListener = listener;
            return this;
        }

        @Override
        public WindowBuilder<Window> gamepadListener(GamepadListener listener) {
            this.gamepadListener = listener;
            return this;
        }

        @Override
        public WindowBuilder<Window> monitorListener(MonitorListener listener) {
            this.monitorListener = listener;
            return this;
        }
    }
}