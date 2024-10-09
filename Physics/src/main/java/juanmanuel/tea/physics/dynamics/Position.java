package juanmanuel.tea.physics.dynamics;

public record Position(double e1, double e2, double e3) implements Vector3 {
    public Position {
        if (Double.isNaN(e1) || Double.isNaN(e2) || Double.isNaN(e3))
            throw new IllegalArgumentException("Position cannot be NaN");
    }

    @Override
    public String toString() {
        return "Position[" + e1 + ", " + e2 + ", " + e3 + "]";
    }
}
