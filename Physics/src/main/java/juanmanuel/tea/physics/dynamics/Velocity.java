package juanmanuel.tea.physics.dynamics;

public record Velocity(double e1, double e2, double e3) implements Vector3 {
    public Velocity {
        if (Double.isNaN(e1) || Double.isNaN(e2) || Double.isNaN(e3))
            throw new IllegalArgumentException("Velocity cannot be NaN");
    }
}
