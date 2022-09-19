package aplication;

import graphic.scene.Scene;

import java.util.*;

/**
 * Base class for JVE applications
 */
public abstract class Application {
    private int width, height;
    private String title;
    private boolean fullScreen;


    public Application() {

    }

    public void applySettings(ApplicationSetting appSetting) {

    }


    public class Settings implements ApplicationSetting {
        @Override
        public void setWidth(int width) {
            Application.this.width = width;
        }

        @Override
        public void setHeight(int height) {
            Application.this.height = height;
        }

        @Override
        public void setFullScreen(boolean value) {
            Application.this.fullScreen = value;
        }

        @Override
        public void setName(String title) {
            Application.this.title = title;
        }
    }

    // Scene must be unique
    private final Hashtable<Class<? extends Scene>, Scene> scenes = new Hashtable<>();
    private Class<? extends Scene> startingScene;

    /**
     * Starts the application.
     */
    public final void start() {
        scenes.get(startingScene);
    }

    /**
     * Initialization of the application, called on start.
     */
    public abstract void init();

    /**
     * Adds a new Scene to the pool. Only one instance per class is allowed.
     *
     * @param scene
     * @return
     */
    public final Scene addScene(Scene scene) {
        Scene prev;
        if ((prev = scenes.put(scene.getClass(), scene)) == null)
            setStartingScene(scene.getClass());

        return prev;

    }


    public final Scene removeScene(Class<? extends Scene> scene) {
        return scenes.remove(scene);
    }




    public void setStartingScene(Class<? extends Scene> startingScene) {
        this.startingScene = startingScene;
    }

    public Hashtable<Class<? extends Scene>, Scene> getScenes() {
        return scenes;
    }




}
