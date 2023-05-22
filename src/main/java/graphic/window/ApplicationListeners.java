package graphic.window;

import io.inputs.GamepadListener;
import io.inputs.KeyListener;
import io.inputs.MonitorListener;
import io.inputs.MouseListener;

import java.util.Optional;

public record ApplicationListeners(Optional<WindowListener> windowListener,
                                   Optional<MouseListener> mouseListener,
                                   Optional<KeyListener> keyListener,
                                   Optional<GamepadListener> gamepadListener,
                                   Optional<MonitorListener> monitorListener) {
}
