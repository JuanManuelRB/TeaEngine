package graphic.window;

import aplication.Size;
import aplication.Position;
import graphic.render.Viewer;
import graphic.scene.View;
import io.inputs.AbstractGamepadListener;
import io.inputs.AbstractKeyListener;
import io.inputs.AbstractMouseListener;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;


/**
 * This class represents a basic window without logic, which the engine needs to run.
 */
public abstract class AbstractWindow implements Viewer, AutoCloseable {
    final AbstractWindowListener windowListener;
    final AbstractMouseListener mouseListener;
    final AbstractKeyListener keyListener;
    final AbstractGamepadListener gamepadListener;

    static {
        // El callback puede cambiarse para que sea mas util que solo la salida estandar.
        // The callback can be changed to another output.
        GLFWErrorCallback.createPrint(System.err).set();
    }

    /**
     * Default constructor for the Window implementation.
     * Sets the listeners to the window.
     *
     * @param windowListener AN instance of AbstractWindowListener.
     * @param mouseListener AN instance of AbstractMouseListener.
     * @param keyListener AN instance of AbstractKeyListener.
     * @param gamepadListener AN instance of AbstractGamepadListener.
     */
    public AbstractWindow(
            AbstractWindowListener windowListener,
            AbstractMouseListener mouseListener,
            AbstractKeyListener keyListener,
            AbstractGamepadListener gamepadListener
        ) {

        // Check GLFW initialization
        if (!GLFW.glfwInit())
            throw new IllegalStateException("No ha sido posible inicializar GLFW");

        this.windowListener = windowListener;
        this.mouseListener = mouseListener;
        this.keyListener = keyListener;
        this.gamepadListener = gamepadListener;

    }

    /**
     * Creates the window and sets the context.
     */
    protected abstract void create();

    /**
     *
     * @return Window listener in use.
     */
    public AbstractWindowListener windowListener() {
        return windowListener;
    }

    /**
     *
     * @return Mouse listener in use.
     */
    public AbstractMouseListener mouseListener() {
        return mouseListener;
    }

    /**
     *
     * @return Key listener in use.
     */
    public AbstractKeyListener keyListener() {
        return keyListener;
    }

    /**
     *
     * @return Gamepad listener in use.
     */
    public AbstractGamepadListener gamepadListener() {
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
