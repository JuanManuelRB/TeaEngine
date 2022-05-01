package io.inputs;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;

public abstract class AbstractKeyListener {
    protected final KeyAction[] keys = new KeyAction[GLFW_KEY_LAST];

    public AbstractKeyListener() {
        Arrays.fill(keys, KeyAction.UNPRESSED);
    }


    /**
     * The callback method needed by GLFW.
     *
     * @param window a GLFW window instance, a long.
     * @param key the keyboard key that was pressed or released.
     * @param scancode the platform-specific scancode of the key.
     * @param action the key action.
     * @param mods bitfield describing which modifiers keys were held down
     */
    public abstract void keyCallback(long window, int key, int scancode, int action, int mods);

    public abstract boolean activeKey(int key);

    /**
     * Evaluates a key and returns a KeyAction enum instance.
     *
     * If the key is pressed returns PRESSED.
     * If the key is held returns HELD.
     * If the key is released returns RELEASED.
     * If the key is not pressed returns UNPRESSED.
     *
     * @param key the keyboard key to evaluate.
     * @return a KeyAction.
     */
    public abstract KeyAction getKey(int key);
}
