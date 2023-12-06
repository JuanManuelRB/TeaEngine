package aplication.util;

import juanmanuel.gealma.vga.vga3.Rotor3;
import org.joml.Matrix4f;

public class MathUtils {
    public static Matrix4f rotor3ToMatrix4f(double s, double a, double b, double c) {
        Matrix4f matrix = new Matrix4f();
        // Fill the matrix according to the conversion formula
        matrix.set(new float[] {
                (float) (s*s + a*a - b*b - c*c), (float) (2*a*b - 2*s*c), (float) (2*a*c + 2*s*b), 0.0f,
                (float) (2*a*b + 2*s*c), (float) (s*s - a*a + b*b - c*c), (float) (2*b*c - 2*s*a), 0.0f,
                (float) (2*a*c - 2*s*b), (float) (2*b*c + 2*s*a), (float) (s*s - a*a - b*b + c*c), 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        });
        return matrix;
    }

    public static Matrix4f rotor3ToMatrix4f(Rotor3 rotor) {
        return rotor3ToMatrix4f(rotor.e0().value(), rotor.e1().value(), rotor.e2().value(), rotor.e3().value());
    }
}
