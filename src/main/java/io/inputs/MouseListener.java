package io.inputs;

public abstract class MouseListener {
    public abstract void mousePosCallback(long window, double xpos, double ypos);

    public abstract void mouseButtonCallback(long window, int button, int action, int mods);

    public abstract void mouseScrollCallback(long window, double xOffset, double yOffset);

    public abstract void mouseEnterCallback(long window, int entered);

    public abstract boolean activeButton(int button);
}
