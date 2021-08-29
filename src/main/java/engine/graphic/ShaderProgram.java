package engine.graphic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import engine.io.ResourceLoader;


import static org.lwjgl.opengl.GL20.*;


/**
 *
 */
public class ShaderProgram {
    private final int programID;//TODO: convert to record?
    private int vertexShaderID, fragmentShaderID, geometryShaderID;
//    private Map<String, Integer> uniforms;

    int positionSize = 3;
    int colorSize = 4;
    int floatSizeBytes = 4;
    int vertexSizeBytes = (positionSize + colorSize) * floatSizeBytes;


    public ShaderProgram() throws ShaderError {
        programID = glCreateProgram();
        if (programID == 0)
            throw new ShaderError("Shader program creation error: Could not create the shader program");
//        uniforms = new HashMap<>();TODO
    }

    //TODO: documentation, because I don't know what this does or what is it's purpose.
//    public void createUniform(String uniformName) throws ShaderError {
//        int uniformLocation = glGetUniformLocation(programID, uniformName);
//        if (uniformLocation < 0)
//            throw new ShaderCreationError("Could not locate the Uniform: " + uniformName);
//        uniforms.put(uniformName, uniformLocation);
//    }
//
//    public void setUniform(String uniformName, Matrix4f value) {
//        try (MemoryStack stack = MemoryStack.stackPush()){
//            glUniformMatrix4fv(uniforms.get(uniformName), false, value.get(stack.mallocFloat(16)));
//        }
//    }

    /**
     *
     * @param vertexShaderPath
     * @throws ShaderError
     */
    public void createVertexShader(Path vertexShaderPath) throws ShaderError, IOException {
        try {
            vertexShaderID = createShader(ResourceLoader.loadShader(vertexShaderPath), GL_VERTEX_SHADER);

        } catch (IOException ioEx) {
            throw ioEx;
        } catch (Exception e) {
            throw new ShaderError(
                    "Could not create vertex shader: " + vertexShaderPath.toString(), e);
        }
    }

    /**
     *
     * @param fragmentShaderPath A path to a fragment shader code.
     * @throws ShaderError
     */
    public void createFragmentShader(Path fragmentShaderPath) throws ShaderError, IOException {
        try {
            fragmentShaderID = createShader(ResourceLoader.loadShader(fragmentShaderPath), GL_FRAGMENT_SHADER);

        }catch (IOException ioEx) {
            throw ioEx;
        }catch (Exception e) {
            throw new ShaderError(
                    "Could not create fragment shader: " + fragmentShaderPath.toString(), e);
        }
    }

//    public int createGeometryShader(Path geometryShaderPath) throws GeometryCreationError, IOException {
//        try {
//            geometryShaderID = createShader(ResourceLoader.loadShader(geometryShaderPath), GL_SHADER);
//        }catch (IOException ioEx) {
//            //TODO
//        }catch (Exception e) {
//            throw new GeometryCreationError(
//                    "Couldn't create fragment shader: " + geometryShaderPath.toString(), e);
//        }
//    }TODO: create geometry shader

    /**
     *
     * @param shaderCode Código de shader de tipo String.
     * @param shaderType Tipo de Shader, GL_FRAGMENT_SHADER o GL_VERTEX_SHADER.
     * @return Devuelve el identificador del shader proporcionado por OpenGL. El shader ya esta asignado al
     *         identificador del programa
     * @throws Exception WIP
     */
    protected int createShader(String shaderCode, int shaderType) throws ShaderError {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0)
            throw new ShaderError("Error when creating shader of type: " + shaderType);

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE)
            throw new ShaderError("Error compiling the shader: "
                                    + glGetShaderInfoLog(shaderId, 1024));

        glAttachShader(programID, shaderId);
        return shaderId;
    }

    /**
     * Enlaza el programa a OpenGL
     *
     * @throws Exception No se pudo enlazar el ID de programa.
     */
    public void link() throws ShaderError {
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
            throw new ShaderError("Error linking shader code: "
                    + glGetProgramInfoLog(programID, 1024));

        //Detach vertex and fragment shaders from the program
        if (vertexShaderID != 0)
            glDetachShader(programID, vertexShaderID);

        if (fragmentShaderID != 0)
            glDetachShader(programID, fragmentShaderID);

        // TODO: Validación OpenGL, creo que debe eliminarse en fase de producción.
        glValidateProgram(programID);


        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0)
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programID, 1024));
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programID != 0) {
            glDeleteProgram(programID);
        }
    }

//    public int generarEBO() {
//        // Guardar array en memoria
//        IntBuffer elementBuffer = MemoryUtil.memAllocInt(elementArray.length);
//        elementBuffer.put(elementArray).flip();
//
//        // Generar el EBO
//        eboID = glGenBuffers();
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER , eboID);
//        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);
//
//        return elementBuffer;
//    }

    public void createAttribPointer(int positionSize, int colorSize, int floatSizeBytes) {
        glVertexAttribPointer(0, positionSize,GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }//TODO

}
