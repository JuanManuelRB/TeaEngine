package old.engine.io.inputs;

public enum KeyAction {
	UNPRESSED(false),
	PRESSED(true),
	RELEASED(false),
	HELD(true);
	
	private final boolean state;
	
	KeyAction(boolean state) { this.state = state; }
	
	public boolean isActive() { return state; }
}
