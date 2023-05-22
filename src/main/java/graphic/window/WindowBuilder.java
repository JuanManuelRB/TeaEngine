package graphic.window;

import io.inputs.GamepadListener;
import io.inputs.KeyListener;
import io.inputs.MonitorListener;
import io.inputs.MouseListener;

public interface WindowBuilder<T extends AbstractWindow> {
    /**
     * Builds a window.
     * @return builded Window
     */
    T build();

    WindowBuilder<T> title(String title);

    WindowBuilder<T> width(int width);

    WindowBuilder<T> height(int height);

    default WindowBuilder<T> withDimensions(int width, int height) {
        return this.width(width).height(height);
    }

    default WindowBuilder<T> withDimensions(Size size) {
        return this.width((int) size.width()).height((int) size.height());
    }

    WindowBuilder<T> positionX(int xPos);

    WindowBuilder<T> positionY(int yPos);

    default WindowBuilder<T> withPosition(int x, int y) {
        return this.positionX(x).positionY(y);
    }

    default WindowBuilder<T> withPosition(Position position) {
        return this.positionX((int) position.x()).positionY((int) position.y());
    }

    WindowBuilder<T> screenMode(AbstractWindow.WindowMode windowMode);

    WindowBuilder<T> alignment(AbstractWindow.Alignment alignment);


    WindowBuilder<T> windowListener(WindowListener listener);
    WindowBuilder<T> mouseListener(MouseListener listener);
    WindowBuilder<T> keyListener(KeyListener listener);
    WindowBuilder<T> gamepadListener(GamepadListener listener);
    WindowBuilder<Window> monitorListener(MonitorListener listener);


}
