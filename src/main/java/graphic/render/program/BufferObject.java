package graphic.render.program;

import graphic.render.ElementBufferObject;
import org.lwjgl.opengl.GL15;

import java.nio.*;

import static org.lwjgl.opengl.GL15.*;

/**
 * Element Buffer Object. It's a wrapper for OpenGL VBO. TODO: Fix documentation.
 */
public abstract sealed class BufferObject<Self extends BufferObject<?>> implements AutoCloseable permits VertexBufferObject, ElementBufferObject {
    private boolean deleteOnClose = false;
    protected final int id;
    protected int bufferType;

    private boolean deleted = false;
    protected DataType dataType;

    /**
     * Creates a buffer object.
     */
    public BufferObject() {
        this.id = glGenBuffers();
    }

    /**
     * @return The id of the buffer.
     */
    public final int id() {
        return id;
    }

    /**
     * Binds the buffer and sets the buffer type to the given type. If the buffer is already bound, it will only set the buffer type.
     *
     * @param bufferType The type of the buffer.
     */
    public final Self bind(int bufferType) {
        glBindBuffer(bufferType, id);
        this.bufferType = bufferType;
        return (Self) this;
    }

    public final Self bind(VertexArrayObject vao) {
        vao.bind();
        return bind();
    }

    /**
     * Binds the buffer with the current buffer type.
     */
    public abstract Self bind();

    /**
     * Unbinds the buffer.
     */
    public static void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Buffer data target the buffer object.
     * @param data The data target buffer.
     * @param mode The usage mode of the buffer. See {@link GL15#glBufferData(int, ByteBuffer, int)}.
     * @return this buffer object.
     */
    public Self buffer(Buffer data, Usage mode, boolean unsigned) {
        try(var bufferObject = bind()) {
            dataType = switch (data) {
                case ByteBuffer byteBuffer -> {
                    glBufferData(bufferType, byteBuffer, mode.glUsage());
                    yield unsigned ? DataType.UBYTE : DataType.BYTE;
                }

                case ShortBuffer shortBuffer -> {
                    glBufferData(bufferType, shortBuffer, mode.glUsage());
                    yield unsigned ? DataType.USHORT : DataType.SHORT;
                }

                case IntBuffer intBuffer -> {
                    glBufferData(bufferType, intBuffer, mode.glUsage());
                    yield unsigned ? DataType.UINT : DataType.INT;
                }

                case LongBuffer longBuffer -> {
                    glBufferData(bufferType, longBuffer, mode.glUsage());
                    yield DataType.INT;
                }

                case FloatBuffer floatBuffer -> {
                    glBufferData(bufferType, floatBuffer, mode.glUsage());
                    yield DataType.FLOAT;
                }

                case DoubleBuffer doubleBuffer -> {
                    glBufferData(bufferType, doubleBuffer, mode.glUsage());
                    yield DataType.FLOAT;
                }

                default -> throw new IllegalStateException("Unexpected value: " + data);
            };
            return bufferObject;
        }
    }

    public DataType dataType() {
        return dataType;
    }


    /**
     * Deletes the buffer object.
     */
    public final void delete() {
        if (!deleted)
            glDeleteBuffers(id);
        deleted = true;
    }

    public final boolean isDeleted() {
        return deleted;
    }

    public final void deleteOnClose(boolean delete) {
        this.deleteOnClose = delete;
    }

    /**
     * Deletes the buffer object.
     */
    @Override
    public void close() {
        unbind();
        if (deleteOnClose)
            delete();
    }

    public enum Usage {
        STATIC_DRAW(GL_STATIC_DRAW),
        DYNAMIC_DRAW(GL_DYNAMIC_DRAW),
        STREAM_DRAW(GL_STREAM_DRAW),
        STATIC_READ(GL_STATIC_READ),
        DYNAMIC_READ(GL_DYNAMIC_READ),
        STREAM_READ(GL_STREAM_READ),
        STATIC_COPY(GL_STATIC_COPY),
        DYNAMIC_COPY(GL_DYNAMIC_COPY),
        STREAM_COPY(GL_STREAM_COPY);

        private final int glUsage;

        Usage(int glUsage) {
            this.glUsage = glUsage;
        }

        public int glUsage() {
            return glUsage;
        }
    }
}
