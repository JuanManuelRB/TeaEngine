package graphic.render;

import static graphic.render.Shader.Type.GEOMETRY;

public final class GeometryShader extends Shader {
    public GeometryShader(String code) throws ShaderError {
        super(code, GEOMETRY);
    }
}
