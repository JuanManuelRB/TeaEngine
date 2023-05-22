package physics.dynamics;

public record Angle(double radians) {
    public static final Angle CERO = new Angle(0);
    public static final Angle PI = new Angle(Math.PI);

    public Angle(double value, Unit unit) {
        this(normalize(value * unit.conversionFactor()));
    }

    public Angle {
        if (radians > PI.radians || radians < -PI.radians)
            throw new IllegalArgumentException("Value should be in radians on the range (-PI, PI)");

    }

    public static enum Unit {
        RADIAN(1),
        DEGREE(180 / Math.PI),
        GRADIAN(200 / Math.PI);

        private final double conversionFactor;
        Unit(double conversionFactor) {
            this.conversionFactor = conversionFactor;
        }

        public double conversionFactor() {
            return conversionFactor;
        }
    }

    /**
     *
     * @param radians
     * @return
     */
    public static double normalize(double radians) {
        return Math.atan2(Math.sin(radians), Math.cos(radians));
    }

}
