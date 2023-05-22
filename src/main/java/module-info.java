module voxelengine {
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires geometricalgebra;

    requires transitive kotlin.stdlib;

    requires transitive org.lwjgl.natives;
    requires transitive org.lwjgl.glfw.natives;

    requires transitive org.joml;

}