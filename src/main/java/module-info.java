module JEngine.main.engine {
    requires java.base;
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.joml;
    requires kotlin.stdlib;

    exports engine;
    exports engine.graphic;
    exports engine.io;
    // exports engine.sound;
    // exports engine.ai;
    // exports engine.net;


}


