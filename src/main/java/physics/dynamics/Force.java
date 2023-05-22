package physics.dynamics;

import juanmanuel.gealma.threedimensional.Vector3;

public record Force(Vector3 vector) {
    public Force(double x, double y, double z) {
        this(new Vector3(x, y, z));
    }

    /**
     *
     * @return A force with only the x component.
     */
    public Force x() {
        return this.y(0).z(0);
    }

    /**
     *
     * @return A force with only the y component.
     */
    public Force y() {
        return this.z(0).x(0);
    }

    /**
     *
     * @return A force with only the z component.
     */
    public Force z() {
        return this.x(0).y(0);
    }

    /**
     *
     * @param value The value to change the x component.
     * @return A force with the x component replaced by the value.
     */
    public Force x(double value) {
        return new Force(value, vector.e2().value(), vector.e3().value());
    }

    /**
     *
     * @param value The value to change the y component.
     * @return A force with the y component replaced by the value.
     */
    public Force y(double value) {
        return new Force(vector.e1().value(), value, vector.e3().value());
    }

    /**
     *
     * @param value The value to change the z component.
     * @return A force with the z component replaced by the value.
     */
    public Force z(double value) {
        return new Force(vector.e1().value(), vector.e2().value(), value);
    }

    public Acceleration div(Mass mass) {
        return new Acceleration(vector.div(mass.mass()));
    }
}
