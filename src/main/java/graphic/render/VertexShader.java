package graphic.render;

import static graphic.render.Shader.Type.VERTEX;

/**
 *
 */
public final class VertexShader extends Shader {
    public VertexShader(String program) {
        super(program, VERTEX);
    }

}
