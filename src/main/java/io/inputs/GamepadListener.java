package io.inputs;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;


public class GamepadListener extends AbstractGamepadListener {//TODO
	private static GamepadListener gamepadInstance;
	private final int[] gamepads = new int[GLFW_JOYSTICK_LAST];
	
	
	private GamepadListener() {
		Arrays.fill(gamepads, 0);
		
	}
	
	public static GamepadListener get() {
		if(gamepadInstance == null)
			gamepadInstance = new GamepadListener();
		
		return gamepadInstance;
	}
	// TODO
	public static void joystickCallback(int jid, int event) {
//		switch (event) {
//			case GLFW_CONNECTED:
//				for (int i = 0; i < get().gamepads.length; i++) {
//					if(get().gamepads[i] == 0 || get().gamepads[i] == jid) {
//						get().gamepads[i] = jid;
//						break;
//
//					} else if (get().gamepads[i] != 0) {
//						continue;
//
//					}
//				}
//				break;
//
//			case GLFW_DISCONNECTED:
//				for (int i = 0; i < get().gamepads.length; i++) {
//					if(get().gamepads[i] == jid) {
//						get().gamepads[i] = 0;
//						break;
//
//					}
//				}
//				break;
//
//			default:
//				throw new IllegalArgumentException("Unexpected value: " + event);
//
//		}
		switch (event) {
			case GLFW_CONNECTED -> {
				for (int i = 0; i < get().gamepads.length; i++) {
					if(get().gamepads[i] == 0 || get().gamepads[i] == jid) {
						get().gamepads[i] = jid;
						break;

					} else if (get().gamepads[i] != 0) {
						continue;

					}
				}
			}

			case GLFW_DISCONNECTED -> {
				for (int i = 0; i < get().gamepads.length; i++) {
					if(get().gamepads[i] == jid) {
						get().gamepads[i] = 0;
						break;

					}
				}

			}

			default -> throw new IllegalArgumentException("Unexpected value: " + event);
		}
	}
	
	
	public static int[] getGamepads() {
		return get().gamepads;

	}

}
