package graphic.render.shader;

import org.lwjgl.opengl.GL40;

import static org.lwjgl.opengl.GL40.*;

public abstract sealed class Shader implements AutoCloseable permits VertexShader, FragmentShader, GeometryShader {
	private final int id;
	private final String program;

	public Shader(String sourceProgram, int programID) {
		assert programID != 0 : "Error when creating shader:\n" + sourceProgram;

		glShaderSource(programID, sourceProgram);
		glCompileShader(programID);

		if (glGetShaderi(programID, GL_COMPILE_STATUS) == GL_FALSE)
			throw new ShaderError("Error compiling the shader: "
					+ glGetShaderInfoLog(programID, 1024));

		id = programID;
		program = sourceProgram;
	}

	/**
	 *
	 * @param sourceProgram source program of the shader.
	 * @param shaderType type of the shader.
	 * @throws ShaderError when {@link GL40 OpenGL4.0} can't create a {@link GL40#glCreateShader shader}.
	 */
	public Shader(String sourceProgram, Type shaderType) throws ShaderError {
		var programID = glCreateShader(shaderType.id());
		assert programID != 0 : "Error when creating shader of type: " + shaderType.GL_SHADER_ID;

		glShaderSource(programID, sourceProgram);
		glCompileShader(programID);

		if (glGetShaderi(programID, GL_COMPILE_STATUS) == GL_FALSE) {
			throw new ShaderError("Error compiling the shader: "
					+ glGetShaderInfoLog(programID, 1024));
		}

		this.id = programID;
		this.program = sourceProgram;
	}

	/**
	 *
	 * @return the OpenGL shader program ID.
	 */
	public int id() {
		return id;
	}

	/**
	 *
	 * @return the source shader program.
	 */
	String program() {
		return program;
	}

	/**
	 * Deletes the shader.
	 */
	@Override
	public void close() {
		glDeleteShader(id);
	}

	/**
	 * Types of possible shaders.
	 */
	public enum Type {
		VERTEX(GL_VERTEX_SHADER),
		FRAGMENT(GL_FRAGMENT_SHADER),
		GEOMETRY(GL_GEOMETRY_SHADER),
		TESSELATION_CONTROL(GL_TESS_CONTROL_SHADER),
		TESSELATION_EVALUATION(GL_TESS_EVALUATION_SHADER);

		private final int GL_SHADER_ID;

		Type(int glShaderId) {
			this.GL_SHADER_ID = glShaderId;
		}

		/**
		 *
		 * @return the GL shader type.
		 */
		public int id() {
			return GL_SHADER_ID;
		}
	}
}