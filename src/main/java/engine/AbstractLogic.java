package engine;

import graphic.render.Renderer;
import graphic.window.AbstractWindow;
import graphic.window.Window;
import org.jetbrains.annotations.NotNull;

/**
 * This interface represents the execution logic of the application.
 */
public abstract class AbstractLogic {
    protected AbstractWindow window;
    protected final Renderer renderer;

    public AbstractLogic(@NotNull Renderer renderer) {
        this.renderer = renderer;
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
     * {@link #mainSteps(int updates) game.Main Steps} -
     * {@link #lastStep() Last Step}
     *
     * @param updates Number of updates of the main body.
     */
    public final void updateLogic(int updates) {
        inputEvents();
        firstStep();
        mainSteps(updates);
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
    public abstract void end();
}
