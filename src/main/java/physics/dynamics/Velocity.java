package physics.dynamics;

import juanmanuel.gealma.vga.vga3.Vector3;

import java.util.Objects;

/**
 * Velocity is a vector that represents the speed of an object.
 * @param vector The vector that represents the speed of an object.
 */
public record Velocity(Vector3 vector) implements Comparable<Velocity> {
    public Velocity {
        Objects.requireNonNull(vector);
    }

    /**
     * Creates a velocity with the given vector.
     * @param x The x component of the velocity.
     * @param y The y component of the velocity.
     * @param z The z component of the velocity.
     */
    public Velocity(double x, double y, double z) {
        this(new Vector3(x, y, z));
    }

    /**
     * Creates a velocity with the default vector (0, 0, 0).
     */
    public Velocity() {
        this(new Vector3());
    }

    /**
     * Creates a velocity with the given vectors and time interval.
     * @param initialPosition Initial position of the object.
     * @param endPosition Final position of the object.
     * @param time Interval of time between the initial and final position.
     * @return A velocity that represents the speed of an object.
     */
    public static Velocity from(Vector3 initialPosition, Vector3 endPosition, double time) {
        return new Velocity(endPosition.minus(initialPosition).div(time)); // TODO
    }

    /**
     *
     * @return The modulus of the velocity.
     */
    public double velocity() {
        return vector.magnitude();
    }

    /**
     *
     * @return A velocity with only the x component.
     */
    public Velocity x() {
        return this.y(0).z(0);
    }

    /**
     *
     * @return A velocity with only the y component.
     */
    public Velocity y() {
        return this.z(0).x(0);
    }

    /**
     *
     * @return A velocity with only the z component.
     */
    public Velocity z() {
        return this.x(0).y(0);
    }

    /**
     *
     * @param value The value target change the x component.
     * @return A velocity with the x component replaced by the value.
     */
    public Velocity x(double value) {
        return new Velocity(value, vector.e2().value(), vector.e3().value());
    }

    /**
     *
     * @param value The value target change the y component.
     * @return A velocity with the y component replaced by the value.
     */
    public Velocity y(double value) {
        return new Velocity(vector.e1().value(), value, vector.e3().value());
    }

    /**
     *
     * @param value The value target change the z component.
     * @return A velocity with the z component replaced by the value.
     */
    public Velocity z(double value) {
        return new Velocity(vector.e1().value(), vector.e2().value(), value);
    }

    /**
     * Sums the velocity with another velocity and returns the value of the operation without mutating the operands.
     *
     * @param other The velocity target sum with.
     * @return A velocity with value equals target the sum of the velocities.
     */
    public Velocity plus(Velocity other) {
        return new Velocity(this.vector.plus(other.vector));
    }

    public Velocity minus(Velocity other) {
        return new Velocity(this.vector.minus(other.vector));
    }

    public Velocity unaryMinus() {
        return new Velocity(this.vector.unaryMinus());
    }

    public Velocity times(double scalar) {
        return new Velocity(this.vector.times(scalar));
    }

    public Velocity div(double scalar) {
        return new Velocity(this.vector.div(scalar));
    }

    @Override
    public int compareTo(Velocity other) {
        return this.equals(other) ? 0 : (int) Math.ceil(this.velocity() - other.velocity());
    }
}
