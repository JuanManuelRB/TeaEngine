package graphic.render.shader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static graphic.render.shader.Shader.Type.VERTEX;

/**
 *
 */
public final class VertexShader extends Shader {
    public VertexShader(String program) {
        super(program, VERTEX);
    }

//    public VertexShader(Path path) { // TODO: Implement when call to this constructor is not restricted to be the top statement. All my hopes are on you project Amber.
//        try {
//            this(Files.readAllLines(path).get(0));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public VertexShader(Path path) throws IOException, ShaderError {
        super(Files.readAllLines(path).get(0), VERTEX);
    }

}
