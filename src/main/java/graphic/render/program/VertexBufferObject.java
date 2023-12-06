package graphic.render.program;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;

/**
 * Element Buffer Object. It's a wrapper for OpenGL VBO. It's used to store vertex data.
 */
public final class VertexBufferObject extends BufferObject<VertexBufferObject> {
    public VertexBufferObject() {
        super();
        bufferType = GL_ARRAY_BUFFER;
    }

    @Override
    public VertexBufferObject bind() {
        return bind(bufferType);
    }
}
