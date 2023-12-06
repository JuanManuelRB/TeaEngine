package graphic;

public record Color(byte r, byte g, byte b, byte a) {
    public static final Color BLACK = new Color((byte) 0, (byte) 0, (byte) 0, (byte) 255);
    public static final Color WHITE = new Color((byte) 255, (byte) 255, (byte) 255, (byte) 255);
    public static final Color RED = new Color((byte) 255, (byte) 0, (byte) 0, (byte) 255);
    public static final Color GREEN = new Color((byte) 0, (byte) 255, (byte) 0, (byte) 255);
    public static final Color BLUE = new Color((byte) 0, (byte) 0, (byte) 255, (byte) 255);
    public static final Color YELLOW = new Color((byte) 255, (byte) 255, (byte) 0, (byte) 255);
    public static final Color CYAN = new Color((byte) 0, (byte) 255, (byte) 255, (byte) 255);
    public static final Color MAGENTA = new Color((byte) 255, (byte) 0, (byte) 255, (byte) 255);
    public static final Color TRANSPARENT = new Color((byte) 0, (byte) 0, (byte) 0, (byte) 0);

    public Color(byte r, byte g, byte b) {
        this(r, g, b, (byte) 255);
    }

    public Color(int r, int g, int b) {
        this((byte) r, (byte) g, (byte) b, (byte) 255);
    }

    public Color(int r, int g, int b, int a) {
        this((byte) r, (byte) g, (byte) b, (byte) a);
    }

    public Color(float r, float g, float b) {
        this((byte) (r * 255), (byte) (g * 255), (byte) (b * 255), (byte) 255);
    }

    public Color(float r, float g, float b, float a) {
        this((byte) (r * 255), (byte) (g * 255), (byte) (b * 255), (byte) (a * 255));
    }

    public Color(int rgba) {
        this((byte) (rgba >> 24), (byte) (rgba >> 16), (byte) (rgba >> 8), (byte) rgba);
    }

    public float[] normalizedArray() {
        return new float[] {r / 255f, g / 255f, b / 255f, a / 255f};
    }
}
