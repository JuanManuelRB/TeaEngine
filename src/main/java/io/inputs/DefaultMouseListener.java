package io.inputs;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;


public class DefaultMouseListener extends MouseListener {
	private static DefaultMouseListener instance = null;
	private final KeyAction[] buttons = new KeyAction[GLFW_MOUSE_BUTTON_LAST];
	private double mouseX, mouseY,  	//Posición del ratón.
			       scrollX, scrollY;	//Desplazamiento de la rueda.
	
	private DefaultMouseListener() {
		this.mouseX = 0f;
		this.mouseY = 0f;
		this.scrollX = 0f;
		this.scrollY = 0f;
		Arrays.fill(buttons, KeyAction.UNPRESSED);
	}

	public static DefaultMouseListener get() {
		if (instance == null)
			instance = new DefaultMouseListener();
		
		return instance;
	}

	@Override
	public void mousePosCallback(long window, double xpos, double ypos) {
		get().mouseX = xpos;
		get().mouseY = ypos;
		
	}

	@Override
	public void mouseButtonCallback(long window, int button, int action, int mods) {
		switch (action) {
			case GLFW_PRESS -> get().buttons[button] = KeyAction.PRESSED;
			case GLFW_REPEAT -> get().buttons[button] = KeyAction.HELD;
			case GLFW_RELEASE -> get().buttons[button] = KeyAction.RELEASED;
			default -> get().buttons[button] = KeyAction.UNPRESSED;

		}
	}

	@Override
	public void mouseScrollCallback(long window, double xOffset, double yOffset) {
		get().scrollX = xOffset;
		get().scrollY = yOffset;
	}

	@Override
	public void mouseEnterCallback(long window, int entered) {
        return; // TODO: Implement
	}

	@Override
	public boolean activeButton(int button) {
		return get().buttons[button] != null && (get().buttons[button].isActive());
	}


	public KeyAction getButton(int glfwButton) {
		return get().buttons[glfwButton];
	}


	public static float getMouseX() {
		return (float)get().mouseX;
	}


	public static float getMouseY() {
		return (float)get().mouseY;
	}

	public static float getScrollX() {
		return (float)get().scrollX;
	}

	public static float getScrollY() {
		return (float)get().scrollY;
	}
	
	

}
