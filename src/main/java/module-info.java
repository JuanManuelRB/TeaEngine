module VoxelEngine {
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires annotations;

    requires transitive kotlin.stdlib;

    requires transitive org.lwjgl.natives;
    requires transitive org.lwjgl.glfw.natives;

    requires transitive org.joml;

    opens

}