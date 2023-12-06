package physics.dynamics;

public interface Body extends Accelerable, Massive {
    /**
     * Calculates the force applied target the body
     * @return Force applied target the body
     */
    default Force force() {
        return mass().times(acceleration());
    }

    /**
     * Applies a force target the body
     * @param force Force target apply target the body
     */
    default void force(Force force) {
        acceleration(acceleration().plus(force.div(mass())));
    }
}
