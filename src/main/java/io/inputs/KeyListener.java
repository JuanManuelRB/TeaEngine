package io.inputs;

import static org.lwjgl.glfw.GLFW.*;


public class KeyListener extends AbstractKeyListener {
	private static KeyListener instance;

	private KeyListener() {}
	
	public static KeyListener get() {
		if(KeyListener.instance == null)
			instance = new KeyListener();
		
		return instance;
		
	}

	@Override
	public void keyCallback(long window, int key, int scancode, int action, int mods) {
		switch (action) {
			case GLFW_PRESS -> get().keys[key] = KeyAction.PRESSED;
			case GLFW_REPEAT -> get().keys[key] = KeyAction.HELD;
			case GLFW_RELEASE -> get().keys[key] = KeyAction.RELEASED;
			default -> get().keys[key] = KeyAction.UNPRESSED;
		};
	}

	/**
	 * Returns true when the key is active.
	 *
	 * @param key An integer which value corresponds to a keyboard key.
	 * @return True if the key is pressed, False if the key is not pressed.
	 */
	@Override
	public boolean activeKey(int key) {
		return get().keys[key] != null && (get().keys[key].isActive());
	}


	@Override
	public KeyAction getKey(int key) {
		return get().keys[key];
	}
}
