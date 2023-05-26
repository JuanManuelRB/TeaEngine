package graphic.window;

import graphic.render.Renderer;
import graphic.render.Viewer;
import io.inputs.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import physics.dynamics.Position;
import physics.dynamics.Size;

import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;


/**
 * This class represents a basic window without logic, which the engine needs to run.
 */
public abstract class AbstractWindow implements Viewer, AutoCloseable {
    final WindowListener windowListener;
    final MouseListener mouseListener;
    final KeyListener keyListener;
    final GamepadListener gamepadListener;
    final MonitorListener monitorListener;
    final Renderer renderer;

    static {
        // El callback puede cambiarse para que sea mas util que solo la salida estandar.
        // The callback can be changed to another output.
        GLFWErrorCallback.createPrint(System.err).set();
    }

    /**
     * Default constructor for the Window implementation.
     * Sets the listeners to the window.
     *
     * @param applicationListeners listeners of the application.
     */
    public AbstractWindow(ApplicationListeners applicationListeners) {

        // Check GLFW initialization
        if (!GLFW.glfwInit())
            throw new IllegalStateException("No ha sido posible inicializar GLFW");

        // If exist listener assign it, else assign default listener. TODO
        this.windowListener = applicationListeners.windowListener().orElse(null);
        this.mouseListener = applicationListeners.mouseListener().orElse(DefaultMouseListener.get());
        this.keyListener = applicationListeners.keyListener().orElse(DefaultKeyListener.get());
        this.gamepadListener = applicationListeners.gamepadListener().orElse(DefaultGamepadListener.get());
        this.monitorListener = applicationListeners.monitorListener().orElse(DefaultMonitorListener.get());

    }

    public static Map<Integer, Long> listMonitors() {
        // return glfwGetMonitors().get();
        return null;
    }

    /**
     * Creates the window and sets the context.
     */
    protected abstract void create();

    /**
     *
     * @return Window listener in use.
     */
    public WindowListener windowListener() {
        return windowListener;
    }

    /**
     *
     * @return Mouse listener in use.
     */
    public MouseListener mouseListener() {
        return mouseListener;
    }

    /**
     *
     * @return Key listener in use.
     */
    public KeyListener keyListener() {
        return keyListener;
    }

    /**
     *
     * @return Gamepad listener in use.
     */
    public GamepadListener gamepadListener() {
        return gamepadListener;
    }



    /**
     *
     * @return Title of the window.
     */
    public abstract String getTitle();

    /**
     *
     * @return The X position of the window.
     */
    public abstract int getPosX();

    /**
     *
     * @return The Y position of the window.
     */
    public abstract int getPosY();

    /**
     * 
     * @return The position of the window.
     */
    public Position getPosition() {
        return new Position(getPosX(), getPosY(), 0);
    }

    /**
     *
     * @return The width of the window.
     */
    public abstract int getWidth();

    /**
     *
     * @return The height of the window.
     */
    public abstract int getHeight();

    public Size getSize() {
        return new Size(getWidth(), getHeight(), 0);
    }

    public abstract void setSize(Size size);

    public abstract void setPosition(Position position);

    public abstract void align(Alignment alignment);

    @Override
    public void close() throws Exception {
        if (getContext() == 0)
            throw new IllegalStateException("The context is not set");

        glfwWindowShouldClose(getContext());
        GLFW.glfwDestroyWindow(getContext());
        GLFW.glfwTerminate();
    }

    public abstract void setClearColor(float red, float green, float blue, float alpha);//TODO: remove, only for testing

    public boolean closing() {
        return glfwWindowShouldClose(getContext());
    }

    public enum WindowMode {
        WINDOWED,
        BORDERLESS,
        FULLSCREEN
    }

    public enum Alignment {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        CENTER
    }

}
