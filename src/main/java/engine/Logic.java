package engine;

import engine.graphic.Window;

/**
 * This interface represents the execution logic of the application.
 */
public interface Logic {

    /**
     * Init method of the application. All the initialization should be made here.
     *
     * @throws Exception Initialization exception.
     */
    void init() throws Exception;

    /**
     * Events are executed before any other step.
     */
    void inputEvents() ;

    /**
     * This step is executed once before the main steps.
     */
    void firstStep();

    /**
     * Main body of the code.
     * @param updates Number of times the code should be executed.
     */
    void mainSteps(int updates);

    /**
     * This step is executed once after the main steps.
     */
    void lastStep();


    /**
     * Default execution order of the logic.
     * The execution order is the given:
     *
     * {@link #inputEvents() Events} -
     * {@link #firstStep() First Step} -
     * {@link #mainSteps(int updates) Main Steps} -
     * {@link #lastStep() Last Step}
     *
     * @param updates Number of updates of the main body.
     */
    default void updateLogic(int updates) {
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
    void render(Window window) throws Exception;

    /**
     * Finalize
     */
    void end();
}
