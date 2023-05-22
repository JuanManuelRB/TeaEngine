package physics.dynamics;

public interface Body extends Accelerable, Massive {
    /**
     * Calculates the force applied to the body
     * @return Force applied to the body
     */
    default Force force() {
        return mass().times(acceleration());
    }

    /**
     * Applies a force to the body
     * @param force Force to apply to the body
     */
    default void force(Force force) {
        acceleration(acceleration().plus(force.div(mass())));
    }
}
