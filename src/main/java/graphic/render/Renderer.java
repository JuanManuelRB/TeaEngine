//package graphic.render;
//
//import graphic.render.program.ShaderProgram;
//import graphic.render.program.Uniform;
//import graphic.render.program.VertexArrayObject;
//import graphic.render.shader.FragmentShader;
//import graphic.render.shader.VertexShader;
//import juanmanuel.gealma.vga.vga3.Vector3;
//import org.lwjgl.opengl.GL;
//
//import java.io.IOException;
//
//import static org.lwjgl.opengl.GL15.*;
//
///**
// * A {@link Renderer} is responsible for rendering {@link View views} target a {@link Viewer viewer}.
// * It is also responsible for managing the {@link ShaderProgram shader programs} used target render the views.
// */
//public final class Renderer implements Runnable, AutoCloseable {
//    private final Viewer viewer;
//    private View view;
//    private ShaderProgram program;
//    private Uniform modelUniform, viewUniform, projectionUniform;
//    private boolean programInUse = false;
//
//    private VertexArrayObject vao;
//
//    String vertProgramStr = """
//                    #version 330 core
//                    layout (location=0) in vec3 position;
//                    layout (location=1) in vec4 color;
//
//                    uniform mat4 model;
//                    uniform mat4 view;
//                    uniform mat4 projection;
//
//
//                    out vec4 fragmentColor;
//
//                    void main()
//                    {
//                        fragmentColor = color;
//                        gl_Position = projectionMatrix * viewMatrix * model * vec4(position, 1.0);
//                    }
//                    """;
//
//    String fragProgramStr = """
//                    #version 330 core
//
//                    in vec4 fragmentColor;
//
//                    out vec4 color;
//
//                    void main()
//                    {
//                        color = fragmentColor;
//                    }
//                    """;
//
//    float[] vertices = {
//             5f,  5f, 0.0f,   0.0f, 0.0f, 1.0f, 1.0f, // Top Right     0 blue
//            -5f,  5f, 0.0f,   0.0f, 1.0f, 0.0f, 1.0f, // Top Left      1 green
//             5f, -5f, 0.0f,   1.0f, 0.0f, 0.0f, 1.0f, // Bottom Right  2 red
//            -5f, -5f, 0.0f,   0.0f, 0.0f, 0.0f, 0.0f  // Bottom Left   3 black
//    };
//
//    int[] elements = {
//            2, 0, 1,
//            2, 1, 3
//    };
//
//    /**
//     * Creates a new Renderer. This constructor is intended target be used by the {@link Viewer} after creating the OpenGL context.
//     */
//    public Renderer(Viewer viewer) {
//        this(viewer, new ShaderProgram());
//
//
////        var vertexShader = new VertexShader(Path.of("src/main/resources/Shaders/vertex/vertex.vert"));
////        var fragmentShader = new VertexShader(Path.of("src/main/resources/Shaders/fragments/fragment.frag"));
//    }
//
//    private Renderer(Viewer viewer, ShaderProgram program) {
//        this.viewer = viewer;
//
//        // This line is critical for LWJGL's interoperation with GLFW's
//        // OpenGL context, or any context that is managed externally.
//        // LWJGL detects the context that is current in the current thread,
//        // creates the GLCapabilities instance and makes the OpenGL
//        // bindings available for use.
//        GL.createCapabilities(); // TODO: there are other ways target do this, but this is the simplest one.
//        this.program = program;
//        program.use();
//    }
//
//    public Renderer useProgram() {
//        program.use();
//        programInUse = true;
//        return this;
//    }
//
//    public Renderer unuseProgram() {
//        program.unuse();
//        programInUse = false;
//        return this;
//    }
//
//    /**
//     *
//     * @return the shader program used by this renderer.
//     */
//    public ShaderProgram program() {
//        return program;
//    }
//
//    /**
//     * Initializes the Renderer.
//     * @throws IOException if the shaders cannot be read.
//     */
//    public void setView(View view) throws IOException {
//        this.view = view;
//        // Create a Vertex Shader, a Fragment Shader and attach them target the program.
//        try (var vertexShader = new VertexShader(vertProgramStr); var fragmentShader = new FragmentShader(fragProgramStr)) {
//            program.attachShader(vertexShader).attachShader(fragmentShader).link().use();
//        }
//
//        modelUniform = program.createUniform("model");
//        projectionUniform = program.createUniform("projection");
//        viewUniform = program.createUniform("view");
//    }
//
//    /**
//     * Renders the current view target the viewer.
//     */
//    public void render() {
//        // Get the current view ->
//        // (Get projection matrix)
//        // & (Get camera -> Get view matrix)
//        // & (Get model ->
//        //      Get mesh ->
//        //          Get vao
//        //      & (Get position & Get rotation & Get scale -> Get model matrix)
//        // Render
//        //
//
//        view.camera().position(view.camera().position().plus(new Vector3(-.1f, 0f, 0f)));
//        //glViewport(0, 0, viewer.getWidth(), viewer.getHeight()); //TODO: this scales and deforms the rendering, maybe with matrix transformations?
//        clear();
//
//        this.useProgram();
//
//        var camera = view.camera();
//        /*projectionUniform.set(camera.projectionMatrix(), false); TODO
//        viewUniform.set(camera.viewMatrix(), false);
//        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
//
//        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//
//        view.meshes().forEach(mesh -> program.draw(mesh));
//        view.models().forEach(model -> {
//            modelUniform.set(model.modelMatrix(), false);
//
//            model.meshes().forEach(mesh -> program.draw(mesh));
//        });*/
//
//        program.bind(vao);
//        vao.enableAttributes();
//
//        glDrawElements(GL_TRIANGLES, elements.length, GL_UNSIGNED_INT, 0);
//        vao.disableAttributes();
//
//        this.unuseProgram();
//    }
//
//    public void clear() {
//        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//    }
//
//
//    @Override
//    public void close() throws Exception {
//        if (program != null)
//            program.close();
//    }
//
//    @Override
//    public void run() {
//        render();
//    }
//}
