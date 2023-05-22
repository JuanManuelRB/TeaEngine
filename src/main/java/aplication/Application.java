package aplication;

import graphic.scene.Scene;
import graphic.window.Window;

public abstract class Application implements Runnable {
    private Scene<?> mainScene;
    private Window window;

    public Application() {
        window = Window.builder().build();
    }

    public Application setMainScene(Scene<?> mainScene) {
        this.mainScene = mainScene;
        return this;
    }

    @Override
    public final void run() {

    }
}
