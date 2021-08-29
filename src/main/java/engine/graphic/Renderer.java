package engine.graphic;

import java.io.Closeable;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;


public final class Renderer implements Runnable, AutoCloseable {
    private ShaderProgram shaderProgram;
    int vaoId, vboId;

    public Renderer() {}

//    /**
//     * Field of View in Radians
//     */
//    private static final float FOV = (float) Math.toRadians(60.0f);
//    private static final float Z_NEAR = 0.01f;
//    private static final float Z_FAR = 1000.f;
//    Matrix4f projectionMatrix;
//    float aspectRatio = (float) Window.getWidth() / Window.getHeight();
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

    public void init() throws Throwable {
        // Create Shader Program
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Path.of("src/main/resources/Shaders/vertex/vertex.vs"));
        shaderProgram.createFragmentShader(Path.of("src/main/resources/Shaders/fragments/fragment.fs"));
        shaderProgram.link();

    }

    public void render(Window window, Mesh mesh) {
        clear();

        shaderProgram.bind();

        // Bind to the VAO
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);

        // Draw Mesh
        glBindVertexArray(mesh.getVaoId());
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        // Restore state
        glBindVertexArray(0);

        shaderProgram.unbind();

        // Update the window
        Window.update();
    }



//    public void render(Mesh mesh) {
////        shaderProgram.getEBO(elementArray);TODO
////        shaderProgram.createAttribPointer(positionSize, colorSize, floatSizeBytes);
//
//        clear();
//
//        if ( Ventana.isResized() ) {
//            glViewport(0, 0, Ventana.getAncho(), Ventana.getAlto());
//            Ventana.setResized(false);
//        }
//
//        shaderProgram.bind();
//
//        // Draw the mesh
//        glBindVertexArray(mesh.getVaoID());
//        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
//
//        // Restore state
//        glBindVertexArray(0);
//
//        shaderProgram.unbind();
//    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {
        if (shaderProgram != null)
            shaderProgram.cleanup();
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     *
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {

    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
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
