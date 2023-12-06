package graphic.render.program;

import graphic.Renderable;
import graphic.render.ElementBufferObject;

import java.nio.ByteBuffer;

public class Mesh implements AutoCloseable, Renderable { //Primitive object? record?
    private final VertexArrayObject vao;
    private final VertexBufferObject vertexVBO;
    private final VertexBufferObject colorVBO;
    private final ElementBufferObject ebo;
    private final Texture texture;

    public Mesh(float[] vertices, float[] colors, int[] indices, Texture texture) {
        if (vertices.length % 3 != 0) throw new IllegalArgumentException("The vertices array must be a multiple of 3.");
        if (colors.length % 4 != 0) throw new IllegalArgumentException("The colors array must be a multiple of 4.");
        if (vertices.length / 3 != colors.length / 4) throw new IllegalArgumentException("The vertices and colors arrays must have the same length.");

        this.texture = texture;
        try (var vao = new VertexArrayObject()) {
            this.vao = vao;
            vertexVBO = new VertexBufferObject();
            try (var vertVBO = vertexVBO.bind(vao)) {
                var vertexBuffer = ByteBuffer
                        .allocateDirect(vertices.length * Float.BYTES)
                        .asFloatBuffer()
                        .put(vertices)
                        .flip();

                vertVBO.buffer(vertexBuffer, BufferObject.Usage.STATIC_DRAW, true);
                var vert = vao.new VertexAttributePointer(0, 3, vertexVBO.dataType().glType(), false, 0, 0, vertexVBO);
            }

            colorVBO = new VertexBufferObject();
            try (var colVBO = colorVBO.bind(vao)) {
                var colorBuffer = ByteBuffer
                        .allocateDirect(colors.length * Float.BYTES)
                        .asFloatBuffer()
                        .put(colors)
                        .flip();

                colVBO.buffer(colorBuffer, BufferObject.Usage.STATIC_DRAW, true);
                var color = vao.new VertexAttributePointer(1, 4, colVBO.dataType().glType(), false, 0, 0, colVBO);
            }

            ebo = new ElementBufferObject();
            try (var idxEBO = ebo.bind(vao)) {
                var elementBuffer = ByteBuffer
                        .allocateDirect(indices.length * Integer.BYTES)
                        .asIntBuffer()
                        .put(indices)
                        .flip();

                idxEBO.buffer(elementBuffer, BufferObject.Usage.STATIC_DRAW);
            }
        }
    }

    /**
     *
     * @return The type of the indices.
     */
    public DataType indexType() {
        return ebo.dataType();
    }

    /**
     *
     * @return The VAO of the mesh.
     */
    public VertexArrayObject vao() {
        return vao;
    }


    public int vertexCount() {
        return 0;
    }

    @Override
    public void close() throws Exception {
        vao.close();
        vertexVBO.close();
        colorVBO.close();
        ebo.close();
    }

    @Override
    public Mesh mesh() {
        return this;
    }
}
