package graphic.render;

import static org.lwjgl.opengl.GL32.*;

public abstract sealed class Shader implements AutoCloseable permits VertexShader, FragmentShader, GeometryShader {
	private final int shaderID;
	public Shader(String code, Type shaderType) throws ShaderError {
		shaderID = glCreateShader(shaderType.typeID);
		if (shaderID == 0)
			throw new ShaderError("Error when creating shader of type: " + shaderType.typeID);

		glShaderSource(shaderID, code);
		glCompileShader(shaderID);

		if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE)
			throw new ShaderError("Error compiling the shader: "
					+ glGetShaderInfoLog(shaderID, 1024));
	}

	public int shaderID() {
		return shaderID;
	}

	@Override
	public void close() {
		glDeleteShader(shaderID);
	}



//	public Optional<Integer> uniform(String name) {
//		return Optional.of(uniforms.get(name));
//	}
//	public Set<Map.Entry<String, Integer>> uniforms() {
//		return uniforms.entrySet();
//	}


	public enum Type {
		VERTEX(GL_VERTEX_SHADER),
		FRAGMENT(GL_FRAGMENT_SHADER),
		GEOMETRY(GL_GEOMETRY_SHADER);

		private final int typeID;

		private Type(int typeID) {
			this.typeID = typeID;
		}

		public int typeID() {
			return typeID;
		}
	}
}