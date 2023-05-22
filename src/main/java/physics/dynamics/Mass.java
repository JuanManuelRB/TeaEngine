package physics.dynamics;

public record Mass(double mass) {
    public Mass plus(Mass other) {
        return new Mass(mass + other.mass());
    }

    public Mass minus(Mass other) {
        return new Mass(mass - other.mass());
    }

    public Mass times(double factor) {
        return new Mass(mass * factor);
    }

    public Mass div(double factor) {
        return new Mass(mass / factor);
    }

    public Force times(Acceleration acceleration) {
        return new Force(acceleration.accelerationVector().times(mass));
    }
}
