package aplication;

public non-sealed interface ApplicationSetting extends Setting {
    default void setDimensions(int width, int height) {
        setWidth(width);
        setHeight(height);
    }
    void setWidth(int width);
    void setHeight(int height);
    void setFullScreen(boolean value);
}
