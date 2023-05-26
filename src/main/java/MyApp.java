import graphic.render.ShaderProgram;
import graphic.render.shader.FragmentShader;
import graphic.render.shader.ShaderError;
import graphic.render.shader.VertexShader;

import java.io.IOException;
import java.nio.file.Path;

public class MyApp {
    public static void main(String[] args) {
        try (
                var shaderProgram = new ShaderProgram();
                var vertexShader = new VertexShader(Path.of("src/main/resources/Shaders/vertex/vertex.vert"));
                var fragmentShader = new FragmentShader(Path.of("src/main/resources/Shaders/fragments/fragment.frag"))
        ) {

        } catch (IOException e) {
            e.printStackTrace();

        } catch (ShaderError shaderError) {
            shaderError.printStackTrace();
        }
    }
}
