package graphic.render;

import graphic.render.shader.Shader;
import graphic.scene.View;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL15.*;

/**
 * This class
 */
public final class Renderer implements Runnable, AutoCloseable {
    private ShaderProgram program;

    /**
     * Field of View in Radians
     */
    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    Matrix4f projectionMatrix;
//    float aspectRatio = (float) window.getWidth() / window.getHeight(); //TODO: Se ejecuta antes de la inicializacion de GLFW a traves de GameLogic



    public Renderer() {}


//
//    private float[] vertexArray = new float[]{
//            // posición            // color
//             0.5f, -0.5f, 0.0f,    1.0f, 0.0f, 0.0f, 1.0f, // derecho abajo
//            -0.5f,  0.5f, 0.0f,    0.0f, 1.0f, 0.0f, 1.0f, // izquierdo arriba
//             0.5f,  0.5f, 0.0f,    0.0f, 0.0f, 1.0f, 1.0f, // derecho arriba
//            -0.5f, -0.5f, 0.0f,    1.0f, 1.0f, 0.0f, 1.0f  // izquierdo abajo
//    };
//
//    int positionSize = 3;
//    int colorSize = 4;
//    int floatSizeBytes = 4;
//    int vertexSizeBytes = (positionSize + colorSize) * floatSizeBytes;

    public void init() throws IOException {
        // Create Shader Program
        program = new ShaderProgram();
        program.createVertexShader(Path.of("src/main/resources/Shaders/vertex/vertex.vs"));
        program.createFragmentShader(Path.of("src/main/resources/Shaders/fragments/fragment.fs"));
        program.link();

        // Create the uniforms
        program.createUniform("proyectionMatrix");
        program.createUniform("worldMatrix");

    }

    /**
     *
     * @param shaderProgram The program the renderer attaches to.
     * @return A {@link Renderer Renderer} instance with the Shader attached.
     */
    public Renderer ofShader(Shader shaderProgram) {
        return new Renderer();
    }

    /**
     * Accepts a {@link Renderable renderable} and a {@link Viewer viewer}
     * @param renderable a {@link Renderable Renderable} instance.
     * @param x an int, the X position to render.
     * @param y an int, the Y position to render.
     * @param viewer a {@link Viewer Viewer} instance where to render.
     */
    public void render(@NotNull Renderable renderable, int x, int y, @NotNull Viewer viewer) {
        glViewport(0, 0, viewer.getWidth(), viewer.getHeight()); //TODO: this scales and deforms the rendering, maybe with matrix transformations?

        clear();

        program.use();

        // Update projection Matrix
        Matrix4f projectionMatrix = Transformation.getProjectionMatrix(FOV, viewer.getWidth(), viewer.getHeight(), Z_NEAR, Z_FAR);
        program.setUniform("projectionMatrix", projectionMatrix);

        // Bind to the VAO TODO: ESTO NO HACE NADA! -> esta en mesh.render()
//        glBindVertexArray(vaoId);
//        glEnableVertexAttribArray(0);

        // Draw Mesh
        renderable.render();
        program.unuse();

        // shaderProgram.getEBO(elementArray);TODO
        // shaderProgram.createAttribPointer(positionSize, colorSize, floatSizeBytes);

    }

    public void render(@NotNull View view, @NotNull Viewer viewer) {
        glfwMakeContextCurrent(viewer.getContext());
        glViewport(0, 0, viewer.getWidth(), viewer.getHeight()); //TODO: this scales and deforms the rendering, maybe with matrix transformations?

    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


    @Override
    public void close() throws Exception {
        if (program != null)
            program.close();
    }


    @Override
    public void run() {
        //TODO
    }

//	public void renderMesh(Mesh mesh) {
//		GL30.glBindVertexArray(mesh.getVAO());
//		GL30.glEnableVertexAttribArray(0);
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());
//		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndices().length, GL11.GL_FLOAT, 0);
//
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//		GL30.glDisableVertexAttribArray(0);
//		GL30.glBindVertexArray(0);
//
//	}

}
