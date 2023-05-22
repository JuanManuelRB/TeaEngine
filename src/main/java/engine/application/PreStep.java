package engine.application;

/**
 * Logic that executes once before the steps cicle.
 */
@FunctionalInterface
public interface PreStep {
    void preStep();
}
