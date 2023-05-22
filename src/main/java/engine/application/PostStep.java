package engine.application;

/**
 * Logic that executes once after the steps cicle.
 */
@FunctionalInterface
public interface PostStep {
    void postStep();
}
