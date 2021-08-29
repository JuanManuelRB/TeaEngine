package engine.io.inputs;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;


public class KeyListener {
	private static KeyListener instance;
	private final KeyAction[] keys = new KeyAction[GLFW_KEY_LAST];
	
	private KeyListener() {
		Arrays.fill(keys, KeyAction.UNPRESSED);
	}
	
	public static KeyListener get() {
		if(KeyListener.instance == null)
			instance = new KeyListener();
		
		return instance;
		
	}
	
	public static void keyCallback(long window, int key, int scancode, int action, int mods) {
		switch (action) {
			case GLFW_PRESS -> get().keys[key] = KeyAction.PRESSED;
			case GLFW_REPEAT -> get().keys[key] = KeyAction.HELD;
			case GLFW_RELEASE -> get().keys[key] = KeyAction.RELEASED;
			default -> get().keys[key] = KeyAction.UNPRESSED;
		};
	}

	/**
	 *
	 * @param key An integer which value corresponds to a keyboard key.
	 * @return True if the key is pressed, False if the key is not pressed.
	 */
	public static boolean activeKey(int key) {
		return get().keys[key] != null && (get().keys[key].isActive());
	}

	public static KeyAction getTecla(int key) {
		return get().keys[key];
	}
}
