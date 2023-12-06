package graphic.scene;

import graphic.Color;
import juanmanuel.gealma.vga.vga3.Vector3;
import org.joml.Matrix4f;

import java.util.Objects;

public class Camera extends SceneObject {
    private final Projection projection;
    private Vector3 position;

    private float fov = (float) Math.toRadians(65.0f);
    private float zFar = 1000.f;
    private float zNear= 0.01f;
    private int width = 1280;
    private int height = 720;
    private Color backgroundColor = Color.BLACK;

    public Camera(Vector3 position, float fov, int width, int height, float zNear, float zFar) {
        Objects.requireNonNull(position, "The position of a camera cannot be null.");
        this.position = position;

        this.fov = fov;
        this.width = width;
        this.height = height;
        this.zNear = zNear;
        this.zFar = zFar;
//        this(position);

        this.projection = new Projection(width, height, fov, zNear, zFar);
    }

    public Camera(Vector3 position) {
        Objects.requireNonNull(position, "The position of a camera cannot be null.");
        this.position = position;

        projection = new Projection(width, height, fov, zNear, zFar);
    }

    public void size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void position(Vector3 position) {
        Objects.requireNonNull(position, "The position of a camera cannot be null.");
        this.position = position;
    }

    public Vector3 position() {
        return position;
    }


    public static class Projection {
        private final Matrix4f projection = new Matrix4f();

        private final float fov, zNear, zFar;

        /**
         * Creates a projection matrix.
         * @param width the width of the window.
         * @param height the height of the window.
         * @param fov the field of view.
         * @param zNear the near plane.
         * @param zFar the far plane.
         */
        public Projection(int width, int height, float fov, float zNear, float zFar) {
            this.fov = fov;
            this.zNear = zNear;
            this.zFar = zFar;

            projection.identity();
            perspective(width, height);
        }

        public Matrix4f projection() {
            return projection;
        }

        /**
         * @return the field of view.
         */
        public float fov() {
            return fov;
        }

        public void fov(float fov) {
            fov = Math.max(1, Math.min(fov, 180));
        }

        /**
         *
         * @return the near plane.
         */
        public float zNear() {
            return zNear;
        }

        public void zNear(float zNear) {
            zNear = Math.max(0.1f, Math.min(zNear, zFar));
        }

        /**
         *
         * @return the far plane.
         */
        public float zFar() {
            return zFar;
        }

        public void zFar(float zFar) {
            zFar = Math.max(zNear, zFar);
        }

        /**
         * Updates the projection matrix.
         * @param width the width of the window.
         * @param height the height of the window.
         * @return the projection matrix.
         */
        public Matrix4f perspective(float width, float height) {
            projection.identity();
            float aspectRatio = width / height;
            return projection.setPerspective(fov, aspectRatio, zNear, zFar);
        }

        public Matrix4f orthographic(float left, float right, float bottom, float top) {
            projection.identity();
            return projection.setOrtho(left, right, bottom, top, zNear, zFar);
        }





    }
}
