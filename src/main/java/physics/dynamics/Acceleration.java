package physics.dynamics;

import juanmanuel.gealma.threedimensional.Vector3;

public record Acceleration(Vector3 accelerationVector) {
    public Force times(Mass mass) {
        return new Force(accelerationVector.times(mass.mass()));
    }

    public Acceleration plus(Acceleration other) {
        return new Acceleration(accelerationVector.plus(other.accelerationVector()));
    }

    public Acceleration minus(Acceleration other) {
        return new Acceleration(accelerationVector.minus(other.accelerationVector()));
    }

    public Acceleration times(double factor) {
        return new Acceleration(accelerationVector.times(factor));
    }

    public Acceleration div(double factor) {
        return new Acceleration(accelerationVector.div(factor));
    }
}
