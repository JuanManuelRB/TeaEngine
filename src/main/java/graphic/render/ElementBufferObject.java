package graphic.render;

import graphic.render.program.BufferObject;
import graphic.render.program.DataType;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBufferData;

/**
 * Element Buffer Object. It's a wrapper for OpenGL EBO. It's used to store vertex indices.
 */
public final class ElementBufferObject extends BufferObject<ElementBufferObject> {
    private DataType dataType;
    /**
     * Creates a buffer object.
     *
     */
    public ElementBufferObject() {
        super();
        bufferType = GL_ELEMENT_ARRAY_BUFFER;
    }


    @Override
    public ElementBufferObject bind() {
        return bind(bufferType);
    }

    public ElementBufferObject buffer(Buffer data, Usage mode) {
        dataType = switch (data) {
            case ByteBuffer byteBuffer -> {
                glBufferData(bufferType, byteBuffer, mode.glUsage());
                yield DataType.BYTE;
            }

            case ShortBuffer shortBuffer -> {
                glBufferData(bufferType, shortBuffer, mode.glUsage());
                yield DataType.SHORT;
            }

            case IntBuffer intBuffer -> {
                glBufferData(bufferType, intBuffer, mode.glUsage());
                yield DataType.INT;
            }
            default -> throw new IllegalArgumentException("Unsupported buffer type: " + data.getClass());
        };

        return this;
    }
}
