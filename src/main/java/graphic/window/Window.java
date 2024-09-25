//package graphic.window;
//
//import graphic.render.Renderer;
//import graphic.render.View;
//import graphic.scene.Camera;
//import graphic.scene.ViewObject;
//import io.inputs.GamepadListener;
//import io.inputs.KeyListener;
//import io.inputs.MonitorListener;
//import io.inputs.MouseListener;
//import juanmanuel.gealma.vga.vga3.Vector3;
//import org.lwjgl.glfw.GLFWVidMode;
//import org.lwjgl.opengl.GL;
//import org.lwjgl.system.MemoryStack;
//import physics.dynamics.Size;
//
//import java.io.IOException;
//import java.util.Objects;
//
//import static org.lwjgl.glfw.GLFW.*;
//import static org.lwjgl.opengl.GL11.*;
//import static org.lwjgl.system.MemoryUtil.NULL;
//
///**
// * Main Window of the application.
// * Holds a reference target the GLFW window and implements methods target change its behavior
// *
// * {@snippet lang=java:
// *      Window window = Window.builder().build();
// *      window.create();
// *
// * }
// * TODO
// *
// */
//public final class Window extends AbstractWindow {
//    private final long glfwWindow;
//    private Renderer renderer;
//    private View mainView;
//
//    /**
//     * Creates a new window, and initialises GLFW.
//     *
//     * OpenGL is not initialised yet, needs target be with {@code create()} before any atempt target acces the api.
//     *
//     * @param title of the window.
//     * @param options of the window.
//     *
//     */
//    private Window(String title, Options options, ApplicationListeners applicationListeners) throws IllegalStateException {
//        super(applicationListeners);
//
//        // Configurar GLFW
//        glfwDefaultWindowHints();                  // optional, the current window hints are already the default
//        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);  // the window will stay hidden after creation
//        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
//
//        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
//        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
//        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
//        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
//
//        // TODO: create function that associates the window with a monitor or puts its value target NULL
//        //  if the window should be fullscreen.
//
//        // Crear la ventana
//        this.glfwWindow = glfwCreateWindow(options.width, options.height, title, NULL, NULL);
//
//        if (glfwWindow == NULL)
//            throw new IllegalStateException("Error: Window could not be created");
//
//        // Center the window on the primary monitor. // TODO: alinear en vez de centrar.
//        {
//            // Get the window size.
//            var size = getSize();
//
//            // Get primary monitor resolution.
//            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//
//            align(Alignment.CENTER);
//            // Center the window.
//            assert videoMode != null;
//            glfwSetWindowPos(
//                    getContext(),
//                    ((int) (videoMode.width() - size.width()) / 2),
//                    ((int) (videoMode.height() - size.height()) / 2)
//            );
//        }
//
//        // Set Callbacks
//        {
//            // Window Callbacks
//            glfwSetWindowSizeCallback(getContext(), windowListener::sizeCallback);                // Tamaño de ventana
//            glfwSetWindowPosCallback(getContext(), windowListener::positionCallback);             // Posición de ventana
//            glfwSetFramebufferSizeCallback(getContext(), windowListener::frameBufferSizeCallback);// Tamaño en pixeles para OpenGL
//            glfwSetWindowFocusCallback(getContext(), windowListener::focusCallback);              // Foco
//            glfwSetWindowCloseCallback(getContext(), windowListener::closeCallback);              // Cierre
//
//            // Monitor
//            glfwSetMonitorCallback(monitorListener::monitorCallback);
//
//            // Mouse Callbacks
//            glfwSetCursorPosCallback(getContext(), mouseListener::mousePosCallback);        // Posición de ratón
//            glfwSetMouseButtonCallback(getContext(), mouseListener::mouseButtonCallback);   // Botones de ratón
//            glfwSetScrollCallback(getContext(), mouseListener::mouseScrollCallback);        // Desplazamiento de la rueda
//
//            // Keys Callback
//            var pkcb = glfwSetKeyCallback(getContext(), keyListener::keyCallback); // Keyboard
//
//            // TODO: No existe callback de gamepad, GLFW no lo considera dependiente de la ventana.
//            // glfwSetJoystickCallback();
//            // glfwSetJoystickUserPointer();
//        }
//
//        // Sets the window as the actual context.
//        makeContextCurrent();
//        GL.createCapabilities();
//
//        System.out.println("OpenGL: " + glGetString(GL_VERSION));
//        System.out.println("GLFW: " + glfwGetVersionString());
//
//        // Equivalente a usar VSync, se actualiza según la tasa de refresco de la ventana TODO
//        glfwSwapInterval(1);
//
//        renderer = new Renderer(this);
//        Camera camera = new Camera(new Vector3(0, 0, 0));
////        camera.projectionMatrix(getWidth(), getHeight()); TODO
//        try {
//            renderer.setView(new ViewObject(camera));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public Window renderer(Renderer renderer) {
//        this.renderer = renderer;
//        return this;
//    }
//
//    @Override
//    public Renderer renderer() {
//        return renderer;
//    }
//
//    /**
//     *
//     * @return a builder for the window.
//     */
//    public static Builder builder() {
//        return new Builder();
//    }
//
//    @Override
//    public void display(View view, Vector3 position, Size size) {
//        // TODO
//    }
//
//    @Override
//    public long getContext() {
//        return glfwWindow;
//    }
//
//    /**
//     * Creates the window.
//     */
//    @Override
//    public void create() {
//        makeContextCurrent();
//        // Show the window
//        glfwShowWindow(getContext());
//
////        // TODO: this should be done in the renderer. Also, it should be done on each loop.
////        // This line is critical for LWJGL's interoperation with GLFW's
////        // OpenGL context, or any context that is managed externally.
////        // LWJGL detects the context that is current in the current thread,
////        // creates the GLCapabilities instance and makes the OpenGL
////        // bindings available for use.
////        GL.createCapabilities();
//
//        // Set target a default color
//        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//
//
//
//    }
//
//    @Override
//    public void update() {
//        makeContextCurrent();
//        renderer.render();
//
//        glfwSwapBuffers(getContext());
//        glfwPollEvents();
//    }
//
//    @Override
//    public String getTitle() {
//        return null;
//    }// TODO: get the title from glfw
//
//    @Override
//    public void setClearColor(float red, float green, float blue, float alpha) {
//        if (getContext() != NULL)
//            glClearColor(red, green, blue, alpha);
//    }
//
//    private int[] getPositionArr() {
//        try(var stack = MemoryStack.stackPush()) {
//            var pPosX = stack.mallocInt(1);
//            var pPosY = stack.mallocInt(1);
//
//            glfwGetWindowPos(getContext(), pPosX, pPosY);
//
//            return new int[] {pPosX.get(), pPosY.get()};
//        }
//    }
//
//
//    /**
//     *
//     * @return An {@code int} with the value of the X position of the window.
//     */
//    @Override
//    public int getPosX() {
//        return getPositionArr()[0];
//    }
//
//    /**
//     *
//     * @return An {@code int} with the value of the Y position of the window.
//     */
//    @Override
//    public int getPosY() {
//        return getPositionArr()[1];
//    }
//
//    /**
//     *
//     * @return A {@code Position} with the values of the X and Y position of the window.
//     */
//    @Override
//    public Vector3 getPosition() {
//        return new Vector3(getPositionArr()[0], getPositionArr()[1], 0);
//    }
//
//    private int[] getSizeArr() {
//        try ( var stack = MemoryStack.stackPush() ) {
//            var pWidth = stack.mallocInt(1);  // int*
//            var pHeight = stack.mallocInt(1); // int*
//
//            // Get the window size
//            glfwGetWindowSize(getContext(), pWidth, pHeight);
//
//            return new int[]{pWidth.get(), pHeight.get()};
//        }
//    }
//
//    /**
//     *
//     * @return An {@code int} with the value of the width of the window.
//     */
//    @Override
//    public int getWidth() {
//        return getSizeArr()[0];
//    }
//
//    /**
//     *
//     * @return An {@code int} with the value of the height of the window.
//     */
//    @Override
//    public int getHeight() {
//        return getSizeArr()[1];
//    }
//
//    /**
//     *
//     * @return A {@code Size} with the values of the width and height of the window.
//     */
//    @Override
//    public Size getSize() {
//        return new Size(getSizeArr()[0], getSizeArr()[1], 0);
//
//    }
//
//    @Override
//    public void setSize(Size size) {
//        glfwSetWindowSize(getContext(), (int) size.width(), (int) size.height());
//    }
//
//    @Override
//    public void setPosition(Vector3 position) {
//        // TODO
//    }
//
//    @Override
//    public void align(Alignment alignment) {
//        // TODO
//    }
//
//    public record Options(boolean resizable, int width, int height, int updatesPerSecond, int positionX, int positionY, WindowMode windowMode, Alignment alignment) {
//        public Options {
//            if (width <= 0 || height <= 0)
//                throw new IllegalArgumentException("Width and height must be greater than 0");
//            if (updatesPerSecond <= 0)
//                throw new IllegalArgumentException("Updates per second must be greater than 0");
//
//            Objects.requireNonNull(windowMode);
//            Objects.requireNonNull(alignment);
//        }
//
//        /**
//         * Default options for the window.
//         * <p>
//         *     <ul>
//         *         <li>Resizable: true</li>
//         *         <li>Width: 800</li>
//         *         <li>Height: 600</li>
//         *         <li>Updates per second: 60</li>
//         *         <li>Position X: 0</li>
//         *         <li>Position Y: 0</li>
//         *         <li>Window mode: Windowed</li>
//         *         <li>Alignment: Center</li>
//         *     </ul>
//         * </p>
//         *
//         */
//        public Options() {
//            this(true, 800, 600, 60, 0, 0, WindowMode.WINDOWED, Alignment.CENTER);
//        }
//
//        public Options resizable(boolean resizable) {
//            return new Options(resizable, width, height, updatesPerSecond, positionX, positionY, windowMode, alignment);
//        }
//
//        public Options width(int width) {
//            return new Options(resizable, width, height, updatesPerSecond, positionX, positionY, windowMode, alignment);
//        }
//
//        public Options height(int height) {
//            return new Options(resizable, width, height, updatesPerSecond, positionX, positionY, windowMode, alignment);
//        }
//
//        public Options updatesPerSecond(int updatesPerSecond) {
//            return new Options(resizable, width, height, updatesPerSecond, positionX, positionY, windowMode, alignment);
//        }
//
//        public Options positionX(int positionX) {
//            return new Options(resizable, width, height, updatesPerSecond, positionX, positionY, windowMode, alignment);
//        }
//
//        public Options positionY(int positionY) {
//            return new Options(resizable, width, height, updatesPerSecond, positionX, positionY, windowMode, alignment);
//        }
//
//        public Options windowMode(WindowMode windowMode) {
//            return new Options(resizable, width, height, updatesPerSecond, positionX, positionY, windowMode, alignment);
//        }
//
//        public Options alignment(Alignment alignment) {
//            return new Options(resizable, width, height, updatesPerSecond, positionX, positionY, windowMode, alignment);
//        }
//
//
//    }
//
//    public static class Listener extends WindowListener {
//        int posX, posY, width, height;
//        private int frameBufferHeight;
//        private int frameBufferWidth;
//        private boolean focus, closing = false;
//
//        private static Listener defaultWindowListenerInstance;
//
//        private Listener() {}
//
//
//        /**
//         * Singleton.
//         * @return WindowListener
//         */
//        public static Listener get() {
//            if (defaultWindowListenerInstance == null)
//                defaultWindowListenerInstance = new Listener();
//
//            return defaultWindowListenerInstance;
//        }
//
//        @Override
//        final public void positionCallback(long window, int xpos, int ypos) {
//            this.posX = xpos;
//            this.posY = ypos;
//
//        }
//
//        @Override
//        final public void sizeCallback(long window, int width, int height) {
//            this.width = width;
//            this.height = height;
//
//        }
//
//        @Override
//        final public void frameBufferSizeCallback(long window, int width, int height) {
//            this.frameBufferWidth = width;
//            this.frameBufferHeight = height;
//        }
//
//        @Override
//        final public void focusCallback(long window, boolean focus) {
//            this.focus = focus;
//        }
//
//        @Override
//        final public void closeCallback(long window) {
//            this.closing = true;
//        }
//
//        /**
//         *
//         * @return the X position in the monitor.
//         */
//        public static int getPosX() {
//            return get().posX;
//        }
//
//        /**
//         *
//         * @return the Y position in the monitor.
//         */
//        public static int getPosY() {
//            return get().posY;
//        }
//
//        /**
//         *
//         * @return the width of the window.
//         */
//        public static int getWidth() {
//            return get().width;
//        }
//
//        /**
//         *
//         * @return the height of the window.
//         */
//        public static int getHeight() {
//            return get().height;
//        }
//
//        /**
//         * @return the FrameBuffer width in pixels of the window.
//         *
//         * This may or not, depending on the system, coincide with {@link #getWidth()}.
//         */
//        public static int getFrameBufferWidth() { return get().frameBufferWidth; }
//
//        /**
//         * @return the FrameBuffer height in pixels of the window.
//         *
//         * This may or not, depending on the system, coincide with {@link #getHeight()}.
//         */
//        public static int getFrameBufferHeight() { return get().frameBufferHeight; }
//    }
//
//    public static class Builder implements WindowBuilder<Window> {
//        private String title = "Ventana";
//        private Options windowOptions = new Options();
//
//        private ApplicationListeners applicationListeners = new ApplicationListeners();
//
//        @Override
//        public Window build() {
//            return new Window(title, windowOptions, applicationListeners);
//        }
//
//        @Override
//        public WindowBuilder<Window> resizable(boolean resizable) {
//            this.windowOptions = this.windowOptions.resizable(resizable);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> updatesPerSecond(int updatesPerSecond) {
//            this.windowOptions = this.windowOptions.updatesPerSecond(updatesPerSecond);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> title(String title) {
//            this.title = title;
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> width(int width) {
//            this.windowOptions = this.windowOptions.width(width);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> height(int height) {
//            this.windowOptions = this.windowOptions.height(height);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> positionX(int xPos) {
//            this.windowOptions = this.windowOptions.positionX(xPos);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> positionY(int yPos) {
//            this.windowOptions = this.windowOptions.positionY(yPos);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> screenMode(WindowMode windowMode) {
//            this.windowOptions = this.windowOptions.windowMode(windowMode);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> alignment(AbstractWindow.Alignment alignment) {
//            this.windowOptions = this.windowOptions.alignment(alignment);
//            return this;
//        }
//
//
//        @Override
//        public WindowBuilder<Window> windowListener(WindowListener listener) {
//            this.applicationListeners = this.applicationListeners.withWindowListener(listener);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> mouseListener(MouseListener listener) {
//            this.applicationListeners = this.applicationListeners.withMouseListener(listener);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> keyListener(KeyListener listener) {
//            this.applicationListeners = this.applicationListeners.withKeyListener(listener);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> gamepadListener(GamepadListener listener) {
//            this.applicationListeners = this.applicationListeners.withGamepadListener(listener);
//            return this;
//        }
//
//        @Override
//        public WindowBuilder<Window> monitorListener(MonitorListener listener) {
//            this.applicationListeners = this.applicationListeners.withMonitorListener(listener);
//            return this;
//        }
//    }
//}