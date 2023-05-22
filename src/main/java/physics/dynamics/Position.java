package physics.dynamics;

import juanmanuel.gealma.threedimensional.MathGeometric3;
import juanmanuel.gealma.threedimensional.Vector3;

import java.util.Objects;

public record Position(Vector3 positionVector) {
    public Position {
        Objects.requireNonNull(positionVector);
    }
    public Position(double x, double y, double z) {
        this(new Vector3(x, y, z));
    }

    public double x() {
        return positionVector.e1().value();
    }

    public double y() {
        return positionVector.e2().value();
    }

    public double z() {
        return positionVector.e3().value();
    }

    public Position move(Vector3 other) {
        return new Position(positionVector.plus(other));
    }

    public Vector3 distanceVector(Position other) {
        return this.positionVector.minus(other.positionVector);
    }

    public double distanceTo(Position other) {
        return distanceVector(other).magnitude();
    }

    /**
     * Realizes the interpolation with another position linearly by a value
     *
     * @param other Position to interpolate with.
     * @param value Quantity of the interpolation.
     * @return A Position result of interpolating with the other position by a value.
     */
    public Position lerp(Position other, double value) {
        return new Position(MathGeometric3.lerp(this.positionVector, other.positionVector, value));
    }

    public Position slerp(Position other, double value) {
        return new Position(MathGeometric3.slerp(this.positionVector, other.positionVector, value));
    }

    public Position nlerp(Position other, double value) {
        return new Position(MathGeometric3.nlerp(this.positionVector, other.positionVector, value));
    }
}
