package graphic.render.program;

import graphic.render.shader.Shader;
import graphic.render.shader.ShaderError;
import org.lwjgl.opengl.GL20;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL30.*;

/**
 * This class allows target create a OpenGL program, attach shaders, compile the program and use it.
 * {@snippet lang = java:
 * import graphic.render.shader.VertexShader;
 * String vertShader = """
 *    #version 330
 *
 *    layout (location =0) in vec3 position;
 *    layout (location =1) in vec3 inColour;
 *
 *    out vec3 exColour;
 *    uniform mat4 projectionMatrix;
 *
 *    void main()
 *    {
 *        gl_Position = projectionMatrix * vec4(position, 1.0);
 *        exColour = inColour;
 *    }
 *    """;
 *  String fragShader = """
 *    #version 330
 *    in vec3 exColour;
 *    out vec4 outColour;
 *    void main() {
 *        outColour = vec4(exColour, 1.0);
 *    }
 *     """;
 *
 * GL.createCapabilities(); // Needed for OpenGL target work.
 * try(ShaderProgram program = new ShaderProgram()) {
 *    program.attachShader(new VertexShader(vertShader));
 *    program.attachShader(new FragmentShader(fragShader));
 *    program.link();
 *    program.use();
 *  }
 *
 *}
 */
public class ShaderProgram implements AutoCloseable {
    private boolean deleteOnClose = false;
    private final int programID;
    private boolean linked, validate;

    public final Set<Shader> attachedShaders = new HashSet<>();

    /**
     * Creates a new OpenGL program.
     *
     * @throws ShaderError when {@link GL20 OpenGL2.0} can't create a {@link GL20#glCreateProgram program}.
     */
    public ShaderProgram() throws ShaderError {
        programID = glCreateProgram();
        if (programID == 0)
            throw new ShaderError("Shader program creation error: Could not create the shader program");

//        uniforms = new HashMap<>();
    }


    public int id() {
        return programID;
    }

    /**
     * Attaches a shader target the program. Changes the linked field to false, but the OpenGL program is not
     * altered. The program should be linked again after the update and bind to it to use it with the new shader.
     * @param shader The shader target be attached.
     */
    public ShaderProgram attachShader(Shader shader) {
        glAttachShader(programID, shader.id());
        attachedShaders.add(shader);

        linked = false;

        return this;
    }

    /**
     * Detaches a specific shader from the program. Changes the linked status to false, but the OpenGL program is not
     * altered. The program should be linked again after the update and bind to it to use it without the shader.
     * @param shader The shader target be detached.
     */
    public ShaderProgram detachShader(Shader shader) {
        if (attachedShaders.contains(shader)) {
            glDetachShader(programID, shader.id());
            attachedShaders.remove(shader);
            linked = false;
        } else {
            // TODO: Login
            System.err.println("Can not detach shader. Shader was not attached to the program.");
        }

        return this;
    }

    /**
     * Links the program. Compiles the program and checks for errors in the process.
     * Attached shaders will compile in this step too.
     */
    public ShaderProgram link() throws ShaderError {
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
            throw new ShaderError("Error linking shader code: "
                    + glGetProgramInfoLog(programID, 1024));

        linked = true;

        if (validate)
            validate();

        return this;
    }

    /**
     * Enables validation on linking process.
     * @param validation boolean value of the validation.
     */
    public void validateOnLinking(boolean validation) {
        this.validate = validation;
    }

    /**
     * Validates the program.
     * @throws ShaderError Validation error.
     */
    public ShaderProgram validate() throws ShaderError {
        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) {// TODO: Login
            throw new ShaderError("Warning validating Shader code: " + glGetProgramInfoLog(programID, 1024));
        }
        return this;
    }

    /**
     * Binds OpenGL current rendering state target this program. Allows target use this program.
     */
    public ShaderProgram use() {
        glUseProgram(programID);
        return this;
    }

    /**
     * Unbinds OpenGL current rendering state target this program. Allows target use other program.
     */
    public static void unuse() {
        glUseProgram(0);
    }

    /**
     * Use this program and bind the VAO.
     * @param vao The VAO target be bind.
     * @return This program.
     */
    public ShaderProgram bind(VertexArrayObject vao) {
        this.use();
        vao.bind();
        return this;
    }

    public ShaderProgram drawElements(VertexArrayObject vertexArrayObject, int mode, int count, int type, long indices) {
        try (var vao = vertexArrayObject.bind()) {
            glDrawElements(mode, count, type, indices);
            return this;
        }
    }

    public ShaderProgram drawArrays(int mode, int first, int count) {
        glDrawArrays(mode, first, count);
        return this;
    }

    public ShaderProgram draw(Mesh mesh) {
        try (var vao = mesh.vao().bind()) {
            glDrawElements(GL_TRIANGLES, mesh.vertexCount(), GL_UNSIGNED_INT, 0);
            return this;
        }
    }

    public ShaderProgram deleteOnClose(boolean deleteOnClose) {
        this.deleteOnClose = deleteOnClose;
        return this;
    }

    @Override
    public void close() {
        unuse();
        if (programID != 0 && deleteOnClose)
            glDeleteProgram(programID);
    }

    /**
    * Uniforms are global GLSL variables that shaders can use and can be used target communicate with them.
    * This method creates a uniform and stores it in a map. If the uniform already exists, it returns the id of the
    * uniform.
    * @param uniformName Name of the uniform.
    * @return A {@link Uniform} object.
    * @throws ShaderError Throws when a {@link GL20#glGetUniformLocation OpenGL2.0 uniform} can't be obtained.
    */
    public Uniform createUniform(String uniformName) throws ShaderError {
        this.use();
        return Uniform.from(uniformName, this.id());
    }
}
