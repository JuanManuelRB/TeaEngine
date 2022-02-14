package old.engine.graphic.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

//TODO: Value based?
public record Transformation(Matrix4f projectionMatrix, Matrix4f worldMatrix) {

    public static Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        return new Matrix4f().setPerspective(fov, width / height, zNear, zFar);
    }

    public static Matrix4f getWorldMatrix(Vector3f offset, Vector3f rotation, float scale) {
        return new Matrix4f().translation(offset).
                rotateX((float)Math.toRadians(rotation.x)).
                rotateY((float)Math.toRadians(rotation.y)).
                rotateZ((float)Math.toRadians(rotation.z)).
                scale(scale);
    }
}
