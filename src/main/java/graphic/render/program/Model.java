package graphic.render.program;

import aplication.objects.Transformation;
import aplication.util.MathUtils;
import graphic.Renderable;
import juanmanuel.gealma.vga.vga3.Rotor3;
import juanmanuel.gealma.vga.vga3.Vector3;
import org.joml.Matrix4f;


/**
 * Model is a renderable same as mesh is. All models have at least one mesh.
 */
public class Model implements Renderable {
    private Transformation transformation;

    private final Mesh mesh;

    public Model(Mesh mesh) {
        this.mesh = mesh;
    }

    public Vector3 position() {
        return transformation.position();
    }

    public void position(Vector3 position) {
        this.transformation.position(position);
    }

    public Rotor3 rotation() {
        return transformation.rotation().toRotor();
    }

    public void rotation(Rotor3 rotation) {
        this.transformation.rotation(rotation);
    }

    public Vector3 scale() {
        return transformation.scale();
    }

    public void scale(Vector3 scale) {
        this.transformation.scale(scale);
    }

    /**
     *
     * @return The mesh of this model.
     */
    @Override
    public Mesh mesh() {
        return mesh;
    }

    /**
     * Computes the model matrix of this model.
     *
     * @return The model matrix of this model.
     */
    public Matrix4f matrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.translate((float) position().x(), (float) position().y(), (float) position().z());
        matrix.mul(MathUtils.rotor3ToMatrix4f(
                rotation().e0().value(),
                rotation().e1e2().value(),
                rotation().e2e3().value(),
                rotation().e3e1().value()
        ));
        matrix.scale((float) scale().x(), (float) scale().y(), (float) scale().z());
        return matrix;
    }
}
