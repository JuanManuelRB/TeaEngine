package graphic.scene;


public interface Scene {
    public boolean setParent(Scene parent);

    public boolean addChild(Scene child);
}
