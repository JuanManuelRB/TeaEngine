package graphic.render.shader;

import static graphic.render.shader.Shader.Type.GEOMETRY;

public final class GeometryShader extends Shader {
    public GeometryShader(String code) throws ShaderError {
        super(code, GEOMETRY);
    }
}
