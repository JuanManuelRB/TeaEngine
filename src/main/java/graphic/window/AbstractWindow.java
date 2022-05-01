package graphic.window;

import io.inputs.AbstractGamepadListener;
import io.inputs.AbstractKeyListener;
import io.inputs.AbstractMouseListener;

/**
 * This class represents a basic window without logic, which the engine needs to run.
 */
public abstract class AbstractWindow implements AutoCloseable {
    final AbstractWindowListener windowListener;
    final AbstractMouseListener mouseListener;
    final AbstractKeyListener keyListener;
    final AbstractGamepadListener gamepadListener;

    /**
     * Default constructor for the Window implementation.
     * Sets the listeners to the window.
     *
     * @param windowListener An instance of an implementation of AbstractWindowListener.
     */
    public AbstractWindow(
            AbstractWindowListener windowListener,
            AbstractMouseListener mouseListener,
            AbstractKeyListener keyListener,
            AbstractGamepadListener gamepadListener
    ) {
        this.windowListener = windowListener;
        this.mouseListener = mouseListener;
        this.keyListener = keyListener;
        this.gamepadListener = gamepadListener;

    }

    /**
     * Method to create the window and set the context.
     */
    protected abstract void create();

    /**
     * Method to update the window.
     */
    public abstract void update();


    /**
     *
     * @return window listener in use.
     */
    public AbstractWindowListener windowListener() {
        return windowListener;
    }

    /**
     *
     * @return mouse listener in use.
     */
    public AbstractMouseListener mouseListener() {
        return mouseListener;
    }

    /**
     *
     * @return key listener in use.
     */
    public AbstractKeyListener keyListener() {
        return keyListener;
    }

    /**
     *
     * @return gamepad listener in use.
     */
    public AbstractGamepadListener gamepadListener() {
        return gamepadListener;
    }


    public abstract String getTitle();

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

    public abstract boolean closing();
}
