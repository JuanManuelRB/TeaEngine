package graphic.render;

import static graphic.render.Shader.Type.FRAGMENT;

public final class FragmentShader extends Shader{
    public FragmentShader(String code) throws ShaderError {
        super(code, FRAGMENT);
    }
}
