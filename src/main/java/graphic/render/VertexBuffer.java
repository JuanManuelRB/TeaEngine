package graphic.render;

import static org.lwjgl.opengl.GL30.*;


public class VertexBuffer {
    private final int vboID;

    public VertexBuffer(VertexArray vao) {
        vao.bind();
        vboID = glGenBuffers();
    }

    public void bind(VertexArray vao) {
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
    }

    public enum Type {
        ARRAY_BUFFER,
        ELEMENT_ARRAY_BUFFER;
    }
}
