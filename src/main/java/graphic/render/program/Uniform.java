package graphic.render.program;

import graphic.render.shader.ShaderError;
import org.joml.*;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL40.*;

/**
 * Represents a uniform variable in a shader program. A uniform variable is a global variable that is the same for all
 * vertices of a primitive being processed. Uniforms are used target communicate with the shader program.
 */
public record Uniform(int program, int location) {
    public Uniform {
        if (location < 0)
            throw new IllegalArgumentException("Location must be positive: " + location);
    }

    /**
     * @param name the name of the uniform.
     * @param programID the shader program ID.
     * @return the uniform with the given name.
     * @throws ShaderError if the uniform is not found.
     */
    public static Uniform from(String name, int programID) {
        System.out.println("Creating uniform: " + name);
        System.out.println("Program ID: " + programID);

        int location = glGetUniformLocation(programID, name);
        System.out.println("Location: " + location);

        if (location < 0)
            throw new ShaderError("Uniform creation error. Could not find the uniform: " + name);

        return new Uniform(programID, location);
    }

    private void useProgram() {
        glUseProgram(program);
    }

    private void unuseProgram() {
        ShaderProgram.unuse();
    }

    public void set(int value) {
        useProgram();
        glUniform1i(location, value);
        unuseProgram();
    }

    public void set(int x, int y) {
        useProgram();
        glUniform2i(location, x, y);
        unuseProgram();
    }

    public void set(int x, int y, int z) {
        useProgram();
        glUniform3i(location, x, y, z);
        unuseProgram();
    }

    public void set(int x, int y, int z, int w) {
        useProgram();
        glUniform4i(location, x, y, z, w);
        unuseProgram();
    }

    public void set(float value) {
        useProgram();
        glUniform1f(location, value);
        unuseProgram();
    }

    public void set(float x, float y) {
        useProgram();
        glUniform2f(location, x, y);
        unuseProgram();
    }

    public void set(float x, float y, float z) {
        useProgram();
        glUniform3f(location, x, y, z);
        unuseProgram();
    }

    public void set(float x, float y, float z, float w) {
        useProgram();
        glUniform4f(location, x, y, z, w);
        unuseProgram();
    }

    public void set(float[] values) {
        useProgram();
        switch (values.length) {
            case 1 -> glUniform1fv(location, values);
            case 2 -> glUniform2fv(location, values);
            case 3 -> glUniform3fv(location, values);
            case 4 -> glUniform4fv(location, values);
            default -> throw new IllegalArgumentException("Invalid number of values: " + values.length);
        }
        unuseProgram();
    }

    public void set(int[] values) {
        useProgram();
        switch (values.length) {
            case 1 -> glUniform1iv(location, values);
            case 2 -> glUniform2iv(location, values);
            case 3 -> glUniform3iv(location, values);
            case 4 -> glUniform4iv(location, values);
            default -> throw new IllegalArgumentException("Invalid number of values: " + values.length);
        }
        unuseProgram();
    }

    public void set(Matrix2f matrix, boolean transpose) {
        useProgram();
        glUniformMatrix2fv(location, transpose, matrix.get(new float[4]));
        unuseProgram();
    }

    public void set(Matrix3f matrix, boolean transpose) {
        useProgram();
        glUniformMatrix3fv(location, transpose, matrix.get(new float[9]));
        unuseProgram();
    }

    public void set(Matrix4f matrix, boolean transpose) {
        useProgram();
        glUniformMatrix4fv(location, transpose, matrix.get(new float[16]));
        unuseProgram();
    }

    public void set(Matrix3x2f matrix, boolean transpose) {
        useProgram();
        glUniformMatrix3x2fv(location, transpose, matrix.get(new float[6]));
        unuseProgram();
    }

    public void set(Matrix4x3f matrix, boolean transpose) {
        useProgram();
        glUniformMatrix4x3fv(location, transpose, matrix.get(new float[8]));
        unuseProgram();
    }

    public void set(Matrix2d matrix, boolean transpose) {
        useProgram();
        glUniformMatrix2dv(location, transpose, matrix.get(new double[4]));
        unuseProgram();
    }

    public void set(Matrix3d matrix, boolean transpose) {
        useProgram();
        glUniformMatrix3dv(location, transpose, matrix.get(new double[9]));
        unuseProgram();
    }

    public void set(Matrix4d matrix, boolean transpose) {
        useProgram();
        glUniformMatrix4dv(location, transpose, matrix.get(new double[16]));
        unuseProgram();
    }




}
