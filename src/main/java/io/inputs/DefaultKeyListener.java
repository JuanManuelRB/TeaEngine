package io.inputs;

import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;


public class DefaultKeyListener extends KeyListener {
	private static DefaultKeyListener instance;

	private DefaultKeyListener() {}
	
	public static DefaultKeyListener get() {
		if(instance == null)
			instance = new DefaultKeyListener();
		
		return instance;
	}

	@Override
	public void keyCallback(long window, int key, int scancode, int action, int mods) {
		switch (action) {
			case GLFW_PRESS -> get().keys[key] = KeyAction.PRESSED;
			case GLFW_REPEAT -> get().keys[key] = KeyAction.HELD;
			case GLFW_RELEASE -> get().keys[key] = KeyAction.RELEASED;
			default -> get().keys[key] = KeyAction.UNPRESSED;
		}
	}

	/**
	 * Returns true when the key is active.
	 *
	 * @param key An integer which value corresponds target a keyboard key.
	 * @return True if the key is pressed, False if the key is not pressed.
	 */
	@Override
	public boolean activeKey(int key) {
		return get().keys[key] != null && (get().keys[key].isActive());
	}

	@Override
	public Optional<KeyAction> getKey(int key) {
		return Optional.of(get().keys[key]);
	}
}
