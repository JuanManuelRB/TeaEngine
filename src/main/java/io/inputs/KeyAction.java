package io.inputs;

/**
 * A simple enum that holds the possible states of a key.
 *
 */
public enum KeyAction {
	UNPRESSED(false),
	PRESSED(true),
	RELEASED(false),
	HELD(true);

	private final boolean state;
	
	KeyAction(boolean state) { this.state = state; }

	/**
	 *
	 * @return a boolean that is true when the key is PRESSED or HELD and false when is UNPRESSED or RELEASED.
	 */
	public boolean isActive() { return state; }
}
