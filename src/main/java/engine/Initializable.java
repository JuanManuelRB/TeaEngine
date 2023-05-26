package engine;

@FunctionalInterface
public interface Initializable {
    /**
     * Initializes the logic. This method is called once before the first step.
     *
     * @throws InitializationException when the logic can't be initialized.
     */
    public abstract void init() throws InitializationException;
}
