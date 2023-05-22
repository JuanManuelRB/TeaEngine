package physics.dynamics;

public interface Positionable {
    Position position();

    /**
     * Sets the position.
     * @param position the new position
     */
    void position(Position position);
}
