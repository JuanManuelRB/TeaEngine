package aplication.geometricalgebra;

public record GeometricObject(double scalar, Vector vector, Bivector bivector, Trivector trivector) implements Geometric {
    public GeometricObject plus(GeometricObject other) {
        return new GeometricObject(this.scalar + other.scalar,
                                        this.vector.plus(other.vector),
                                        this.bivector.plus(other.bivector),
                                        this.trivector.plus(other.bivector));
    }

    public record Vector(double x, double y, double z) implements Geometric {

        public static Vector plus(Vector first, Vector second) {
            return new Vector(first.x + second.x, first.y + second.y, first.z + second.z);
        }

        public static Vector plus(Vector ... vectors) {
            for (int i = 0; i < vectors.length; i++) {

            }
        }

        public Vector plus(Vector other) {
            return Vector.plus(this, other);
        }

        @Override
        public double scalar() {
            return 0;
        }

        @Override
        public Vector vector() {
            return this;
        }

        @Override
        public Bivector bivector() {
            return null;
        }

        @Override
        public Trivector trivector() {
            return null;
        }
    }
    public record Bivector(double xy, double yz, double zx) implements Geometric {

        @Override
        public double scalar() {
            return 0;
        }

        @Override
        public Vector vector() {
            return null;
        }

        @Override
        public Bivector bivector() {
            return this;
        }

        @Override
        public GeometricObject.Trivector trivector() {
            return null;
        }
    }

    public record Trivector(double xyz) implements Geometric {

        @Override
        public double scalar() {
            return 0;
        }

        @Override
        public Vector vector() {
            return null;
        }

        @Override
        public Bivector bivector() {
            return null;
        }

        @Override
        public Trivector trivector() {
            return this;
        }
    }
}
