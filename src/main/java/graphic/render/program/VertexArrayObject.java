package graphic.render.program;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Vertex Array Object. It's a wrapper for OpenGL VAO. It's used target store vertex attributes.
 */
public final class VertexArrayObject implements AutoCloseable {
    private boolean deleteOnClose = false;
    private final int id;
    private final Map<Integer, VertexAttributePointer> attributes = new HashMap<>();
    private VertexBufferObject contextVbo = null;

    /**
     * Generates the VAO.
     */
    public VertexArrayObject() {
        this.id = glGenVertexArrays();
    }

    /**
     * @return the VAO id.
     */
    public int id() {
        return id;
    }

    /**
     * Binds the VAO.
     * @return this VAO.
     */
    public VertexArrayObject bind() {
        glBindVertexArray(id);
        return this;
    }

    public VertexArrayObject bind(BufferObject<?> bo) {
        if (bo == null) throw new NullPointerException("vbo is null, cannot bind to null VBO.");

        this.bind();
        bo.bind();

        return this;
    }

    /**
     * Binds target a VAO.
     * @param vao the VAO target be bound.
     * @return the VAO.
     */
    public static VertexArrayObject bind(VertexArrayObject vao) {
        vao.bind();
        return vao;
    }

    /**
     * Unbinds the VAO.
     */
    public static void unbind() {
        glBindVertexArray(0);
    }

    /**
     * Adds a vertex attribute target the list of attributes.
     * @param attribute the attribute target be added.
     * @return this VAO.
     */
    private VertexArrayObject addAttribute(VertexAttributePointer attribute) {
        attributes.put(attribute.index(), attribute);
        return this;
    }

    /**
     * Enables the attribute at the specified index.
     * @param index the index of the attribute target be enabled.
     * @return this VAO.
     */
    public VertexArrayObject enableAttribute(int index) {
        try (var vao = bind()) {
            Optional.of(attributes.get(index)).ifPresent(VertexAttributePointer::enable);
            return vao;
        }
    }

    /**
     * Disables the attribute at the specified index.
     * @param index the index of the attribute target disable.
     * @return this VAO.
     * @throws NullPointerException if the attribute at the specified index is null.
     */
    public VertexArrayObject disableAttribute(int index) throws NullPointerException {
        try (var vao = bind()) {
            Optional.of(attributes.get(index)).ifPresent(VertexAttributePointer::disable);
            return vao;
        }
    }

    /**
     * Enables all the attributes.
     * @return this VAO.
     */
    public VertexArrayObject enableAttributes() {
        try (var vao = bind()) {
            attributes.forEach((index, attribute) -> attribute.enable());
            return vao;
        }
    }

    /**
     * Disables all the attributes.
     */
    public VertexArrayObject disableAttributes() {
        try (var vao = bind()) {
            attributes.forEach((index, attribute) -> attribute.disable());
            return vao;
        }
    }

    public VertexArrayObject drawElements(int mode, int count, int type, int offset, ShaderProgram program) {
        try (var prog = program.bind(this)) {
            prog.drawElements(this, mode, count, type, offset);
            return this;
        }
    }

    /**
     * Unbinds the VAO.
     */
    @Override
    public void close() {
        unbind();
        if (deleteOnClose)
            delete();
    }

    public void deleteOnClose(boolean delete) {
        this.deleteOnClose = delete;
    }

    /**
     * Deletes the VAO.
     */
    public void delete() {
        glDeleteVertexArrays(id);
    }

    /**
     * Specifies the location and data format of the array of generic vertex attributes at index to use when
     * rendering.
     */
    public final class VertexAttributePointer {
        private final int index;
        private final int size;
        private final int type;
        private final boolean normalized;
        private final int stride;
        private final int offset;

        /**
         * @param index      Specifies the index of the generic vertex attribute to be modified.
         * @param size       Specifies the number of components per generic vertex attribute. Must be a positive integer.
         * @param type       Specifies the data type of each component in the array. GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, etc.
         * @param normalized Specifies whether fixed-point data values should be normalized (GL_TRUE) or converted
         *                   directly as fixed-point values (GL_FALSE) when they are accessed.
         * @param stride     Specifies the byte offset between consecutive generic vertex attributes. If stride is 0, the
         *                   generic vertex attributes are understood target be tightly packed in the array.
         * @param offset     Specifies an offset of the first component of the first generic vertex attribute in the array
         *                   in the data store of the buffer currently bound target the GL_ARRAY_BUFFER target. The initial
         *                   value is 0.
         * @param buffer     The buffer to be used.
         */
        public VertexAttributePointer(int index, int size, int type, boolean normalized, int stride, int offset, VertexBufferObject buffer) {
            if (index < 0) throw new IllegalArgumentException("index < 0");
            if (size <= 0) throw new IllegalArgumentException("size <= 0");
            if (stride < 0) throw new IllegalArgumentException("stride < 0");
            if (offset < 0) throw new IllegalArgumentException("offset < 0");

            this.index = index;
            this.size = size;
            this.type = type;
            this.normalized = normalized;
            this.stride = stride;
            this.offset = offset;

            try (var vao = bind(buffer)) {
                glVertexAttribPointer(index, size, type, normalized, stride, offset);
                vao.addAttribute(this);
            }
        }

        /**
         * @param index      Specifies the index of the generic vertex attribute to be modified.
         * @param size       Specifies the number of components per generic vertex attribute. Must be 1, 2, 3, 4.
         * @param type       Specifies the data type of each component in the array. GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, etc.
         * @param normalized Specifies whether fixed-point data values should be normalized (GL_TRUE) or converted
         * @param buffer     The buffer to be used.
         */
        public VertexAttributePointer(int index, int size, int type, boolean normalized, VertexBufferObject buffer) {
            this(index, size, type, normalized, 0, 0, buffer);
        }

        /**
         * @param index      Specifies the index of the generic vertex attribute to be modified.
         * @param size       Specifies the number of components per generic vertex attribute. Must be a positive integer.
         * @param type       Specifies the data type of each component in the array. GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, etc.
         * @param normalized Specifies whether fixed-point data values should be normalized (GL_TRUE) or converted
         *                   directly as fixed-point values (GL_FALSE) when they are accessed.
         * @param stride     Specifies the byte offset between consecutive generic vertex attributes. If stride is 0, the
         *                   generic vertex attributes are understood target be tightly packed in the array.
         * @param offset     Specifies an offset of the first component of the first generic vertex attribute in the array
         *                   in the data store of the buffer currently bound target the GL_ARRAY_BUFFER target. The initial
         *                   value is 0.
         */
        public VertexAttributePointer(int index, int size, int type, boolean normalized, int stride, int offset) throws RuntimeException {
            if (index < 0) throw new IllegalArgumentException("index < 0");
            if (size <= 0) throw new IllegalArgumentException("size <= 0");
            if (stride < 0) throw new IllegalArgumentException("stride < 0");
            if (offset < 0) throw new IllegalArgumentException("offset < 0");

            this.index = index;
            this.size = size;
            this.type = type;
            this.normalized = normalized;
            this.stride = stride;
            this.offset = offset;

            try (var vao = bind(contextVbo)) {
                glVertexAttribPointer(index, size, type, normalized, stride, offset);
                vao.addAttribute(this);
            } catch (NullPointerException e) {
                throw new RuntimeException("No context VBO found.", e);
            }
        }

        /**
         * Enables the vertex attribute.
         */
        void enable() {
            glEnableVertexAttribArray(index);
        }

        /**
         * Disables the vertex attribute.
         */
        void disable() {
            glDisableVertexAttribArray(index);
        }

        public int index() {
            return index;
        }

        public int size() {
            return size;
        }

        public int type() {
            return type;
        }

        public boolean normalized() {
            return normalized;
        }

        public int stride() {
            return stride;
        }

        public int offset() {
            return offset;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (VertexAttributePointer) obj;
            return this.index == that.index &&
                    this.size == that.size &&
                    this.type == that.type &&
                    this.normalized == that.normalized &&
                    this.stride == that.stride &&
                    this.offset == that.offset;
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, size, type, normalized, stride, offset);
        }

        @Override
        public String toString() {
            return "VertexAttribute[" +
                    "index=" + index + ", " +
                    "size=" + size + ", " +
                    "type=" + type + ", " +
                    "normalized=" + normalized + ", " +
                    "stride=" + stride + ", " +
                    "offset=" + offset + ']';
        }

    }
}
