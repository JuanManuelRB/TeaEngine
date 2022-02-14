module engine {
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
//    requires jdk.incubator.foreign;
    requires annotations;

    requires transitive kotlin.stdlib;

    requires transitive org.lwjgl.natives;
    requires transitive org.lwjgl.glfw.natives;
    requires transitive org.joml;

    exports engine;
    exports graphic.window;
    exports graphic.render;
//    exports graphic.render;
//    exports io;

    // exports engine.sound;
    // exports engine.ai;
    // exports engine.net;

    opens io.inputs;
    opens graphic.window;
//    exports graphic.render.scene;
//    opens graphic.render.scene;
}