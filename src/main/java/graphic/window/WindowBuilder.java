package graphic.window;

import io.inputs.AbstractGamepadListener;
import io.inputs.AbstractKeyListener;
import io.inputs.AbstractMouseListener;

public interface WindowBuilder {
    /**
     * Builds a window.
     * @return builded Window
     */
    AbstractWindow build();

    void setTitle(String title);

    void setWidth(int width);
    void setHeight(int height);
    default void setDimension(int width, int height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    void setWindowListener(AbstractWindowListener listener);
    void setMouseListener(AbstractMouseListener listener);
    void setKeyListener(AbstractKeyListener listener);
    void setGamepadListener(AbstractGamepadListener listener);


}
