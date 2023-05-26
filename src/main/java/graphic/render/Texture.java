package graphic.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public class Texture {
    private Path path;
    private int id;

    public Texture(Path path) {
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT); // TODO: Make this configurable
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT); // TODO: Make this configurable
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); // TODO: Make this configurable
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); // TODO: Make this configurable

        IntBuffer width = IntBuffer.allocate(1);
        IntBuffer height = IntBuffer.allocate(1);
        IntBuffer channels = IntBuffer.allocate(1);
        ByteBuffer data = stbi_load(path.toString(), width, height, channels, 0);

        if (data == null)
            throw new RuntimeException("Failed to load texture: " + stbi_load(path.toString(), width, height, channels, 0));

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(data);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

}
