package game

import engine.AbstractLogic
import graphic.render.Mesh
import graphic.render.Renderer
import graphic.window.Window
import io.inputs.KeyListener
import org.lwjgl.glfw.GLFW

class PokeGameLogic(): AbstractLogic(Renderer()) {
    private var direction = 0
    private var color = 0.0f
    private lateinit var mesh: Mesh


    /**
     * Initialization of game logic.
     *
     * @throws Exception
     */
    override fun init() {
        window = Window.get() //TODO: La inicializacion de la ventana deberia estar como parametro al instanciar AbstractLogic
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
     * This function is called once per update. Each update correlates to one render update.
     */
    override fun firstStep() {
        color += direction * 0.01f;
        if (color > 1) {
            color = 1.0f;
        } else if (color < 0) {
            color = 0.0f;
        }
    }

    /**
     * @param updates The max number of times the code inside the function should be called per update
     */
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
    override fun render() {
        window.setClearColor(color, color, color, 0.0f)
        renderer.render(mesh, window)
    }

    override fun end() {
        renderer.cleanup()
        mesh.cleanUp()
    }
}