module tea {
    requires java.management;
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.joml;
    requires gealma;

    requires transitive kotlin.stdlib;

    requires org.lwjgl.stb;
    requires tea_engine.core;
}