package graphic.render.shader;

import static graphic.render.shader.Shader.Type.VERTEX;

/**
 *
 */
public final class VertexShader extends Shader {
    public VertexShader(String program) {
        super(program, VERTEX);
    }

}
