package graphic.render;

import java.lang.foreign.MemorySegment;

import static org.lwjgl.opengl.GL15.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class Mesh implements Renderable { //Primitive object? record?
    private final ShaderProgram.VertexArrayObject vao;

    /**
     * Creates a {@link Mesh} from the given positions, colours and indices.
     * @param positions 3D positions
     * @param colours RGBA colours
     * @param indices indices of the positions
     */
    public Mesh(float[] positions, float[] colours, int[] indices) {
        try (var vertexArrayObject = new ShaderProgram.VertexArrayObject()) {
            vao = vertexArrayObject;
        }

        // VBO
        var posVbo = new ShaderProgram.VertexBufferObject(vao);
        var colourVbo = new ShaderProgram.VertexBufferObject(vao);

        // EBO
        var idxVbo = new ShaderProgram.ElementBufferObject(vao);


        MemorySegment positionsSegment = MemorySegment.ofArray(positions);
        MemorySegment coloursSegment = MemorySegment.ofArray(colours);
        MemorySegment indicesSegment = MemorySegment.ofArray(indices);

        posVbo.buffer(positionsSegment.asByteBuffer().asFloatBuffer().flip(), GL_STATIC_DRAW);
        posVbo.bind();
        posVbo.vertexAttribPointer(0, positions.length / 3, GL_FLOAT, false, 0, 0);

        colourVbo.buffer(coloursSegment.asByteBuffer().asFloatBuffer().flip(), GL_STATIC_DRAW);
        colourVbo.bind();
        colourVbo.vertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        idxVbo.buffer(indicesSegment.asByteBuffer().asIntBuffer().flip(), GL_STATIC_DRAW);


    }
}
