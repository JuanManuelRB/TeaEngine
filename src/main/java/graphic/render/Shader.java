package graphic.render;

import static org.lwjgl.opengl.GL20.*;

public sealed abstract class Shader implements AutoCloseable permits VertexShader, FragmentShader, GeometryShader {
	private final int programID;

	public Shader(int programID) throws ShaderError {
		this.programID = programID;
        if (programID == 0)
	        throw new ShaderError("Shader program creation error: Could not create the shader program");
	}

	public Shader() throws ShaderError {
		this(glCreateProgram());
	}

	public abstract void create(String shaderCode, ShaderType type) throws ShaderError;

	public void link() throws ShaderError {
		glLinkProgram(programID);
		if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
            throw new ShaderError("Error linking shader code: "
                    + glGetProgramInfoLog(programID, 1024));

	}

	public enum ShaderType {
		Vertex(GL_VERTEX_SHADER),
		Fragment(GL_FRAGMENT_SHADER);
//		Geometry(GL_SHADER)

		private final int shaderType;

		private ShaderType(int shaderType) {
			this.shaderType = shaderType;
		}
	}

	@Override
	public void close() throws Exception {
        unbind();
        if (programID != 0) {
            glDeleteProgram(programID);
        }
	}

	public void bind() {
        glUseProgram(programID);
    }

    public static void unbind() {
        glUseProgram(0);
    }
}