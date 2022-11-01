package aplication;

public record Vector (double i, double j, double k) {
    public Vector() {
        this(0, 0, 0);
    }

    public Vector(Position first, Position second) {
        this(second.x() - first.x(), second.y() - first.y(), second.z() - first.z());
    }

    public double magnitude() {
        return Math.sqrt(i*i + j*j + k*k);
    }

    public double length() {
        return magnitude();
    }

    public double inner(Vector other) {
        return this.i * other.i + this.j * other.j + this.k * other.k;
    }

    public Angle angle(Vector other) {
        return new Angle(Math.acos( inner(other) / (this.magnitude() * other.magnitude())));
    }
}
