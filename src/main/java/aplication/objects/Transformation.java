package aplication.objects;


import juanmanuel.gealma.vga.vga3.Rotor3;
import juanmanuel.gealma.vga.vga3.Vector3;

public class Transformation {
    private Vector3 position;
    private Rotor3 rotation;
    private Vector3 scale;

    public Transformation() {
        position = new Vector3();
        rotation = new Rotor3(1,0, 0, 0);
        scale = new Vector3(1, 1, 1);
    }

    public Transformation(Vector3 position, Rotor3 rotation, Vector3 scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Vector3 position() {
        return position;
    }

    public Rotor3 rotation() {
        return rotation;
    }

    public Vector3 scale() {
        return scale;
    }

    public void position(Vector3 position) {
        this.position = position;
    }

    public void rotation(Rotor3 rotation) {
        this.rotation = rotation;
    }

    public void scale(Vector3 scale) {
        this.scale = scale;
    }
}
