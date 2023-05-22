package physics.dynamics;

public interface Kinetic extends Positionable {
    /**
     * Calculates the velocity.
     * @return Velocity
     */
    Velocity velocity();

    /**
     * Sets the velocity.
     * @param velocity the new velocity
     */
    void velocity(Velocity velocity);
}
