package graphic.render;

import static org.lwjgl.opengl.GL30.*;

public class VertexArray {
    private final int vaoID;

    public VertexArray() {
        vaoID = glGenVertexArrays();

    }

    public void delete() {
        glDeleteVertexArrays(vaoID);
    }

    public void bind() {
        glBindVertexArray(vaoID);
    }

    public static void unbind() {
        glBindVertexArray(0);
    }

}
