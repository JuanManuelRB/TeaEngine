package graphic.window;

import io.inputs.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

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
 * TODO
 *
 */
public final class Window extends AbstractWindow {
    private final long glfwWindow;

    private Window(String title, int width, int height,
                   AbstractWindowListener windowListener,
                   AbstractMouseListener mouseListener,
                   AbstractKeyListener keyListener,
                   AbstractGamepadListener gamepadListener
    ) {
        super(windowListener, mouseListener, keyListener, gamepadListener);

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
    }

    /**
     *
     * @return a builder for the window.
     */
    public static Builder builder() {
        return new Builder();
    }

    public long glfwWindow() {
        return glfwWindow;
    }

    /**
     * Creates the window.
     */
    @Override
    protected void create() {
        // Callbacks de ventana
        glfwSetWindowSizeCallback(glfwWindow(), windowListener::sizeCallback);                // Tamaño de ventana
        glfwSetWindowPosCallback(glfwWindow(), windowListener::positionCallback);             // Posición de ventana
        glfwSetFramebufferSizeCallback(glfwWindow(), windowListener::frameBufferSizeCallback);// Tamaño en pixeles para OpenGL
        glfwSetWindowFocusCallback(glfwWindow(), windowListener::focusCallback);              // Foco
        glfwSetWindowCloseCallback(glfwWindow(), windowListener::closeCallback);              // Cierre

        // Callbacks de ratón
        glfwSetCursorPosCallback(glfwWindow(), mouseListener::mousePosCallback);        // Posición de ratón
        glfwSetMouseButtonCallback(glfwWindow(), mouseListener::mouseButtonCallback);   // Botones de ratón
        glfwSetScrollCallback(glfwWindow(), mouseListener::mouseScrollCallback);        // Desplazamiento de la rueda

        // Callbacks de teclado
        var pkcb = glfwSetKeyCallback(glfwWindow(), keyListener::keyCallback); // Teclado

        //TODO: No existe callback de gamepad, GLFW no lo considera dependiente de la ventana.
//        glfwSetJoystickCallback()
//        glfwSetJoystickUserPointer();

        // Get the thread stack and push a new frame
        try (MemoryStack stack = MemoryStack.stackPush() ) {
            var pWidth = stack.mallocInt(1); // int*
            var pHeight = stack.mallocInt(1); // int*

            // Get the window size.
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

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void update() {
        glfwSwapBuffers(glfwWindow()); // swap the color buffers

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpiar framebuffer

    }

    @Override
    public void close() throws Exception {
        destroy();
    }

    public void destroy() {
        GLFW.glfwWindowShouldClose(glfwWindow());
        GLFW.glfwDestroyWindow(glfwWindow());
        GLFW.glfwTerminate();
    }

    @Override
    public boolean closing() {
        return GLFW.glfwWindowShouldClose(glfwWindow());
    }

    @Override
    public void setClearColor(float red, float green, float blue, float alpha) {
        if (glfwWindow() != NULL)
            glClearColor(red, green, blue, alpha);
    }




    /**
     *
     * @return An {@code int} with the value of the X position of the window.
     */
    @Override
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
    @Override
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
    @Override
    public int getWidth() {
        try ( var stack = MemoryStack.stackPush() ) {
            var pWidth = stack.mallocInt(1);  // int*
            var pHeight = stack.mallocInt(1); // int*

            // Get the window size
            glfwGetWindowSize(glfwWindow(), pWidth, pHeight);

            return pWidth.get(0);
        }
    }

    /**
     *
     * @return An {@code int} with the value of the height of the window.
     */
    @Override
    public int getHeight() {
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            var pWidth = stack.mallocInt(1);  // int*
            var pHeight = stack.mallocInt(1); // int*

            // Get the window size
            glfwGetWindowSize(glfwWindow(), pWidth, pHeight);

            return pHeight.get(0);
        }
    }



    public static class Builder implements WindowBuilder {
        private String title = "Ventana";
        private int width = 480, height = 360;
        private AbstractWindowListener windowListener = WindowListener.get();
        private AbstractMouseListener mouseListener = MouseListener.get();
        private AbstractKeyListener keyListener = KeyListener.get();
        private AbstractGamepadListener gamepadListener = GamepadListener.get();

        @Override
        public AbstractWindow build() {
            return new Window(title, width, height, windowListener, mouseListener, keyListener, gamepadListener);
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public void setHeight(int height) {
            this.height = height;
        }

        @Override
        public void setWindowListener(AbstractWindowListener listener) {
            this.windowListener = listener;
        }

        @Override
        public void setMouseListener(AbstractMouseListener listener) {
            this.mouseListener = listener;
        }

        @Override
        public void setKeyListener(AbstractKeyListener listener) {
            this.keyListener = listener;
        }

        @Override
        public void setGamepadListener(AbstractGamepadListener listener) {
            this.gamepadListener = listener;
        }
    }
}