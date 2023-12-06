package graphic.render.program;

import static org.lwjgl.opengl.GL11.*;

public enum DataType {
    UBYTE(GL_UNSIGNED_BYTE),
    USHORT(GL_UNSIGNED_SHORT),
    UINT(GL_UNSIGNED_INT),
    BYTE(GL_BYTE),
    SHORT(GL_SHORT),
    INT(GL_INT),
    FLOAT(GL_FLOAT),
    DOUBLE(GL_DOUBLE);

    private final int type;

    DataType(int type) {
        this.type = type;
    }

    public int glType() {
        return type;
    }
}
