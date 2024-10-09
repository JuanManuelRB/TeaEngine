package juanmanuel.tea.physics;

import juanmanuel.tea.components.Updated;
import juanmanuel.tea.physics.dynamics.Position;
import juanmanuel.tea.physics.dynamics.Velocity;

public interface PhysicsUpdated extends Updated {
    void updatePhysics();
    Position position();

    void move();

    void position(Position position);

    void position(double e1, double e2, double e3);

    void velocity(Velocity velocity);

    void velocity(double e1, double e2, double e3);

    Velocity velocity();
    boolean collides(PhysicsUpdated other);
}
