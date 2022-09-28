package graphic.render;

import static org.lwjgl.opengl.GL32.*;

public abstract sealed class Shader implements AutoCloseable permits VertexShader, FragmentShader, GeometryShader {
	private final int id;
	public Shader(String code, Type shaderType) throws ShaderError {
		id = glCreateShader(shaderType.id());
		if (id == 0)
			throw new ShaderError("Error when creating shader of type: " + shaderType.id);

		glShaderSource(id, code);
		glCompileShader(id);

		if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
			throw new ShaderError("Error compiling the shader: "
					+ glGetShaderInfoLog(id, 1024));
	}

	public int id() {
		return id;
	}

	/**
	 * Deletes the shader.
	 */
	@Override
	public void close() {
		glDeleteShader(id);
	}



//	public Optional<Integer> uniform(String name) {
//		return Optional.of(uniforms.get(name));
//	}
//	public Set<Map.Entry<String, Integer>> uniforms() {
//		return uniforms.entrySet();
//	}

	/**
	 * Types of possible shaders.
	 */
	public enum Type {
		VERTEX(GL_VERTEX_SHADER),
		FRAGMENT(GL_FRAGMENT_SHADER),
		GEOMETRY(GL_GEOMETRY_SHADER);

		private final int id;

		Type(int id) {
			this.id = id;
		}

		/**
		 *
		 * @return the GL shader type.
		 */
		public int id() {
			return id;
		}
	}
}