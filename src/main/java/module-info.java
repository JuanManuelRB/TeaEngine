module JEngine.main.engine {
    requires java.base;
    requires transitive org.lwjgl;
    requires transitive org.lwjgl.glfw;
    requires transitive org.lwjgl.opengl;
    requires transitive org.joml;
    requires transitive kotlin.stdlib;

    exports engine;
    exports engine.graphic;
    exports engine.io;
    // exports engine.sound;
    // exports engine.ai;
    // exports engine.net;


}


