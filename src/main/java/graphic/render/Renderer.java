package graphic.render;

import graphic.render.shader.VertexShader;
import graphic.scene.View;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL15.*;

/**
 * A {@link Renderer} is responsible for rendering {@link Renderable renderables} to a {@link Viewer viewer}.
 * <p>
 *     A {@link Renderer} is created by calling {@link Renderer#ofShader(ShaderProgram)}. The {@link ShaderProgram}
 *     is responsible for compiling and linking the shaders.
 *     <br>
 *     The {@link Renderer} is responsible for setting the uniforms and attributes of the {@link ShaderProgram}.
 *     <br>
 *     The {@link Renderer} is responsible for rendering the {@link Renderable renderables} to the {@link Viewer viewer}.
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
    Uniform uProjectionMatrix;
//    float aspectRatio = (float) window.getWidth() / window.getHeight(); //TODO: Se ejecuta antes de la inicializacion de GLFW a traves de GameLogic



    public Renderer() {}

    private Renderer(ShaderProgram shaderProgram) {
        this.program = shaderProgram;
    }


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

    /**
     * Initializes the Renderer.
     * @throws IOException if the shaders cannot be read.
     */
    public void init() throws IOException {
        if (program != null)
            return;

        // Create Shader Program
        program = new ShaderProgram();

        var vertexShader = new VertexShader(Path.of("src/main/resources/Shaders/vertex/vertex.vert"));
        var fragmentShader = new VertexShader(Path.of("src/main/resources/Shaders/fragments/fragment.frag"));
        program.attachShader(vertexShader);
        program.attachShader(fragmentShader);
        program.link();

        // Obtain uniform locations
        uProjectionMatrix = program.createUniform("proyectionMatrix");
        var worldMatrix = program.createUniform("worldMatrix");

    }

    /**
     * Creates a {@link Renderer} instance.
     * @param shaderProgram a {@link ShaderProgram} instance.
     * @return a {@link Renderer} instance.
     */
    public Renderer ofShader(ShaderProgram shaderProgram) {
        return new Renderer(shaderProgram);
    }

    /**
     * Accepts a {@link Renderable renderable} and a {@link Viewer viewer}
     * @param renderable a {@link Renderable Renderable} instance.
     * @param x an int, the X position to render.
     * @param y an int, the Y position to render.
     * @param viewer a {@link Viewer Viewer} instance where to render.
     */
    public void render(Renderable renderable, int x, int y, Viewer viewer) {
        glfwMakeContextCurrent(viewer.getContext());
        glViewport(0, 0, viewer.getWidth(), viewer.getHeight()); //TODO: this scales and deforms the rendering, maybe with matrix transformations?

        clear();

        program.use();

        // Update projection Matrix
        Matrix4f projectionMatrix = Transformation.getProjectionMatrix(FOV, viewer.getWidth(), viewer.getHeight(), Z_NEAR, Z_FAR);
        program.setUniform("projectionMatrix", projectionMatrix);

        // Draw Mesh
        renderable.render();
        program.unuse();
    }

    public void render(View view, Viewer viewer) {
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
