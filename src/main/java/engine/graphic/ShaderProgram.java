package engine.graphic;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import engine.io.ResourceLoader;


import static org.lwjgl.opengl.GL20.*;


/**
 *
 */
public class ShaderProgram implements AutoCloseable{
    private final int programID;//TODO: convert to record?
    private int vertexShaderID, fragmentShaderID, geometryShaderID; //TODO: geometry shader
    private final Map<String, Integer> uniforms;


    //TODO: this should be given in the constructor.
    int positionSize = 3;
    int colorSize = 4;
    int floatSizeBytes = 4;
    int vertexSizeBytes = (positionSize + colorSize) * floatSizeBytes;

    /**
     * Creates a new OpenGL program,
     *
     * @throws ShaderError Throws when {@link GL20 OpenGL2.0} can't create a {@link GL20#glCreateProgram program}.
     */
    public ShaderProgram() throws ShaderError {
        programID = glCreateProgram();
        if (programID == 0)
            throw new ShaderError("Shader program creation error: Could not create the shader program");
        uniforms = new HashMap<>();
    }


    /**
     * Uniforms are global GLSL variables that shaders can use and that can be employed to communicate with them.
     *
     * @param uniformName Name of the uniform
     * @throws ShaderError Throws when a {@link GL20#glGetUniformLocation OpenGL2.0 uniform} can't be obtained.
     */
    public void createUniform(String uniformName) throws ShaderError {
        int uniformLocation = glGetUniformLocation(programID, uniformName);
        if (uniformLocation == 0)
            throw new ShaderError("Uniform creation error: Could not find the uniform" + uniformName);
        uniforms.put(uniformName, uniformLocation);
    }

    /**
     *
     * @param uniformName The name of the uniform to set the value
     * @param value The value of the uniform, given by a {@link Matrix4f Matrix4f}
     */
    public void setUniform(String uniformName, Matrix4f value) {
        // Dump the matrix into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(uniforms.get(uniformName), false, value.get(stack.mallocFloat(16)));
        }
    }


    /**
     *
     * @param vertexShaderPath
     * @throws ShaderError
     */
    public void createVertexShader(Path vertexShaderPath) throws ShaderError, IOException {
        try {
            vertexShaderID = createShader(ResourceLoader.loadShader(vertexShaderPath), GL_VERTEX_SHADER);

        } catch (IOException ioEx) {
            throw ioEx;
        } catch (Exception e) {
            throw new ShaderError(
                    "Could not create vertex shader: " + vertexShaderPath.toString(), e);
        }
    }

    /**
     *
     * @param fragmentShaderPath A path to a fragment shader code.
     * @throws ShaderError
     */
    public void createFragmentShader(Path fragmentShaderPath) throws ShaderError, IOException {
        try {
            fragmentShaderID = createShader(ResourceLoader.loadShader(fragmentShaderPath), GL_FRAGMENT_SHADER);

        }catch (IOException ioEx) {
            throw ioEx;
        }catch (Exception e) {
            throw new ShaderError(
                    "Could not create fragment shader: " + fragmentShaderPath.toString(), e);
        }
    }

//    public int createGeometryShader(Path geometryShaderPath) throws GeometryCreationError, IOException {
//        try {
//            geometryShaderID = createShader(ResourceLoader.loadShader(geometryShaderPath), GL_SHADER);
//        }catch (IOException ioEx) {
//            //TODO
//        }catch (Exception e) {
//            throw new GeometryCreationError(
//                    "Couldn't create fragment shader: " + geometryShaderPath.toString(), e);
//        }
//    }TODO: create geometry shader

    /**
     *
     * @param shaderCode Código de shader de tipo String.
     * @param shaderType Tipo de Shader, GL_FRAGMENT_SHADER o GL_VERTEX_SHADER.
     * @return Devuelve el identificador del shader proporcionado por OpenGL. El shader ya esta asignado al
     *         identificador del programa
     * @throws Exception WIP
     */
    protected int createShader(String shaderCode, int shaderType) throws ShaderError {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0)
            throw new ShaderError("Error when creating shader of type: " + shaderType);

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE)
            throw new ShaderError("Error compiling the shader: "
                                    + glGetShaderInfoLog(shaderId, 1024));

        glAttachShader(programID, shaderId);
        return shaderId;
    }

    /**
     * Enlaza el programa a OpenGL
     *
     * @throws Exception No se pudo enlazar el ID de programa.
     */
    public void link() throws ShaderError {
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
            throw new ShaderError("Error linking shader code: "
                    + glGetProgramInfoLog(programID, 1024));

        //Detach vertex and fragment shaders from the program
        if (vertexShaderID != 0)
            glDetachShader(programID, vertexShaderID);

        if (fragmentShaderID != 0)
            glDetachShader(programID, fragmentShaderID);

        // TODO: Validación OpenGL, creo que debe eliminarse en fase de producción.
        glValidateProgram(programID);


        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0)
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programID, 1024));
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programID != 0) {
            glDeleteProgram(programID);
        }
    }

//    public int generarEBO() {
//        // Guardar array en memoria
//        IntBuffer elementBuffer = MemoryUtil.memAllocInt(elementArray.length);
//        elementBuffer.put(elementArray).flip();
//
//        // Generar el EBO
//        eboID = glGenBuffers();
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER , eboID);
//        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);
//
//        return elementBuffer;
//    }

    public void createAttribPointer(int positionSize, int colorSize, int floatSizeBytes) {
        glVertexAttribPointer(0, positionSize,GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }//TODO

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
        cleanup();
    }
}
