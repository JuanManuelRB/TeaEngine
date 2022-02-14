package graphic.window;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import io.inputs.KeyListener;
import io.inputs.MouseListener;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Main Window of the application.
 *
 * Holds a reference to the GLFW window and implements methods to change its behavior
 *
 */
public final class Window extends AbstractWindow implements AutoCloseable { // TODO: make AbstractWindow?
    private static Window windowInstance = null;
    private final long glfwWindow;
    private final static int DEFAULT_WIDTH = 400, DEFAULT_HEIGHT = 300;
    private final static String DEFAULT_TITLE = "Ventana";

    private Window() {
        this(null);
    }

    private Window(AbstractWindowListener windowListener) {
        super(windowListener);

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
        this.glfwWindow = glfwCreateWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_TITLE, NULL, NULL);

        if (glfwWindow == NULL)
            throw new IllegalStateException("Error: Window could not be created");
    }

    // Singleton
    public static Window get() {
        if (windowInstance == null) {
            windowInstance = new Window();
            windowInstance.create();
        }

        return windowInstance;
    }

    public void setWindowListener(AbstractWindowListener listener) {
        if (get().windowListener != null)
            get().windowListener = listener;
        else
            throw new RuntimeException("The Window Listener is already set");
    }

    public static long glfwWindow() {
        return get().glfwWindow;
    }

    /**
     * Creates the window.
     */
    protected void create() {
        // Callbacks de ventana
        if (get().windowListener != null) {
            glfwSetWindowSizeCallback(glfwWindow(), get().windowListener::sizeCallback);                  //Tamaño de ventana
            glfwSetWindowPosCallback(glfwWindow(), get().windowListener::positionCallback);               //Posición de ventana
            glfwSetFramebufferSizeCallback(glfwWindow(), get().windowListener::frameBufferSizeCallback);  //Tamaño en pixeles para OpenGL
            glfwSetWindowFocusCallback(glfwWindow(), get().windowListener::focusCallback);
            glfwSetWindowCloseCallback(glfwWindow(), get().windowListener::closeCallback);

        }

        // Callbacks de ratón
        glfwSetCursorPosCallback(glfwWindow(), MouseListener::mousePosCallback);        // Posición de ratón
        glfwSetMouseButtonCallback(glfwWindow(), MouseListener::mouseButtonCallback);   // Botones de ratón
        glfwSetScrollCallback(glfwWindow(), MouseListener::mouseScrollCallback);        // Desplazamiento de la rueda

        // Callbacks de teclado
        glfwSetKeyCallback(glfwWindow(), KeyListener::keyCallback); // Teclado

        //TODO: No existe callback de gamepad, GLFW no lo considera dependiente de la ventana.


        // Get the thread stack and push a new frame
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(glfwWindow(), pWidth, pHeight);

            // Obtener resolución del monitor primario
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Centrar la ventana
            assert videoMode != null;
            glfwSetWindowPos(
                    glfwWindow(),
                    (videoMode.width() - pWidth.get(0)) / 2,
                    (videoMode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        glfwMakeContextCurrent(glfwWindow());

        // Equivalente a usar VSync, se actualiza según la tasa de refresco de la ventana
        glfwSwapInterval(1);

        //Mostrar la ventana
        glfwShowWindow(glfwWindow());

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    }

    public void update() {
        glfwSwapBuffers(glfwWindow()); // swap the color buffers

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpiar framebuffer

    }

    public boolean closeWindow() {
        return GLFW.glfwWindowShouldClose(glfwWindow());
    }

    @Override
    public void setClearColor(float red, float green, float blue, float alpha) {
        if (glfwWindow() != NULL)
            glClearColor(red, green, blue, alpha);
    }

    public void destroy() {
        GLFW.glfwWindowShouldClose(get().glfwWindow);
        GLFW.glfwDestroyWindow(get().glfwWindow);
        GLFW.glfwTerminate();
        windowInstance = null;
    }

    @Override
    public void close() throws Exception {
        destroy();
    }

    /**
     *
     * @param title the title of the window.
     * @return a {@link #Window} instance
     */
    public static Window withTitle(String title) {//TODO: la clase ha dejado de guardar el tamaño?¿
        glfwSetWindowTitle(glfwWindow(), title);
        return get();
    }

    /**
     *
     * @param width New width of the window
     * @return A {@link #Window Window} instance with the width updated
     */
    public static Window withWidth(int width) {
        glfwSetWindowSize(glfwWindow(), width, get().getHeight());
        return get();

    }

    /**
     *
     * @param height New height of the window
     * @return A {@link #Window Window} instance with the heigth updated
     */
    public static Window withHeight(int height) {
        glfwSetWindowSize(glfwWindow(), get().getWidth(), height);
        return get();
    }

    /**
     *
     * @param width
     * @param height
     * @return
     */
    private static Window withDimensions(int width, int height) {
        return withWidth(width).withHeight(height);

    }

    /**
     *
     * @param positionX New position of the window in the X axis.
     * @return A {@link #Window Window} instance with the position updated.
     */
    public static Window withPositionX(int positionX) {
        glfwSetWindowPos(glfwWindow(), positionX, get().getPosY());
        return get();
    }

    /**
     *
     * @param positionY New position of the window in the Y axis.
     * @return A {@link #Window Window} instance with the position updated.
     */
    public static Window withPositionY(int positionY) {
        glfwSetWindowPos(glfwWindow(), get().getPosX(), positionY);
        return get();
    }

    /**
     *
     * @param posX New position of the window in the X axis.
     * @param posY New position of the window in the Y axis.
     * @return A {@link #Window Window} instance with the position updated.
     */
    private static Window withPosition(int posX, int posY) {
        return withPositionX(posX).withPositionY(posY);

    }

    /**
     *
     * @return An {@code int} with the value of the X position of the window.
     */
    public int getPosX() {
        try(var stack = MemoryStack.stackPush()) {
            var pPosX = stack.mallocInt(1);
            var pPosY = stack.mallocInt(1);

            glfwGetWindowPos(glfwWindow(), pPosX, pPosY);

            return pPosX.get();
        }
    }

    /**
     *
     * @return An {@code int} with the value of the Y position of the window.
     */
    public int getPosY() {
        try(var stack = MemoryStack.stackPush()) {
            var pPosX = stack.mallocInt(1);
            var pPosY = stack.mallocInt(1);

            glfwGetWindowPos(glfwWindow(), pPosX, pPosY);

            return pPosY.get();
        }
    }

    /**
     *
     * @return An {@code int} with the value of the width of the window.
     */
    public int getWidth() {
        try ( var stack = MemoryStack.stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);  // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size
            glfwGetWindowSize(glfwWindow(), pWidth, pHeight);

            return pWidth.get(0);
        }
    }

    /**
     *
     * @return An {@code int} with the value of the height of the window.
     */
    public int getHeight() {
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);  // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size
            glfwGetWindowSize(get().glfwWindow, pWidth, pHeight);

            return pHeight.get(0);
        }
    }

}

