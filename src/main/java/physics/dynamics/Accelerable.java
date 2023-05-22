package physics.dynamics;

public interface Accelerable extends Kinetic {
    Acceleration acceleration();
    void acceleration(Acceleration acceleration);
}
