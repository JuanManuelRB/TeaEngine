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
    public ApplicationListeners() {
        this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public ApplicationListeners withWindowListener(WindowListener windowListener) {
        return new ApplicationListeners(Optional.of(windowListener), mouseListener, keyListener, gamepadListener, monitorListener);
    }

    public ApplicationListeners withMouseListener(MouseListener mouseListener) {
        return new ApplicationListeners(windowListener, Optional.of(mouseListener), keyListener, gamepadListener, monitorListener);
    }

    public ApplicationListeners withKeyListener(KeyListener keyListener) {
        return new ApplicationListeners(windowListener, mouseListener, Optional.of(keyListener), gamepadListener, monitorListener);
    }

    public ApplicationListeners withGamepadListener(GamepadListener gamepadListener) {
        return new ApplicationListeners(windowListener, mouseListener, keyListener, Optional.of(gamepadListener), monitorListener);
    }

    public ApplicationListeners withMonitorListener(MonitorListener monitorListener) {
        return new ApplicationListeners(windowListener, mouseListener, keyListener, gamepadListener, Optional.of(monitorListener));
    }
}
