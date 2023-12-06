package physics.dynamics;


import juanmanuel.gealma.vga.vga3.Vector3;

public interface Positionable {

    /**
     * TODO
     * @return The position of the object.
     */
    Vector3 position();

    /**
     * Sets the position.
     * @param position the new position
     */
    void position(Vector3 position);
}
