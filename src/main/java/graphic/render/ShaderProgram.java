package graphic.render;

import graphic.render.shader.Shader;
import graphic.render.shader.ShaderError;
import org.lwjgl.opengl.GL20;

import java.nio.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.lwjgl.opengl.GL30.*;

/**
 * This class allows to create a OpenGL program, attach shaders, compile the program and use it.
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
 *
 * GL.createCapabilities(); // Needed for OpenGL to work.
 * try(ShaderProgram program = new ShaderProgram()) {
 *    program.attachShader(new VertexShader(vertShader));
 *    program.link();
 *    program.use();
 *  }
 *
 *}
 */
public class ShaderProgram implements AutoCloseable {
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


    int id() {
        return programID;
    }

    /**
     * Attaches a shader to the program. Changes the linked field to false, but the OpenGL program is not
     * altered. The program should be linked again after the update and bind to it to use it with the new shader.
     * @param shader The shader to be attached.
     */
    public void attachShader(Shader shader) {
        glAttachShader(programID, shader.id());
        attachedShaders.add(shader);

        linked = false;
    }

    /**
     * Detaches a specific shader from the program. Changes the linked field to false, but the OpenGL program is not
     * altered. The program should be linked again after the update and bind to it to use it without the shader.
     * @param shader The shader to be detached.
     */
    public void detachShader(Shader shader) {
        if (attachedShaders.contains(shader)) {
            glDetachShader(programID, shader.id());
            attachedShaders.remove(shader);
            linked = false;
        } else {
            // TODO: Login
            System.err.println("Can not detach shader. Shader was not attached to the program.");
        }
    }

    /**
     * Links the program. Compiles the program and checks for errors in the process.
     * Attached shaders will compile in this step too.
     */
    public void link() throws ShaderError {
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
            throw new ShaderError("Error linking shader code: "
                    + glGetProgramInfoLog(programID, 1024));

        linked = true;

        if (validate)
            validate();
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
    public void validate() throws ShaderError {
        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) {// TODO: Login
            throw new ShaderError("Warning validating Shader code: " + glGetProgramInfoLog(programID, 1024));
        }
    }

    /**
     * Binds OpenGL current rendering state to this program. Allows to use this program.
     */
    public void use() {
        glUseProgram(programID);
    }

    /**
     * Unbinds OpenGL current rendering state to this program. Allows to use other program.
     */
    public static void unuse() {
        glUseProgram(0);
    }

    /**
     * Use this program and bind the VAO.
     * @param vao The VAO to be bind.
     * @return The VAO.
     */
    public VertexArrayObject bind(VertexArrayObject vao) {
        use();
        vao.bind();
        return vao;
    }

    @Override
    public void close() {
        unuse();
        attachedShaders.forEach(this::detachShader); // Todo: is this necessary?
        if (programID != 0) {
            glDeleteProgram(programID);
        }
    }

    /**
    * Uniforms are global GLSL variables that shaders can use and can be used to communicate with them.
    * This method creates a uniform and stores it in a map. If the uniform already exists, it returns the id of the
    * uniform.
    * @param uniformName Name of the uniform.
    * @return A {@link Uniform} object.
    * @throws ShaderError Throws when a {@link GL20#glGetUniformLocation OpenGL2.0 uniform} can't be obtained.
    */
    public Uniform createUniform(String uniformName) throws ShaderError {
        return Uniform.from(uniformName, id());
    }

    /**
     * Vertex Array Object. It's a wrapper for OpenGL VAO. It's used to store vertex attributes.
     */
    public static final class VertexArrayObject implements AutoCloseable {
        private final int id;
        public VertexArrayObject() {
            this.id = glGenVertexArrays();
        }

        public int id() {
            return id;
        }

        public void bind() {
            glBindVertexArray(id);
        }

        public static void unbind() {
            glBindVertexArray(0);
        }

        @Override
        public void close() {
            unbind();
        }
    }

    /**
     * Buffer Object. It's a wrapper for OpenGL VBO and EBO.
     */
    public static abstract sealed class BufferObject permits VertexBufferObject, ElementBufferObject {
        protected final VertexArrayObject vertexArrayObject;
        protected final int id;
        protected int bufferType;

        public BufferObject(VertexArrayObject vertexArrayObject) {
            Objects.requireNonNull(vertexArrayObject);
            this.vertexArrayObject = vertexArrayObject;
            vertexArrayObject.bind();
            this.id = glGenBuffers();
        }

        /**
         * @return The id of the buffer.
         */
        public int id() {
            return id;
        }

        /**
         * Binds the buffer. Changes the buffer type.
         * @param bufferType The type of the buffer.
         */
        public final void bind(int bufferType) {
            vertexArrayObject.bind();
            this.bufferType = bufferType;
            glBindBuffer(bufferType, id);
        }

        /**
         * Binds the buffer.
         */
        public abstract void bind();

        /**
         * Buffer data to the buffer.
         */
        public void buffer(Buffer data, int mode) {
            bind();
            switch (data) {
                case ByteBuffer byteBuffer     -> glBufferData(bufferType, byteBuffer, mode);
                case ShortBuffer shortBuffer   -> glBufferData(bufferType, shortBuffer, mode);
                case IntBuffer intBuffer       -> glBufferData(bufferType, intBuffer, mode);
                case LongBuffer longBuffer     -> glBufferData(bufferType, longBuffer, mode);
                case FloatBuffer floatBuffer   -> glBufferData(bufferType, floatBuffer, mode);
                case DoubleBuffer doubleBuffer -> glBufferData(bufferType, doubleBuffer, mode);

                default -> throw new IllegalStateException("Unexpected value: " + data);
            };
        }
    }

    /**
     * Element Buffer Object. It's a wrapper for OpenGL VBO. It's used to store vertex data.
     */
    public static final class VertexBufferObject extends BufferObject {
        public VertexBufferObject(VertexArrayObject vertexArrayObject) { // TODO: Compose instead of extend?
            super(vertexArrayObject);
            bufferType = GL_ARRAY_BUFFER;
        }

        @Override
        public void bind() {
            bind(bufferType);
        }

        /**
         * Specifies the location and data format of the array of generic vertex attributes at index to use when
         * rendering.
         * @param index Specifies the index of the generic vertex attribute to be modified.
         * @param size Specifies the number of components per generic vertex attribute. Must be 1, 2, 3, 4.
         * @param type Specifies the data type of each component in the array. GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, etc.
         * @param normalized Specifies whether fixed-point data values should be normalized (GL_TRUE) or converted
         *                   directly as fixed-point values (GL_FALSE) when they are accessed.
         * @param stride Specifies the byte offset between consecutive generic vertex attributes. If stride is 0, the
         *               generic vertex attributes are understood to be tightly packed in the array.
         * @param pointer Specifies an offset of the first component of the first generic vertex attribute in the array
         *                in the data store of the buffer currently bound to the GL_ARRAY_BUFFER target. The initial
         *                value is 0.
         */
        public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer) {
            bind();
            glVertexAttribPointer(index, size, type, normalized, stride, pointer);
            glEnableVertexAttribArray(index);
        }
    }

    /**
     * Element Buffer Object. It's a wrapper for OpenGL EBO. It's used to store vertex indices.
     */
    public static final class ElementBufferObject extends BufferObject {
        private final int bufferType = GL_ELEMENT_ARRAY_BUFFER;
        public ElementBufferObject(VertexArrayObject vertexArrayObject) {
            super(vertexArrayObject);
        }

        @Override
        public void bind() {
            bind(bufferType);
        }

        /**
         *
         * @param index Specifies the index of the generic vertex attribute to be modified.
         * @param size Specifies the number of components per generic vertex attribute. Must be 1, 2, 3, 4. The initial value is 4.
         * @param glDataType Specifies the data type of each component in the array. The different functions take different values.
         * glVertexAttribPointer can take:
         *                   GL_BYTE,
         *                   GL_UNSIGNED_BYTE,
         *                   GL_SHORT,
         *                   GL_UNSIGNED_SHORT,
         *                   GL_INT,
         *                   GL_UNSIGNED_INT,
         *                   GL_HALF_FLOAT,
         *                   GL_FLOAT,
         *                   GL_DOUBLE,
         *                   GL_FIXED,
         *                   GL_INT_2_10_10_10_REV,
         *                   GL_UNSIGNED_INT_2_10_10_10_REV,
         *                   and GL_UNSIGNED_INT_10F_11F_11F_REV.
         *
         * The initial value is GL_FLOAT.
         * @param normalized For glVertexAttribPointer, specifies whether fixed-point data values should be normalized (GL_TRUE) or converted directly as fixed-point values (GL_FALSE) when they are accessed.
         * @param stride Specifies the byte offset between consecutive generic vertex attributes. If stride is 0, the generic vertex attributes are understood to be tightly packed in the array. The initial value is 0.
         * @param offset Specifies an offset of the first component of the first generic vertex attribute in the array in the data store of the buffer currently bound to the GL_ARRAY_BUFFER target. The initial value is 0.
         */
        public void createAttributePointer(int index, int size, int glDataType, boolean normalized, int stride, long offset) {
            bind();
            glVertexAttribPointer(index, size, glDataType, normalized, stride, offset);
        }
    }
}
