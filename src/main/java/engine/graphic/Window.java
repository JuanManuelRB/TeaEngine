package engine.graphic;

import java.io.Closeable;
import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import engine.io.inputs.KeyListener;
import engine.io.inputs.MouseListener;
import engine.io.inputs.WindowListener;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryUtil.NULL;



/**
 * game.Main Window of the application.
 *
 * La ventana creada es una ventana de GLFW y por lo tanto las acciones que se lleven a cabo deben estar asociadas
 * a esta.
 */
public final class Window implements AutoCloseable { // TODO: make AbstractWindow?
    private static Window windowInstance = null;
    private long glfwWindow;

    private Window() {}

    // Singleton
    public static Window get() {
        if (windowInstance == null) {
            windowInstance = new Window();
            Window.create();
        }

        return windowInstance;
    }


    /**
     * Creates the window.
     */
    private static void create() {
        // Configurar GLFW
        glfwDefaultWindowHints();                  // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);  // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        final int defaultWidth= 300, defaultHeight = 200; // This should be provided by the startup method
        final String defaultWindowTitle = "Ventana"; // Also this

        // TODO: create function that associates the window with a monitor or puts its value to NULL if the window should be fullscreen.
        // Crear la ventana
        get().glfwWindow = glfwCreateWindow(defaultWidth, defaultHeight, defaultWindowTitle, NULL, NULL);

        if (get().glfwWindow == NULL)
            throw new RuntimeException("Error: Window could not be created");

        // Callbacks de ventana
        glfwSetWindowSizeCallback(get().glfwWindow, WindowListener::sizeCallback); //Tamaño de ventana
        glfwSetWindowPosCallback(get().glfwWindow, WindowListener::positionCallback); //Posición de ventana
        glfwSetFramebufferSizeCallback(get().glfwWindow, WindowListener::frameBufferSizeCallback); //Tamaño en pixeles para OpenGL
        glfwSetWindowFocusCallback(get().glfwWindow, WindowListener::focusCallback);
        glfwSetWindowCloseCallback(get().glfwWindow, WindowListener::closeCallback);

        // Callbacks de ratón
        glfwSetCursorPosCallback(get().glfwWindow, MouseListener::mousePosCallback); // Posición de ratón
        glfwSetMouseButtonCallback(get().glfwWindow, MouseListener::mouseButtonCallback); // Botones de ratón
        glfwSetScrollCallback(get().glfwWindow, MouseListener::mouseScrollCallback); // Desplazamiento de la rueda

        // Callbacks de teclado
        glfwSetKeyCallback(get().glfwWindow, KeyListener::keyCallback); // Teclas

        //TODO: No existe callback de gamepad, GLFW no lo considera dependiente de la ventana.


        // Get the thread stack and push a new frame
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(get().glfwWindow, pWidth, pHeight);

            // Obtener resolución del monitor primario
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Centrar la ventana
            assert videoMode != null;
            glfwSetWindowPos(
                    get().glfwWindow,
                    (videoMode.width() - pWidth.get(0)) / 2,
                    (videoMode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        glfwMakeContextCurrent(get().glfwWindow);

        // Equivalente a usar VSync, se actualiza según la tasa de refresco de la ventana
        glfwSwapInterval(1);

        //Mostrar la ventana
        glfwShowWindow(get().glfwWindow);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    }

    public static void update() {
        glfwSwapBuffers(get().glfwWindow); // swap the color buffers

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpiar framebuffer

    }

    public static boolean closeWindow() {
        return GLFW.glfwWindowShouldClose(get().glfwWindow);
    }

    public static void setClearColor(float red, float green, float blue, float alpha) {
        if (get().glfwWindow != NULL)
            glClearColor(red, green, blue, alpha);
    }

    public static void destroy() {
        GLFW.glfwWindowShouldClose(get().glfwWindow);
        GLFW.glfwDestroyWindow(get().glfwWindow);
        GLFW.glfwTerminate();
        windowInstance = null;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     *
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        destroy();
    }

    /**
     *
     * @param titulo the title of the window.
     * @return a {@link #Window} instance
     */
    public static Window withTitle(String titulo) {//TODO: la clase ha dejado de guardar el tamaño?¿
        glfwSetWindowTitle(get().glfwWindow, titulo);
        return get();
    }

    /**
     *
     * @param width New width of the window
     * @return A {@link #Window Window} instance with the width updated
     */
    public static Window withWidth(int width) {
        glfwSetWindowSize(get().glfwWindow, width, WindowListener.getHeight());
        return get();

    }

    /**
     *
     * @param height New height of the window
     * @return A {@link #Window Window} instance with the heigth updated
     */
    public static Window withHeight(int height) {
        glfwSetWindowSize(get().glfwWindow, WindowListener.getWidth(), height);
        return get();
    }

    /**
     *
     * @param positionX New position of the window in the X axis
     * @return A {@link #Window Window} instance with the position updated
     */
    public static Window withPositionX(int positionX) {
        glfwSetWindowPos(get().glfwWindow, positionX, WindowListener.getPosY());
        return get();
    }

    /**
     *
     * @param positionY New position of the window in the Y axis
     * @return A {@link #Window Window} instance with the position updated
     */
    public static Window withPositionY(int positionY) {
        glfwSetWindowPos(get().glfwWindow, WindowListener.getPosX(), positionY);//TODO: this should not depend on WindowListener?
        return get();
    }

    /**
     *
     * @return An {@code int} with the value of the width of the window.
     */
    public static int getWidth() {
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size
            glfwGetWindowSize(get().glfwWindow, pWidth, pHeight);

            return pWidth.get(0);
        }
    }

    /**
     *
     * @return An {@code int} with the value of the height of the window.
     */
    public static int getHeight() {
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size
            glfwGetWindowSize(get().glfwWindow, pWidth, pHeight);

            return pHeight.get(0);
        }
    }

    private static Window withDimensions(int width, int height) {
        return withWidth(width).withHeight(height);

    }

    private static Window withPosition(int posX, int posY) {
        return withPositionX(posX).withPositionY(posY);

    }

}

