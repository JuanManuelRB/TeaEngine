package graphic.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
        private final Matrix4f projectionMatrix;
        private final Matrix4f viewMatrix;

        public Camera() {
            this.projectionMatrix = new Matrix4f();
            this.viewMatrix = new Matrix4f();
        }

        public Matrix4f getProjectionMatrix() {
            return projectionMatrix;
        }

        public Matrix4f getViewMatrix() {
            return viewMatrix;
        }

        public void updateProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
            projectionMatrix.setPerspective(fov, width / height, zNear, zFar);
        }

        public void updateViewMatrix(float x, float y, float z, float pitch, float yaw, float roll) {
            viewMatrix.identity();
            // First do the rotation so camera rotates over its position
            viewMatrix.rotate((float) Math.toRadians(pitch), new Vector3f(1, 0, 0))
                    .rotate((float) Math.toRadians(yaw), new Vector3f(0, 1, 0));
            // Then do the translation
            viewMatrix.translate(-x, -y, -z);
        }

        public void updateViewMatrix(Vector3f position, Vector3f rotation) {
            updateViewMatrix(position.x, position.y, position.z, rotation.x, rotation.y, rotation.z);
        }
}
