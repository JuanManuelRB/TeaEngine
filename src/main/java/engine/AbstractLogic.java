package engine;

import graphic.render.Renderer;
import graphic.window.AbstractWindow;
import org.jetbrains.annotations.NotNull;


/**
 * This interface represents the execution logic of the application.
 */
public abstract class AbstractLogic implements AutoCloseable, Logic {
    protected final AbstractWindow window;
    protected final Renderer renderer;
    private int ups = 30;

    public AbstractLogic(@NotNull Renderer renderer, @NotNull AbstractWindow window) {
        this.renderer = renderer;
        this.window = window;
    }

    /**
     * Init method of the application. All the initialization should be made here.
     *
     * @throws Exception Initialization exception.
     */
    public abstract void init() throws Exception;

    /**
     * Events are executed before any other step.
     */
    public abstract void inputEvents() ;

    /**
     * This step is executed once before the main steps.
     */
    public abstract void firstStep();

    /**
     * Main body of the code.
     * @param updates Number of times the code should be executed.
     */
    public abstract void mainSteps(int updates);

    public abstract void mainSteps(int update, int updates);


    /**
     * This step is executed once after the main steps.
     */
    public abstract void lastStep();


    /**
     * Default execution order of the logic.
     * The execution order is the given:
     *
     * {@link #inputEvents() Events} -
     * {@link #firstStep() First Step} -
     * {@link #mainSteps(int updates) Main Steps} -
     * {@link #lastStep() Last Step}
     */
    @Override
    synchronized public final void update() {
        inputEvents();
        firstStep();
        for (int i = 0; i < ups; i++) {
            mainSteps(i, ups); //TODO: Concurrent updates?
        }
        lastStep();
    }

    /**
     * Método donde se implementa la renderización del juego.
     *
     *
     * @throws Exception
     */
    public abstract void render() throws Exception;

    /**
     * Finalize
     */
    public void end() {
        renderer.cleanup();

    }

    public boolean closing() {
        return window.closing();
    }
}
