package engine;

import aplication.ApplicationUpdate;
import aplication.PreStep;
import aplication.Step;
import graphic.render.Renderer;
import graphic.window.AbstractWindow;

/**
 * This interface represents the execution logic of the application.
 */
public abstract class ApplicationLogic extends ApplicationUpdate {
    protected final AbstractWindow window;
    protected final Renderer renderer;
    private int renderUpdatesPerSecond = 30;

    public ApplicationLogic(Renderer renderer, AbstractWindow window) {
        super((PreStep) null, (Step) null, null); // TODO
        this.renderer = renderer;
        this.window = window;
    }

    /**
     * This method is called once before each update.
     */
    public abstract void inputEvents();

    /**
     * Default execution order of the logic.
     * The execution order is the given:
     * {@link #inputEvents() Events} -
     */
    @Override
    synchronized public final void update() {
        inputEvents();
        super.update();
    }

    /**
     * Método donde se implementa la renderización del juego.
     *
     * @throws Exception
     */
    public abstract void render() throws Exception;

    @Override
    public final void close() throws Exception {
        window.close();
        renderer.close();
    }

    public boolean closing() {
        return window.closing();
    }
}
