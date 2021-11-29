module voxelengine {
    requires java.base;

    //
    requires transitive kotlin.stdlib;

    requires transitive org.lwjgl.natives;
    requires transitive org.lwjgl.glfw;
    requires transitive org.lwjgl.opengl;
    requires transitive org.lwjgl.glfw.natives;
    requires transitive org.joml;

    requires annotations;


    exports engine;
    exports engine.graphic;
    exports engine.io;

    // exports engine.sound;
    // exports engine.ai;
    // exports engine.net;

    opens engine.io.inputs;
    opens engine.graphic;
    exports engine.graphic.window;
    opens engine.graphic.window;
    exports engine.graphic.render;
    opens engine.graphic.render;
    exports engine.graphic.scene;
    opens engine.graphic.scene;
//    opens engine.io.outputs;




}


