package graphic.render.shader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static graphic.render.shader.Shader.Type.FRAGMENT;

public final class FragmentShader extends Shader{
    public FragmentShader(String code) throws IOException {
        super(code, FRAGMENT);
    }

    public FragmentShader(Path path) throws IOException, ShaderError {
        super(Files.readAllLines(path).get(0), FRAGMENT);
    }
}
