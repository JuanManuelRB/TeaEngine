package graphic.window;

import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public abstract class AbstractWindow {
    AbstractWindowListener windowListener;

    /**
     *
     * @param windowListener An instance of an implementation of AbstractWindowListener.
     */
    AbstractWindow(AbstractWindowListener windowListener) {
        this.windowListener = windowListener;
    }

    public abstract void update();

    protected abstract void create();


    /**
     *
     * @return An {@code int} with the value of the X position of the window.
     */
    public abstract int getPosX();

    /**
     *
     * @return An {@code int} with the value of the Y position of the window.
     */
    public abstract int getPosY();

    /**
     *
     * @return An {@code int} with the value of the width of the window.
     */
    public abstract int getWidth();

    /**
     *
     * @return An {@code int} with the value of the height of the window.
     */
    public abstract int getHeight();

    public abstract void setClearColor(float red, float green, float blue, float alpha);
}
