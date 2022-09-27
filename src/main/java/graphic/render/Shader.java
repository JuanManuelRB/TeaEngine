package graphic.render;

import org.lwjgl.opengl.GL20;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.lwjgl.opengl.GL20.*;

public final class Shader implements AutoCloseable{
	private final int programID;

	private final Map<String, Integer> uniforms = new HashMap<>();

	/**
	 * Creates a shader program from an existing one.
	 *
	 * @param programID Other shader program.
	 * @throws ShaderError
	 */
	public Shader(int programID) throws ShaderError {
		this.programID = programID;
		if (programID == 0)
			throw new ShaderError("Shader program creation error: Could not create the shader program");
	}

	/**
	 * Creates a new shader program.
	 * @throws ShaderError
	 */
	public Shader() throws ShaderError {
		this(glCreateProgram());
	}

//  El programa no deberia ser usado por mas de un objeto al mismo tiempo, de lo contrario podrian aparecer
//  condiciones de carrera cuando dos o mas objetos diferentes intenten modificar los shaders del programa.
//
//	/**
//	 *
//	 * @return the program ID.
//	 */
//	public int programID() {
//		return programID;
//	}

	public Optional<Integer> uniform(String name) {
		return Optional.of(uniforms.get(name));
	}
	public Set<Map.Entry<String, Integer>> uniforms() {
		return uniforms.entrySet();
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (!(o instanceof Shader shader))
			return false;

		return programID == shader.programID;
	}

	@Override
	public int hashCode() {
		return programID;
	}

	public void create(String shaderCode, Type type) throws ShaderError {
		int shaderID = glCreateShader(type.typeID());
		if (shaderID == 0)
			throw new ShaderError("Error when creating shader of type: " + type);

		glShaderSource(shaderID, shaderCode);

	}

	/**
	 * Uniforms are global GLSL variables that shaders can use and can be used to communicate with them.
	 *
	 * @param uniformName Name of the uniform
	 * @throws ShaderError Throws when a {@link GL20#glGetUniformLocation OpenGL2.0 uniform} can't be obtained.
	 */
	public void createUniform(String uniformName) throws ShaderError {
		int uniformLocation = glGetUniformLocation(programID, uniformName);
		if (uniformLocation == 0)
			throw new ShaderError("Uniform creation error: Could not find the uniform" + uniformName);

		uniforms.put(uniformName, uniformLocation);
	}

	public void link() throws ShaderError {
		glLinkProgram(programID);
		if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
            throw new ShaderError("Error linking shader code: "
                    + glGetProgramInfoLog(programID, 1024));

	}

	@Override
	public void close() throws Exception {

	}

	public enum Type {
		Vertex(GL_VERTEX_SHADER),
		Fragment(GL_FRAGMENT_SHADER);
//		Geometry(GL_SHADER)

		private final int typeID;

		private Type(int typeID) {
			this.typeID = typeID;
		}

		public int typeID() {
			return typeID;
		}
	}

	public void bind() {
        glUseProgram(programID);
    }

    public static void unbind() {
        glUseProgram(0);
    }
}