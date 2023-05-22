package graphic.render.shader;

import static graphic.render.shader.Shader.Type.FRAGMENT;

public final class FragmentShader extends Shader{
    public FragmentShader(String code) throws ShaderError {
        super(code, FRAGMENT);
    }
}
