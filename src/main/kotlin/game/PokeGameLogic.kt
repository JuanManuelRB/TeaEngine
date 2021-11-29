package game

import engine.Logic
import engine.graphic.render.Mesh
import engine.graphic.render.Renderer
import engine.graphic.window.Window
import engine.io.inputs.KeyListener
import org.lwjgl.glfw.GLFW

class PokeGameLogic(): Logic {
    private var direction = 0
    private var color = 0.0f
    private val renderer = Renderer()
    private lateinit var mesh: Mesh


    /**
     * Punto de entrada donde se inicializan los valores del juego.
     *
     * @throws Exception
     */
    override fun init() {

        // Inicialización de la ventana y del renderer
        Window.get()

        renderer.init() // Init renderer
        val positions = floatArrayOf(
            -0.5f, 0.5f, -1.05f,
            -0.5f, -0.5f, -1.05f,
            0.5f, -0.5f, -1.05f,
            0.5f, 0.5f, -1.05f
        )
        val colours = floatArrayOf(
            0.5f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.5f,
            0.0f, 0.0f, 0.5f
        )
        val indices = intArrayOf(
            0, 1, 3, 3, 1, 2
        )
        mesh = Mesh(positions, colours, indices)
    }

    /**
     * Punto de entrada para todas las entradas de datos del juego.
     * Se ejecuta antes que cualquiera de los pasos.
     *
     * @throws Exception
     */
    override fun inputEvents() {
        direction = when {
            KeyListener.activeKey(GLFW.GLFW_KEY_UP) ->  1
            KeyListener.activeKey(GLFW.GLFW_KEY_DOWN) -> -1
            else -> 0

        }
    }

    /**
     * Primer código a ejecutarse en la lógica.
     */
    override fun firstStep() {
        color += direction * 0.01f;
        if (color > 1) {
            color = 1.0f;
        } else if (color < 0) {
            color = 0.0f;
        }
    }

    override fun mainSteps(updates: Int) {
        //TODO: main body of the loop, implement number of iterations
    }

    /**
     * Ultimo codigo a ejecutarse.
     */
    override fun lastStep() {
    }

    /**
     * Método donde se implementa la renderización del juego.
     *
     *
     * @throws Exception
     */
    override fun render(window: Window) {
        Window.setClearColor(color, color, color, 0.0f)
        renderer.render(window, mesh)
    }

    override fun end() {
        renderer.cleanup()
        mesh.cleanUp()
    }
}