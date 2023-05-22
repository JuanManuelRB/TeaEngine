package engine;

/**
 * Executes a logic once. It is a {@link Runnable} so it can be executed in a thread.
 */
public interface Logic extends Runnable {

    /**
     * Executes the logic.
     */
    void update();
    default void run() {
        update();
    }
}
